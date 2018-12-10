package com.haozhuo.datag.service.biz;

import com.haozhuo.datag.model.InfoALV;
import com.haozhuo.datag.model.NewsRcmdMsg;
import com.haozhuo.datag.model.PushedInfoKeys;
import com.haozhuo.datag.model.RcmdNewsInfo;
import com.haozhuo.datag.service.DataEtlJdbcService;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.KafkaService;
import com.haozhuo.datag.service.RedisService;
import com.haozhuo.datag.model.textspilt.MyKeyword;
import lombok.Setter;
import lombok.Getter;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

/**
 * Created by Lucius on 9/4/18.
 */
@Component
public class InfoRcmdService {
    private static final Logger logger = LoggerFactory.getLogger(InfoRcmdService.class);
    public static final String channelRcmdId = "10000";
    private final int randomLiveOrVideoSize = 1;

    private static final String channelTypeRCMD = "R";
    // --Commented out by Inspection (12/7/18 3:06 PM):public static final String channelTypeArticle = "A";
    private static final String channelTypeVideo = "V";
    private static final String channelTypeLive = "L";

    public static final String allCategoryId = "0";

    private final EsService esService;

    private final RedisService redisService;

    private final DataEtlJdbcService dataetlJdbcService;

    private final KafkaService kafkaService;


    @Autowired
    public InfoRcmdService(EsService esService, RedisService redisService, DataEtlJdbcService dataetlJdbcService, KafkaService kafkaService) {
        this.esService = esService;
        this.redisService = redisService;
        this.dataetlJdbcService = dataetlJdbcService;
        this.kafkaService = kafkaService;
    }


    private String getLoveTags(String userId, String categoryId) {
        if (allCategoryId.equals(categoryId)) {
//            //获取用户感兴趣的标签
//            String loveTags = redisService.getLoveTags(userId);
//            logger.debug("userId:{}, loveTags:{}", userId, loveTags);
            //获取用户报告标签
            String reportTags = esService.getPortraitDiseaseLabelsByUserId(userId);
            logger.debug("userId:{}, reportTags:{}", userId, reportTags);
            return reportTags;
        } else {
            return "";
        }
    }

    private final Random rand = new Random();

    private String[] getRcmdIds(String tags, int size, String esIndex, String hateTags, String[] pushedIds, String... esTypes) {
        String[] tagArray = tags.split(",");
        int s = size;
        if (tagArray.length > size) {
            tags = IntStream.range(0, size).boxed().map(i -> tagArray[rand.nextInt(tagArray.length)]).collect(joining(","));
        } else {
            s = tagArray.length;
        }
        return esService.personalizedRecommend(esIndex, tags, hateTags, pushedIds, s, esTypes);
    }

    private RandomVideoOrLiveId getRandomVideoOrLiveId(String hateTags, InfoALV pushedALV, PushedInfoKeys pushedInfoKeys, String... esType) {
        RandomVideoOrLiveId result = new RandomVideoOrLiveId();
        if (System.currentTimeMillis() % 2 == 0) {
            String[] videoIds = esService.commonRecommend(esService.getVideoIndex(), hateTags, pushedALV.getVideo(), randomLiveOrVideoSize, esType);
            if (videoIds.length == 0) {
                redisService.deleteHashKeyByPushedInfoKeys(pushedInfoKeys, pushedInfoKeys.getVideoHashKey());
            }
            result.setLiveIds(new String[]{});
            result.setVideoIds(videoIds);
        } else {
            String[] liveIds = esService.commonRecommend(esService.getLiveIndex(), hateTags, pushedALV.getLive(), randomLiveOrVideoSize, esType);
            if (liveIds.length == 0) {
                redisService.deleteHashKeyByPushedInfoKeys(pushedInfoKeys, pushedInfoKeys.getLiveHashKey());
            }
            result.setLiveIds(liveIds);
            result.setVideoIds(new String[]{});
        }
        return result;
    }

    @Setter
    @Getter
    private class RandomVideoOrLiveId {
        private String[] videoIds;
        private String[] liveIds;
    }

