package com.haozhuo.datag.service.biz;

import com.haozhuo.datag.common.Tuple;
import com.haozhuo.datag.model.InfoALV;
import com.haozhuo.datag.model.NewsRcmdMsg;
import com.haozhuo.datag.model.PushedInfoKeys;
import com.haozhuo.datag.model.RcmdNewsInfo;
import com.haozhuo.datag.service.DataetlJdbcService;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.KafkaService;
import com.haozhuo.datag.service.RedisService;
import com.haozhuo.datag.service.textspilt.MyKeyword;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

/**
 * Created by Lucius on 9/4/18.
 */
@Component
public class InfoRcmdService {
    private static final Logger logger = LoggerFactory.getLogger(InfoRcmdService.class);
    @Deprecated
    public static final String channelRcmdId = "10000";
    @Deprecated
    public static final String channelVideoId = "20000";
    @Deprecated
    public static final String channelLiveId = "30000";


    public static final String channelTypeRCMD = "R";
    public static final String channelTypeArticle = "A";
    public static final String channelTypeVideo = "V";
    public static final String channelTypeLive = "L";

    public static final String allCategoryId = "0";

    private final EsService esService;

    private final RedisService redisService;

    private final DataetlJdbcService dataetlJdbcService;

    private final KafkaService kafkaService;


    @Autowired
    public InfoRcmdService(EsService esService, RedisService redisService, DataetlJdbcService dataetlJdbcService, KafkaService kafkaService) {
        this.esService = esService;
        this.redisService = redisService;
        this.dataetlJdbcService = dataetlJdbcService;
        this.kafkaService = kafkaService;
    }


    public String getLoveTags(String userId, String channelId, String categoryId) {
        if (allCategoryId.equals(categoryId) || channelLiveId.equals(channelId)) {
            //获取用户感兴趣的标签
            String loveTags = redisService.getLoveTags(userId);
            logger.debug("userId:{}, loveTags:{}", userId, loveTags);
            //获取用户报告标签
            String reportTags = dataetlJdbcService.getLabelStrByUserId(userId);
            logger.debug("userId:{}, reportTags:{}", userId, reportTags);
            return loveTags + "," + reportTags;
        } else {
            return "";
        }
    }

    Random rand = new Random();

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

