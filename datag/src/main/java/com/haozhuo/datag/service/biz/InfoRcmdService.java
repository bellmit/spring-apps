package com.haozhuo.datag.service.biz;

import com.haozhuo.datag.common.Tuple;
import com.haozhuo.datag.model.InfoALV;
import com.haozhuo.datag.model.PushedInfoKeys;
import com.haozhuo.datag.service.DataetlJdbcService;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.RedisService;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Lucius on 9/4/18.
 */
@Component
public class InfoRcmdService {
    private static final Logger logger = LoggerFactory.getLogger(InfoRcmdService.class);
    public static final String channelRcmdId = "10000";
    public static final String channelVideoId = "20000";
    public static final String channelLiveId = "30000";
    public static final String allCategoryId = "0";
    private final EsService esService;

    private final RedisService redisService;

    private final DataetlJdbcService dataetlJdbcService;

    @Autowired
    public InfoRcmdService(EsService esService, RedisService redisService, DataetlJdbcService dataetlJdbcService) {
        this.esService = esService;
        this.redisService = redisService;
        this.dataetlJdbcService = dataetlJdbcService;
    }

    /**
     * 目前资讯的混推中只有推荐频道下才推直播
     *
     * @return
     */
    private String[] getLiveIdsForInfo(String[] pushedLives, String channelId, String hateTags) {
        if (channelRcmdId.equals(channelId)) { //如果是推荐，推一篇直播
            String[] result = esService.commonRecommend(esService.getLiveIndex(), null, hateTags, pushedLives, 1);
            return result;
        } else {
            return new String[]{};
        }
    }

    public Tuple<String, String> getTagTuple(String userId, String channelId, String categoryId) {
        if (allCategoryId.equals(categoryId) || channelLiveId.equals(channelId)) {
            //获取用户感兴趣的标签
            String loveTags = redisService.getLoveTags(userId);
            logger.debug("userId:{}, loveTags:{}", userId, loveTags);
            //获取用户报告标签
            String reportTags = dataetlJdbcService.getLabelStrByUserId(userId);
            logger.debug("userId:{}, reportTags:{}", userId, reportTags);
            return new Tuple<>(loveTags, reportTags);
        } else {
            return new Tuple<>("", "");
        }
    }

    /**
     * curl -XGET "192.168.1.152:9200/article4/_search?pretty" -d '{"size":10,"query":{"function_score":{"query":{"bool":{"should":[{"multi_match":{"query":"风湿关节炎食疗方剂","fields":["title","tags"],"boost":3}},{"multi_match":{"query":"肺炎近视","fields":["title","tags"],"boost":1}}],"must_not":[{"match":{"tags":"近视"}},{"ids":{"values":["131025","131574","131808"]}}]}},"functions":[{"gauss":{"create_time":{"origin":"now","scale":"30d","offset":"15d","decay":"0.8"}}}]}}}'
     */
    public InfoALV channelRecommend(String channelId, String categoryId, String userId, int size) {
        long beginTime = System.currentTimeMillis();
        PushedInfoKeys pushedInfoKeys = new PushedInfoKeys(userId, channelId, categoryId);
        InfoALV pushedALV = redisService.getPushedInfoALV(pushedInfoKeys);
        InfoALV result = new InfoALV();

        //获取用户不感兴趣的标签
        String hateTags = redisService.getHateTags(userId);
        String[] videoIds = new String[]{};
        String[] articleIds = new String[]{};
        String[] liveIds = new String[]{};
        Tuple<String, String> tagTuple = getTagTuple(userId, channelId, categoryId);
        //获取用户感兴趣的标签
        String loveTags = tagTuple.getT1();
        //获取用户报告标签
        String reportTags = tagTuple.getT2();

        logger.debug("userId:{}, hateTags:{}", userId, hateTags);

        if (channelVideoId.equals(channelId) && allCategoryId.equals(categoryId)) {//视频频道下所有
            videoIds = esService.personalizedRecommend(esService.getVideoIndex(), null, loveTags, reportTags, hateTags, pushedALV.getVideo(), size);
            if (videoIds.length < size) {
                String[] supplementsVideoIds = esService.commonRecommend(esService.getVideoIndex(), null, hateTags, (String[]) ArrayUtils.addAll(videoIds, pushedALV.getVideo()), size - videoIds.length);
                videoIds = (String[]) ArrayUtils.addAll(videoIds, supplementsVideoIds);
            }
        } else if (channelVideoId.equals(channelId) && !allCategoryId.equals(categoryId)) {//视频频道下某个分类
            videoIds = esService.commonRecommend(esService.getVideoIndex(), new String[]{categoryId}, hateTags, pushedALV.getVideo(), size);
        } else if (channelLiveId.equals(channelId)) { //直播频道下所有
            liveIds = esService.personalizedRecommend(esService.getLiveIndex(), null, loveTags, reportTags, hateTags, pushedALV.getLive(), size);
            if (liveIds.length < size) {
                String[] supplementsLiveIds = esService.commonRecommend(esService.getLiveIndex(), null, hateTags, (String[]) ArrayUtils.addAll(liveIds, pushedALV.getLive()), size - liveIds.length);
                liveIds = (String[]) ArrayUtils.addAll(liveIds, supplementsLiveIds);
            }
        } else if (allCategoryId.equals(categoryId)) {  //推荐频道 || 资讯某个频道下所有
            //获取用户感兴趣的标签
            String[] esTypes = dataetlJdbcService.channelMap.get(channelId); //推荐 找不到channelId -> null -> 查找所有
            videoIds = esService.commonRecommend(esService.getVideoIndex(), esTypes, hateTags, pushedALV.getVideo(), 2);
            liveIds = getLiveIdsForInfo(pushedALV.getLive(), channelId, hateTags);
            articleIds = esService.personalizedRecommend(esService.getArticleIndex(), esTypes, loveTags, reportTags, hateTags, pushedALV.getArticle(), size - videoIds.length - liveIds.length);
            int nowSize = articleIds.length + videoIds.length + liveIds.length;
            if (nowSize < size) { //articleIdsByTags可能数量会比较少，所以随机补充文章
                String[] supplementsArticleIds = esService.commonRecommend(esService.getArticleIndex(), esTypes, hateTags, (String[]) ArrayUtils.addAll(articleIds, pushedALV.getArticle()), size - nowSize);
                articleIds = (String[]) ArrayUtils.addAll(articleIds, supplementsArticleIds);
            }
        } else { //资讯某个频道下的某个类别
            String[] esTypes = new String[]{categoryId};
            videoIds = esService.commonRecommend(esService.getVideoIndex(), esTypes, hateTags, pushedALV.getVideo(), 2);
            articleIds = esService.commonRecommend(esService.getArticleIndex(), esTypes, hateTags, pushedALV.getArticle(), size - videoIds.length);
        }
        result.setArticle(articleIds);
        result.setVideo(videoIds);
        result.setLive(liveIds);

        if (result.size() < size) { //表示已经推完了，下一次重新推
            redisService.deleteHashKeyByPushedInfoKeys(pushedInfoKeys);
        } else {
            redisService.setPushedInfoALV(pushedInfoKeys, pushedALV, result);
        }

        logger.info("/mul/ALV/user_channel?userId={}&channelId={}&categoryId={}  cost: {}ms", userId, channelId, categoryId, System.currentTimeMillis() - beginTime);

        return result;
    }

