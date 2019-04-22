package com.haozhuo.datag.service.biz;

import com.haozhuo.datag.model.InfoALV;
import com.haozhuo.datag.model.RcmdMsg;
import com.haozhuo.datag.model.PushedInfoKeys;
import com.haozhuo.datag.model.RcmdNewsInfo;
import com.haozhuo.datag.service.DataEtlJdbcService;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.KafkaService;
import com.haozhuo.datag.service.RedisService;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Lucius on 9/4/18.
 */
public class InfoRcmdService {
    private static final Logger logger = LoggerFactory.getLogger(InfoRcmdService.class);
    public static final String channelRcmdId = "10000";
    private final int randomLiveOrVideoSize = 1;

    private static final String channelTypeRCMD = "R";
    // --Commented out by Inspection (12/7/18 3:06 PM):
    private static final String channelTypeArticle = "A";
    private static final String channelTypeVideo = "V";
    private static final String channelTypeLive = "L";

    public static final String allCategoryId = "0";

    private final EsService esService;

    private final RedisService redisService;

    private final DataEtlJdbcService dataetlJdbcService;

    private final KafkaService kafkaService;

    private Boolean sendRcmdMsg;

    public InfoRcmdService(Boolean sendRcmdMsg, EsService esService, RedisService redisService, DataEtlJdbcService dataetlJdbcService, KafkaService kafkaService) {
        this.esService = esService;
        this.redisService = redisService;
        this.dataetlJdbcService = dataetlJdbcService;
        this.kafkaService = kafkaService;
        this.sendRcmdMsg = sendRcmdMsg;
    }


    public String getLoveTags(String userId) {
        return String.join(",", redisService.getPositiveBasePref(userId, 15));
    }

    private InfoALV getVideoOrLiveIdFromMysql(InfoALV pushedALV, PushedInfoKeys pushedInfoKeys) {
        return getVideoOrLiveIdFromMysql(pushedALV, pushedInfoKeys, null);
    }

    private InfoALV getVideoOrLiveIdFromMysql(InfoALV pushedALV, PushedInfoKeys pushedInfoKeys, String channelId) {
        if(allCategoryId.equals(channelId)) {
            channelId=null;
        }
        InfoALV result = new InfoALV();
        long time = System.currentTimeMillis();
        if (System.currentTimeMillis() % 2 == 0) {
            String videoId = dataetlJdbcService.getUnPushedVideoOrLiveId(pushedALV.getVideo(), true, channelId);
            if (videoId == null) {
                redisService.deletePushedInfoKey(pushedInfoKeys.getKey(), pushedInfoKeys.getVideoHashKey());
            } else {
                result.setVideo(new String[]{videoId});
            }

        } else {
            String liveId = dataetlJdbcService.getUnPushedVideoOrLiveId(pushedALV.getLive(), false, channelId);
            if (liveId == null) {
                redisService.deletePushedInfoKey(pushedInfoKeys.getKey(), pushedInfoKeys.getLiveHashKey());
            } else {
                result.setLive(new String[]{liveId});
            }
        }
        return result;
    }

    private String[] getLiveOrVideoIds(String index, PushedInfoKeys pushedInfoKeys, InfoALV pushedALV, String userId, int size) {
        String loveTags = getLoveTags(userId);
        String[] pushedIds;
        String hashKey;
        if (index.equals(esService.getVideoIndex())) {
            pushedIds = pushedALV.getVideo();
            hashKey = pushedInfoKeys.getVideoHashKey();
        } else {
            pushedIds = pushedALV.getLive();
            hashKey = pushedInfoKeys.getLiveHashKey();
        }

        String[] ids = {};
        if (!loveTags.isEmpty()) {
            ids = esService.personalizedRecommend(index, loveTags, pushedIds, size);
        }
        if (ids.length < size) {
            String[] supplementsIds =
                    esService.commonRecommend(index, (String[]) ArrayUtils.addAll(ids, pushedIds), size - ids.length);
            ids = (String[]) ArrayUtils.addAll(ids, supplementsIds);
            if (ids.length < size) {
                redisService.deletePushedInfoKey(pushedInfoKeys.getKey(), hashKey);
            }
        }
        return ids;
    }