    /**
     * curl -XGET "192.168.1.152:9200/article4/_search?pretty" -d '{"size":10,"query":{"function_score":{"query":{"bool":{"should":[{"multi_match":{"query":"风湿关节炎食疗方剂","fields":["title","tags"],"boost":3}},{"multi_match":{"query":"肺炎近视","fields":["title","tags"],"boost":1}}],"must_not":[{"match":{"tags":"近视"}},{"ids":{"values":["131025","131574","131808"]}}]}},"functions":[{"gauss":{"create_time":{"origin":"now","scale":"30d","offset":"15d","decay":"0.8"}}}]}}}'
     */
    public InfoALV channelRecommend(String channelType, String channelId, String categoryId, String userId, int size) {
        long beginTime = System.currentTimeMillis();
        PushedInfoKeys pushedInfoKeys = new PushedInfoKeys(userId, channelId, categoryId);
        InfoALV pushedALV = redisService.getPushedInfoALV(pushedInfoKeys);
        InfoALV result = new InfoALV();

        //获取用户不感兴趣的标签
        String hateTags = redisService.getHateTags(userId);
        String[] videoIds = new String[]{};
        String[] articleIds = new String[]{};
        String[] liveIds = new String[]{};
        String loveTags = getLoveTags(userId, channelId, categoryId);

        logger.debug("userId:{}, hateTags:{}", userId, hateTags);

        if (channelTypeVideo.equalsIgnoreCase(channelType)) {//视频频道下所有
            logger.debug("视频频道下所有");

            videoIds = getRcmdIds(loveTags, 10, esService.getVideoIndex(), hateTags, pushedALV.getVideo());
            //videoIds = esService.personalizedRecommend(esService.getVideoIndex(), tag, hateTags, pushedIds, 1, esTypes);
            if (videoIds.length < size) {
                String[] supplementsVideoIds = esService.commonRecommend(esService.getVideoIndex(), hateTags, (String[]) ArrayUtils.addAll(videoIds, pushedALV.getVideo()), size - videoIds.length);
                videoIds = (String[]) ArrayUtils.addAll(videoIds, supplementsVideoIds);
            }
        } else if (channelTypeLive.equalsIgnoreCase(channelType)) { //直播频道下所有
            logger.debug("直播频道下所有");
            liveIds = getRcmdIds(loveTags, 10, esService.getLiveIndex(), hateTags, pushedALV.getLive());

            if (liveIds.length < size) {
                String[] supplementsLiveIds = esService.commonRecommend(esService.getLiveIndex(), hateTags, (String[]) ArrayUtils.addAll(liveIds, pushedALV.getLive()), size - liveIds.length);
                liveIds = (String[]) ArrayUtils.addAll(liveIds, supplementsLiveIds);
            }
        } else if (channelTypeRCMD.equalsIgnoreCase(channelType)) { //推荐频道下所有
            logger.debug("推荐频道下所有");
            videoIds = esService.commonRecommend(esService.getVideoIndex(), hateTags, pushedALV.getVideo(), 2);
            liveIds = esService.commonRecommend(esService.getLiveIndex(), hateTags, pushedALV.getLive(), 2);
            if (liveIds.length + videoIds.length >= 3) {
                videoIds = new String[]{videoIds[0]};
                liveIds = new String[]{liveIds[0]};
            }

            articleIds = getRcmdIds(loveTags, 8, esService.getArticleIndex(), hateTags, pushedALV.getArticle());

            int nowSize = articleIds.length + videoIds.length + liveIds.length;
            System.out.println("articleIds size:" + articleIds.length);
            if (nowSize < size) { //articleIdsByTags可能数量会比较少，所以随机补充文章
                String[] supplementsArticleIds = esService.commonRecommend(esService.getArticleIndex(), hateTags, (String[]) ArrayUtils.addAll(articleIds, pushedALV.getArticle()), size - nowSize);
                articleIds = (String[]) ArrayUtils.addAll(articleIds, supplementsArticleIds);
            }
        } else if (allCategoryId.equals(categoryId)) {  //文章某个频道下所有
            logger.debug("文章某个频道下所有");
            //获取用户感兴趣的标签
            String[] esTypes = dataetlJdbcService.channelEsTypeMap.get(channelId);
            logger.debug("文章某个频道下所有,esTypes:{}", stream(esTypes).collect(joining(",")));
            videoIds = esService.commonRecommend(esService.getVideoIndex(), hateTags, pushedALV.getVideo(), 2, esTypes);

            articleIds = getRcmdIds(loveTags, 8, esService.getArticleIndex(), hateTags, pushedALV.getArticle(), esTypes);

            int nowSize = articleIds.length + videoIds.length + liveIds.length;
            if (nowSize < size) { //articleIdsByTags可能数量会比较少，所以随机补充文章
                String[] supplementsArticleIds = esService.commonRecommend(esService.getArticleIndex(), hateTags, (String[]) ArrayUtils.addAll(articleIds, pushedALV.getArticle()), size - nowSize, esTypes);
                articleIds = (String[]) ArrayUtils.addAll(articleIds, supplementsArticleIds);
            }
        } else { //文章某个频道下所有某个类别
            String esType = channelId + "_" + categoryId;
            logger.debug("文章某个频道下所有,esTypes:{}", esType);
            videoIds = esService.commonRecommend(esService.getVideoIndex(), hateTags, pushedALV.getVideo(), 2, esType);
            articleIds = esService.commonRecommend(esService.getArticleIndex(), hateTags, pushedALV.getArticle(), size - videoIds.length, esType);
        }
        result.setArticle(articleIds);
        result.setVideo(videoIds);
        result.setLive(liveIds);

        if (result.size() < size) { //表示已经推完了，下一次重新推
            redisService.deleteHashKeyByPushedInfoKeys(pushedInfoKeys);
        } else {
            redisService.setPushedInfoALV(pushedInfoKeys, pushedALV, result);
        }

        logger.info("/mul/ALV/user_channel?channelType={}&userId={}&channelId={}&categoryId={}  cost: {}ms", channelType, userId, channelId, categoryId, System.currentTimeMillis() - beginTime);

        return result;
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

            videoIds = getRcmdIds(getLoveTags(userId, channelId, categoryId), 10, esService.getVideoIndex(), hateTags, pushedALV.getVideo());
            //videoIds = esService.personalizedRecommend(esService.getVideoIndex(), tag, hateTags, pushedIds, 1, esTypes);
            if (videoIds.length < size) {
                String[] supplementsVideoIds = esService.commonRecommend(esService.getVideoIndex(), hateTags, (String[]) ArrayUtils.addAll(videoIds, pushedALV.getVideo()), size - videoIds.length);
                videoIds = (String[]) ArrayUtils.addAll(videoIds, supplementsVideoIds);
            }
        } else if (channelTypeLive.equalsIgnoreCase(channelType)) { //直播频道下所有
            logger.debug("直播频道下所有");
            liveIds = getRcmdIds(getLoveTags(userId, channelId, categoryId), 10, esService.getLiveIndex(), hateTags, pushedALV.getLive());
            if (liveIds.length < size) {
                String[] supplementsLiveIds = esService.commonRecommend(esService.getLiveIndex(), hateTags, (String[]) ArrayUtils.addAll(liveIds, pushedALV.getLive()), size - liveIds.length);
                liveIds = (String[]) ArrayUtils.addAll(liveIds, supplementsLiveIds);
            }
        } else if (channelTypeRCMD.equalsIgnoreCase(channelType)) { //推荐频道下所有
            logger.debug("推荐频道下所有");
            videoIds = esService.commonRecommend(esService.getVideoIndex(), hateTags, pushedALV.getVideo(), 2);
            liveIds = esService.commonRecommend(esService.getLiveIndex(), hateTags, pushedALV.getLive(), 2);
            if (liveIds.length + videoIds.length >= 3) {
                videoIds = new String[]{videoIds[0]};
                liveIds = new String[]{liveIds[0]};
            }

            int compSize = size - videoIds.length - liveIds.length;
            RcmdNewsInfo rcmdNewsInfo = redisService.getRcmdNews(userId, compSize);

            checkIfRequestRcmd(rcmdNewsInfo, userId);
            articleIds = rcmdNewsInfo.getNews().stream().toArray(String[]::new);

            if (articleIds.length < compSize) {
                articleIds = esService.commonRecommend(esService.getArticleIndex(), hateTags, pushedALV.getArticle(), compSize);
                getRcmdIds(getLoveTags(userId, channelId, categoryId), compSize, esService.getArticleIndex(), hateTags, pushedALV.getArticle());
                logger.info("用户 {} 推荐频道 Redis中数据不够，从ES中为取数据", userId);
            } else {
                logger.info("从Redis中为用户:{} 取出{}条数据", userId, articleIds.length);
            }

        } else if (allCategoryId.equals(categoryId)) {  //文章某个频道下所有
            logger.debug("文章某个频道下所有");
            //获取用户感兴趣的标签
            String[] esTypes = dataetlJdbcService.channelEsTypeMap.get(channelId);
            videoIds = esService.commonRecommend(esService.getVideoIndex(), hateTags, pushedALV.getVideo(), 2, esTypes);
            int compSize = size - videoIds.length;

            RcmdNewsInfo rcmdNewsInfo = redisService.getRcmdNewsByChannel(userId, channelId, size - videoIds.length);

            checkIfRequestRcmd(rcmdNewsInfo, userId);
            articleIds = rcmdNewsInfo.getNews().stream().toArray(String[]::new);
            if (articleIds.length == 0) {
                logger.info("用户 {} 的 channelId {}:Redis中没有数据，从ES中取数据", userId, channelId);
                articleIds = esService.commonRecommend(esService.getArticleIndex(), hateTags, (String[]) ArrayUtils.addAll(articleIds, pushedALV.getArticle()), compSize, esTypes);
            } else {
                logger.info("从Redis中频道: {} 用户:{} 取出{}条数据", channelId, userId, articleIds.length);
            }
        } else { //文章某个频道下所有某个类别
            String esType = channelId + "_" + categoryId;
            logger.debug("文章某个频道下所有,esTypes:{}", esType);
            videoIds = esService.commonRecommend(esService.getVideoIndex(), hateTags, pushedALV.getVideo(), 2, esType);
            articleIds = esService.commonRecommend(esService.getArticleIndex(), hateTags, pushedALV.getArticle(), size - videoIds.length, esType);
        }
        result.setArticle(articleIds);
        result.setVideo(videoIds);
        result.setLive(liveIds);

