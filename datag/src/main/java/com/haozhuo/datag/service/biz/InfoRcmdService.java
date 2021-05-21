package com.haozhuo.datag.service.biz;

import com.haozhuo.datag.model.*;
import com.haozhuo.datag.service.DataEtlJdbcService;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.KafkaService;
import com.haozhuo.datag.service.RedisService;
import com.sun.tools.javac.util.List;
import io.swagger.models.auth.In;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Lucius on 9/4/18.
 */
public class InfoRcmdService {
    private static final Logger logger = LoggerFactory.getLogger(InfoRcmdService.class);
    public static final String channelRcmdId = "10000";
    public static final String  dafaultCat = "200";
    public static final String  defaultChannel= "200";
    private final int randomLiveOrVideoSize = 1;

    private static final String channelTypeRCMD = "R";
    // --Commented out by Inspection (12/7/18 3:06 PM):
    private static final String channelTypeArticle = "A";
    private static final String channelTypeVideo = "V";
    private static final String channelTypeLive = "L";
    private static final String originalType = "Y";
    public static final String baseLabels ="高血压,肥胖,血液,肾功能不全,脂肪过多";

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

    public InfoArticle UseridRecommendNews(String userId, int size,String categoryId,String channelId){
        String[] inits = new String[]{"1"};
        PushedInfoKeys pushedInfoKeys = new PushedInfoKeys(userId, channelId, categoryId);
        String labels = redisService.getPushedLablefoArticle(pushedInfoKeys);
        System.out.println(labels);
        if(labels.length()<2){
           String  eslabels = esService.getLabelsByUserId(userId);
           if (eslabels.length()<2){
                labels = baseLabels;
               redisService.putPushedLabelInfoArticle(pushedInfoKeys,labels);
            }else{
               redisService.putPushedLabelInfoArticle(pushedInfoKeys,eslabels);
               labels = eslabels;
            }
        }
        String [] articleIds1 = new String[]{};
        String  pushs= redisService.getPushedInfoArticle(pushedInfoKeys);
        String[] infoId = pushs.split(",");
        //Set<String> pushIds =new HashSet(Arrays.asList(pushs.split(",")));
        if(infoId.length>0){
            articleIds1 = esService.getArticleIds(labels, (String[]) infoId, size);
            //infoId = pushIds.toArray(new String[0]);
            if(infoId.length>50){
                redisService.putPushedInfoArticle(pushedInfoKeys,inits);
                pushs= redisService.getPushedInfoArticle(pushedInfoKeys);
                infoId = pushs.split(",");
                //redisService.up(pushedInfoKeys.getKey(),PushedInfoKeys.getChannelRcmdHashKeyForArticle());
            }
        }else {
            articleIds1 = esService.getArticleIds(labels, (String[]) infoId, size);
        }

        InfoArticle infoArticle =  new InfoArticle();

       // Set upSet = updateRedisPush(, articleIds1);

        redisService.putPushedInfoArticle(pushedInfoKeys,merageStrings(articleIds1,infoId));

        infoArticle.setArticle(articleIds1);

        if(articleIds1.length<size){
            String tags ="";
            if(articleIds1.length==0){
                tags = baseLabels;
            }else {
                tags = dataetlJdbcService.getTagsKeywordsByInfoId(articleIds1[0]);
            }
            String[] articleIds2 = esService.getArticleIds(tags, articleIds1, size-articleIds1.length);
            String[] articleIds3 = merageStrings(articleIds1,articleIds2);
            infoArticle.setArticle(articleIds3);
            if(articleIds3.length<size){
              String[] articleIds4 =  esService.commonRecommend(esService.getArticleIndex(),articleIds3,size-articleIds3.length,"200_200");
              String[] articleIds5 = merageStrings(articleIds3,articleIds4);
              //Set upSet2 = updateRedisPush(upSet,articleIds5);
              redisService.putPushedInfoArticle(pushedInfoKeys,articleIds5);
              infoArticle.setArticle(articleIds5);
            }

        }

        return infoArticle;
    }

    public Set updateRedisPush(Set set,String[] strs){
        Set set1 = new HashSet();
        set1.addAll(set);
        set1.addAll(new HashSet<>(Arrays.asList(strs)));
        return set;
    }
    public String[] merageStrings(String[] strs1,String[] strs2){
        String s1 = StringUtils.join(strs1, ",");
        String s3 = "";
        String s2 = StringUtils.join(strs2, ",");
        if(s1.equals("")){
            s3=s2;
        }else if(s2.equals("")){
            s3 =s1;
        }else {
            s3 = s1+","+s2;
        }
        return  s3.split(",");
    }


    public InfoALV channelRecommendNews(String channelType, String channelId, String categoryId, String userId, int size) {
        long beginTime = System.currentTimeMillis();
        //PushedInfo:ed8e75a2-0869-453d-ace9-f5978ec51e68  key: 0_0_a v:116388,118050,260274,260273,260272,260271,260270,260269,260268
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
        } else {
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