    private InfoALV getInfoALV(InfoALV pushedALV, PushedInfoKeys pushedInfoKeys, String userId, String channelId, int size) {
        String channelName;
        RcmdNewsInfo rcmdNewsInfo;
        String[] esTypes = null;
        int compSize = size - randomLiveOrVideoSize;

        if (channelId == null) {
            rcmdNewsInfo = redisService.getRcmdNews(userId, compSize);
            channelName = "推荐频道";
        } else {
            rcmdNewsInfo = redisService.getRcmdNewsByChannel(userId, channelId, compSize);
            channelName = "channelId:" + channelId;
            esTypes = dataetlJdbcService.channelEsTypeMap.get(channelId);
        }
        checkIfRequestRcmd(rcmdNewsInfo, userId);

        String[] articleIds = rcmdNewsInfo.getNews().toArray(new String[0]);

        if (articleIds.length < compSize) {
            articleIds = esService.commonRecommend(esService.getArticleIndex(), (String[]) ArrayUtils.addAll(articleIds, pushedALV.getArticle()), compSize, esTypes);
            logger.info("用户{}的{} Redis中数据不够，从ES中取数据", userId, channelName);
        } else {
            logger.info("从Redis的{}中为用户{} 取出{}条数据", channelName, userId, articleIds.length);
        }

        InfoALV infoALV = getVideoOrLiveIdFromMysql(pushedALV, pushedInfoKeys, channelId);
        infoALV.setArticle(articleIds);

        return infoALV;
    }

    public InfoALV channelRecommendNews(String channelType, String channelId, String categoryId, String userId, int size) {
        long beginTime = System.currentTimeMillis();
        PushedInfoKeys pushedInfoKeys = new PushedInfoKeys(userId, channelId, categoryId);
        InfoALV pushedALV = redisService.getPushedInfoALV(pushedInfoKeys);

        InfoALV result;

        if (channelTypeVideo.equalsIgnoreCase(channelType)) {//视频频道下所有
            logger.debug("视频频道下所有");
            result = new InfoALV();
            result.setVideo(getLiveOrVideoIds(esService.getVideoIndex(), pushedInfoKeys, pushedALV, userId, size));
        } else if (channelTypeLive.equalsIgnoreCase(channelType)) { //直播频道下所有
            logger.debug("直播频道下所有");
            result = new InfoALV();
            result.setLive(getLiveOrVideoIds(esService.getLiveIndex(), pushedInfoKeys, pushedALV, userId, size));
        } else if (channelTypeRCMD.equalsIgnoreCase(channelType)) { //推荐频道下所有
            logger.debug("推荐频道下所有");
            result = getInfoALV(pushedALV, pushedInfoKeys, userId, null, size);
        } else if (channelTypeArticle.equalsIgnoreCase(channelType) && allCategoryId.equals(categoryId)) {  //文章某个频道下所有
            logger.debug("文章某个频道下所有");
            //获取用户感兴趣的标签
            result = getInfoALV(pushedALV, pushedInfoKeys, userId, channelId, size);
        } else { //文章某个频道下所有某个类别

            result = getVideoOrLiveIdFromMysql(pushedALV, pushedInfoKeys);
            String esType = channelId + "_" + categoryId;
            result.setArticle(esService.commonRecommend(esService.getArticleIndex(), pushedALV.getArticle(), size - randomLiveOrVideoSize, esType));
        }

        if (result.size() < size - randomLiveOrVideoSize) {
            redisService.deletePushedInfoKeysAll(pushedInfoKeys); //生产环境一般不会进入这一步
        } else {
            redisService.setPushedInfoALV(pushedInfoKeys, pushedALV, result);
        }

        logger.info("/mul/ALV/user_channel?channelType={}&userId={}&channelId={}&categoryId={}  cost: {}ms", channelType, userId, channelId, categoryId, System.currentTimeMillis() - beginTime);

        return result;
    }

    private void checkIfRequestRcmd(RcmdNewsInfo rcmdNewsInfo, String userId) {
        if (sendRcmdMsg) {
            boolean canUpdateBasePref = true;
            //只有第一个才可以更新
            for (String channelId : rcmdNewsInfo.getChannelIdList()) {
                kafkaService.sendRcmdRequestMsg(new RcmdMsg(userId, channelId, 1, canUpdateBasePref));
                canUpdateBasePref = false;
            }
        }
    }
}