        if (result.size() < size) { //表示已经推完了，下一次重新推
            redisService.deleteHashKeyByPushedInfoKeys(pushedInfoKeys);
        } else {
            redisService.setPushedInfoALV(pushedInfoKeys, pushedALV, result);
        }

        logger.info("/mul/ALV/user_channel?channelType={}&userId={}&channelId={}&categoryId={}  cost: {}ms", channelType, userId, channelId, categoryId, System.currentTimeMillis() - beginTime);

        return result;
    }

    private void checkIfRequestRcmd(RcmdNewsInfo rcmdNewsInfo, String userId) {
        List<String> channelIds = rcmdNewsInfo.getRequestRcmdChannels();
        if (rcmdNewsInfo.isInitAllChannels()) {
            channelIds.addAll(RcmdNewsInfo.getAllChannelIds()) ;
        }
        channelIds.forEach(channelId -> kafkaService.sendRcmdRequestMsg(new NewsRcmdMsg(userId, channelId, 1)));

    }

    public InfoALV channelRecommend(String channelType, String channelId, String categoryId, int pageNo, int size) {
        long beginTime = System.currentTimeMillis();
        String[] videoIds;
        String[] liveIds = new String[]{};
        String[] articleIds = new String[]{};
        InfoALV result = new InfoALV();
        if (channelTypeVideo.equalsIgnoreCase(channelType)) {//视频频道下所有
            logger.debug("视频频道下所有");
            videoIds = esService.heatRecommend(esService.getHeatVideoIndex(), pageNo, size);
        } else if (channelTypeRCMD.equalsIgnoreCase(channelType)) { //推荐频道
            logger.debug("推荐频道");
            videoIds = esService.heatRecommend(esService.getHeatVideoIndex(), pageNo, 1);
            liveIds = esService.heatRecommend(esService.getLiveIndex(), pageNo, 1);
            articleIds = esService.heatRecommend(esService.getHeatArticleIndex(), pageNo, size - videoIds.length - liveIds.length);
        } else if (allCategoryId.equals(categoryId)) { //文章频道下所有
            logger.debug("其他频道下所有");
            String[] categories = dataetlJdbcService.channelEsTypeMap.get(channelId);
            videoIds = esService.heatRecommend(esService.getHeatVideoIndex(), pageNo, 2, categories);
            articleIds = esService.heatRecommend(esService.getHeatArticleIndex(), pageNo, size - videoIds.length, categories);
            if (articleIds.length == 0) { //如果文章都到底了，那么就不推视频了
                videoIds = new String[]{};
            }
        } else {//文章某个频道下所有某个类别
            logger.debug("其他频道下某个分类");
            String esType = channelId + "_" + categoryId;
            videoIds = esService.heatRecommend(esService.getHeatVideoIndex(), pageNo, 2, esType);
            articleIds = esService.heatRecommend(esService.getHeatArticleIndex(), pageNo, size - videoIds.length, esType);
            if (articleIds.length == 0) {
                videoIds = new String[]{};
            }
        }
        result.setArticle(articleIds);
        result.setVideo(videoIds);
        result.setLive(liveIds);
        logger.info("/mul/ALV/channel?channelType={}&channelId={}&categoryId={}&pageNo={}  cost: {}ms", channelType, channelId, categoryId, pageNo, System.currentTimeMillis() - beginTime);
        return result;
    }
}