    public InfoALV channelRecommend(String channelId, String categoryId, int pageNo, int size) {
        long beginTime = System.currentTimeMillis();
        String[] videoIds;
        String[] liveIds = new String[]{};
        String[] articleIds = new String[]{};
        InfoALV result = new InfoALV();
        if (channelVideoId.equals(channelId) && allCategoryId.equals(categoryId)) {//视频频道下所有
            logger.debug("视频频道下所有");
            videoIds = esService.heatRecommend(esService.getHeatVideoIndex(), pageNo, size);
        } else if (channelVideoId.equals(channelId) && !allCategoryId.equals(categoryId)) {//视频频道下某个分类
            logger.debug("视频频道下某个分类");
            videoIds = esService.heatRecommend(esService.getHeatVideoIndex(), pageNo, size, categoryId);
        } else if (channelRcmdId.equals(channelId)) { //推荐频道
            logger.debug("推荐频道");
            videoIds = esService.heatRecommend(esService.getHeatVideoIndex(), pageNo, 2);
            liveIds = esService.heatRecommend(esService.getLiveIndex(), pageNo, 1);
            articleIds = esService.heatRecommend(esService.getHeatArticleIndex(), pageNo, size - videoIds.length - liveIds.length);
        } else if (allCategoryId.equals(categoryId)) { //其他频道下所有
            logger.debug("其他频道下所有");
            String[] categories = dataetlJdbcService.channelMap.get(channelId);
            videoIds = esService.heatRecommend(esService.getHeatVideoIndex(), pageNo, 2, categories);
            articleIds = esService.heatRecommend(esService.getHeatArticleIndex(), pageNo, size - videoIds.length, categories);
            if (articleIds.length == 0) { //如果文章都到底了，那么就不推视频了
                videoIds = new String[]{};
            }
        } else { //其他频道下某个分类
            logger.debug("其他频道下某个分类");
            videoIds = esService.heatRecommend(esService.getHeatVideoIndex(), pageNo, 2, categoryId);
            articleIds = esService.heatRecommend(esService.getHeatArticleIndex(), pageNo, size - videoIds.length, categoryId);
            if (articleIds.length == 0) {
                videoIds = new String[]{};
            }
        }
        result.setArticle(articleIds);
        result.setVideo(videoIds);
        result.setLive(liveIds);
        logger.info("/mul/ALV/channel?channelId={}&categoryId={}&pageNo={}  cost: {}ms", channelId, categoryId, pageNo, System.currentTimeMillis() - beginTime);
        return result;
    }

}