    public InfoALV channelRecommendNews(String channelType, String channelId, String categoryId, String userId, int size) {
        long beginTime = System.currentTimeMillis();
        PushedInfoKeys pushedInfoKeys = new PushedInfoKeys(userId, channelId, categoryId);
        InfoALV pushedALV = redisService.getPushedInfoALV(pushedInfoKeys);

        InfoALV result = new InfoALV();

        //获取用户不感兴趣的标签
        String hateTags = redisService.getUserPref(userId, channelId).stream()
                .filter(x -> x.getScore() < 0).map(MyKeyword::getName).collect(joining(","));

        String[] videoIds = new String[]{};
        String[] articleIds = new String[]{};
        String[] liveIds = new String[]{};

        if (channelTypeVideo.equalsIgnoreCase(channelType)) {//视频频道下所有
            logger.debug("视频频道下所有");

            videoIds = getRcmdIds(getLoveTags(userId, categoryId), 10, esService.getVideoIndex(), hateTags, pushedALV.getVideo());
            //videoIds = esService.personalizedRecommend(esService.getVideoIndex(), tag, hateTags, pushedIds, 1, esTypes);
            if (videoIds.length < size) {
                String[] supplementsVideoIds = esService.commonRecommend(esService.getVideoIndex(), hateTags, (String[]) ArrayUtils.addAll(videoIds, pushedALV.getVideo()), size - videoIds.length);
                videoIds = (String[]) ArrayUtils.addAll(videoIds, supplementsVideoIds);
                if (videoIds.length < size) {
                    redisService.deleteHashKeyByPushedInfoKeys(pushedInfoKeys, pushedInfoKeys.getVideoHashKey());
                }
            }
        } else if (channelTypeLive.equalsIgnoreCase(channelType)) { //直播频道下所有
            logger.debug("直播频道下所有");
            liveIds = getRcmdIds(getLoveTags(userId, categoryId), 10, esService.getLiveIndex(), hateTags, pushedALV.getLive());
            if (liveIds.length < size) {
                String[] supplementsLiveIds = esService.commonRecommend(esService.getLiveIndex(), hateTags, (String[]) ArrayUtils.addAll(liveIds, pushedALV.getLive()), size - liveIds.length);
                liveIds = (String[]) ArrayUtils.addAll(liveIds, supplementsLiveIds);
            }
        } else if (channelTypeRCMD.equalsIgnoreCase(channelType)) { //推荐频道下所有
            logger.debug("推荐频道下所有");

            RandomVideoOrLiveId vl = getRandomVideoOrLiveId(hateTags, pushedALV, pushedInfoKeys);
            videoIds = vl.videoIds;
            liveIds = vl.liveIds;
            int compSize = size - randomLiveOrVideoSize;
            RcmdNewsInfo rcmdNewsInfo = redisService.getRcmdNews(userId, compSize);

            checkIfRequestRcmd(rcmdNewsInfo, userId);
            articleIds = rcmdNewsInfo.getNews().toArray(new String[0]);

            if (articleIds.length < compSize) {
                articleIds = esService.commonRecommend(esService.getArticleIndex(), hateTags, pushedALV.getArticle(), compSize);
                getRcmdIds(getLoveTags(userId, categoryId), compSize, esService.getArticleIndex(), hateTags, pushedALV.getArticle());
                logger.info("用户 {} 推荐频道 Redis中数据不够，从ES中为取数据", userId);
            } else {
                logger.info("从Redis中为用户:{} 取出{}条数据", userId, articleIds.length);
            }

        } else if (allCategoryId.equals(categoryId)) {  //文章某个频道下所有
            logger.debug("文章某个频道下所有");
            //获取用户感兴趣的标签
            String[] esTypes = dataetlJdbcService.channelEsTypeMap.get(channelId);
            RandomVideoOrLiveId vl = getRandomVideoOrLiveId( hateTags, pushedALV, pushedInfoKeys,esTypes);
            videoIds = vl.videoIds;
            liveIds = vl.liveIds;

            int compSize = size - randomLiveOrVideoSize;

            RcmdNewsInfo rcmdNewsInfo = redisService.getRcmdNewsByChannel(userId, channelId, compSize);

            checkIfRequestRcmd(rcmdNewsInfo, userId);
            articleIds = rcmdNewsInfo.getNews().toArray(new String[0]);
            if (articleIds.length < compSize) {
                logger.info("用户 {} 的 channelId {}:Redis中没有数据，从ES中取数据", userId, channelId);
                articleIds = esService.commonRecommend(esService.getArticleIndex(), (String[]) ArrayUtils.addAll(articleIds, pushedALV.getArticle()), compSize, esTypes);
            } else {
                logger.info("从Redis中频道: {} 用户:{} 取出{}条数据", channelId, userId, articleIds.length);
            }
        } else { //文章某个频道下所有某个类别
            String esType = channelId + "_" + categoryId;
            logger.debug("文章某个频道下所有,esTypes:{}", esType);

            RandomVideoOrLiveId vl = getRandomVideoOrLiveId(hateTags, pushedALV, pushedInfoKeys, esType);
            videoIds = vl.videoIds;
            liveIds = vl.liveIds;
            articleIds = esService.commonRecommend(esService.getArticleIndex(), hateTags, pushedALV.getArticle(), size - randomLiveOrVideoSize, esType);
        }
        result.setArticle(articleIds);
        result.setVideo(videoIds);
        result.setLive(liveIds);

        if (result.size() < size - randomLiveOrVideoSize) {
            redisService.deleteHashKeyByPushedInfoKeys(pushedInfoKeys); //生产环境一般不会进入这一步
        } else {
            redisService.setPushedInfoALV(pushedInfoKeys, pushedALV, result);
        }

        logger.info("/mul/ALV/user_channel?channelType={}&userId={}&channelId={}&categoryId={}  cost: {}ms", channelType, userId, channelId, categoryId, System.currentTimeMillis() - beginTime);

        return result;
    }

    private void checkIfRequestRcmd(RcmdNewsInfo rcmdNewsInfo, String userId) {
        List<String> channelIds = rcmdNewsInfo.getRequestRcmdChannels();
        if (rcmdNewsInfo.isInitAllChannels()) {
            channelIds.addAll(RcmdNewsInfo.getAllChannelIds());
        }
        channelIds.forEach(channelId -> kafkaService.sendRcmdRequestMsg(new NewsRcmdMsg(userId, channelId, 1)));

    }
}
