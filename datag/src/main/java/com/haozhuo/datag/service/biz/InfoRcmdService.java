package com.haozhuo.datag.service.biz;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.model.RcmdInfo;
import com.haozhuo.datag.service.DataetlJdbcService;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.RedisService;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by Lucius on 9/4/18.
 */
@Component
public class InfoRcmdService {
    private static final Logger logger = LoggerFactory.getLogger(InfoRcmdService.class);

    private final EsService esService;

    private final RedisService redisService;

    private final DataetlJdbcService dataetlJdbcService;

    @Autowired
    public InfoRcmdService(EsService esService, RedisService redisService, DataetlJdbcService dataetlJdbcService) {
        this.esService = esService;
        this.redisService = redisService;
        this.dataetlJdbcService = dataetlJdbcService;
    }

    //TODO
    private RcmdInfo rcmdVideos(String channelId, String categoryId, String userId, int pageSize) {
        return new RcmdInfo();
    }

    //TODO
    private RcmdInfo rcmdLive(String channelId, String categoryId, String userId, int pageSize) {
        return new RcmdInfo();
    }

    /*
curl -XGET "192.168.1.152:9200/article3/_search?pretty" -d '{
	"size": 10,
	"query": {
		"function_score": {
			"query": {
				"bool": {
					"should": [{
						"multi_match": {
							"query": "风湿关节炎食疗方剂",  ####################### loveTags #########################
							"fields": ["title", "keywords"],
							"boost": 3
						}
					}, {
						"multi_match": {
							"query": "肺炎近视",   ####################### reportTags #########################
							"fields": ["title", "keywords"],
							"boost": 1
						}
					}],
					"must_not": [{
						"match": {
							"keywords": "近视"  ######################## hateTags ##################
						}
					}, {
						"ids": {
							"values": [
								"131025", "131574", "131808"   ######################## pushedInfoAs ##################
							]
						}
					}]
				}
			},
			"functions": [{
				"gauss": {
					"create_time": {
						"origin": "now",
						"scale": "30d",
						"decay": "0.8"
					}
				},
				"weight": 20
			}]
		}
	}
}'

    */

    /**
     * 目前只有推荐频道下才推直播
     *
     * @param channelId
     * @param categoryId
     * @param userId
     * @param hateTags
     * @return
     */
    private String[] getLiveIdsForInfo(String channelId, String categoryId, String userId, String hateTags) {
        if ("10000".equals(channelId)) { //如果是推荐，推一篇直播
            logger.debug("getLiveIdsForInfo userId:{}", userId);
            String liveHashKey = String.format("l_%s_%s", channelId, categoryId);
            String[] pushedLiveIdsForInfo = redisService.getPushedInfo(userId, liveHashKey, redisService.getPushedKeyMaxSize() / 5);
            String[] result = esService.commonRecommend(esService.getLiveIndex(), null, hateTags, pushedLiveIdsForInfo, 1);
            redisService.setPushedInfo(userId, liveHashKey, pushedLiveIdsForInfo, result);
            return result;
        } else {
            return new String[]{};
        }
    }

    private RcmdInfo rcmdArticles(String channelId, String categoryId, String userId, int size) {
        //long beginTime = System.currentTimeMillis();
        RcmdInfo rcmdInfo = new RcmdInfo();

        //如果有userId，那么从推荐中取
        String articleHashKey = String.format("a_%s_%s", channelId, categoryId);
        String videoHashKey = String.format("v_%s_%s", channelId, categoryId);
        String[] pushedArticleIdsForInfo = redisService.getPushedInfo(userId, articleHashKey, redisService.getPushedKeyMaxSize());
        String[] pushedVideoIdsForInfo = redisService.getPushedInfo(userId, videoHashKey, redisService.getPushedKeyMaxSize() / 5); //混推中 视频数:文章数=1:5

        //获取用户不感兴趣的标签
        String hateTags = redisService.getHateTags(userId);
        String[] videoIds;
        String[] articleIds;
        logger.debug("userId:{}, hateTags:{}", userId, hateTags);
        System.out.println(hateTags);
        if ("0".equals(categoryId)) {  //推荐频道 || 某个频道下所有
            //获取用户感兴趣的标签
            String loveTags = redisService.getLoveTags(userId);
            logger.debug("userId:{}, loveTags:{}", userId, loveTags);

            //获取用户报告标签
            String reportTags = dataetlJdbcService.getLabelStrByUserId(userId);
            logger.debug("userId:{}, reportTags:{}", userId, reportTags);
            String[] esTypes = dataetlJdbcService.channelMap.get(channelId); //推荐 找不到channelId -> null -> 查找所有

            videoIds = esService.commonRecommend(esService.getVideoIndex(), esTypes, hateTags, pushedVideoIdsForInfo, 2);
            String[] liveIds = getLiveIdsForInfo(channelId, categoryId, userId, hateTags);

            articleIds = esService.personalizedRecommend(
                    esService.getArticleIndex(), esTypes, loveTags, reportTags,
                    hateTags, pushedArticleIdsForInfo, size - videoIds.length - liveIds.length);

            int nowSize = articleIds.length + videoIds.length + liveIds.length;
            if (nowSize < size) { //articleIdsByTags可能数量会比较少，所以随机补充文章
                //补充
                String[] supplementsArticleIds = esService.commonRecommend(esService.getArticleIndex(), esTypes, hateTags, (String[]) ArrayUtils.addAll(articleIds, pushedArticleIdsForInfo), size - nowSize);
                articleIds = (String[]) ArrayUtils.addAll(articleIds, supplementsArticleIds);
            }
            rcmdInfo.addLives(liveIds);
        } else { //频道下的某个类别
            String[] esTypes = new String[]{categoryId};
            videoIds = esService.commonRecommend(esService.getVideoIndex(), esTypes, hateTags, pushedVideoIdsForInfo, 2);
            articleIds = esService.commonRecommend(esService.getArticleIndex(), esTypes, hateTags, pushedArticleIdsForInfo, size - videoIds.length);
        }
        redisService.setPushedInfo(userId, articleHashKey, pushedArticleIdsForInfo, articleIds);
        redisService.setPushedInfo(userId, videoHashKey, pushedVideoIdsForInfo, videoIds);
        rcmdInfo.addArticles(articleIds);
        rcmdInfo.addVideos(videoIds);
        return rcmdInfo;
    }

    public Object process(String channelId, String categoryId, String userId, int pageSize) {
        if ("20000".equals(channelId)) {//视频推荐
            return rcmdVideos(channelId, categoryId, userId, pageSize);
        } else if ("30000".equals(channelId)) { //直播推荐
            return rcmdLive(channelId, categoryId, userId, pageSize);
        } else { //文章推荐
            return rcmdArticles(channelId, categoryId, userId, pageSize);
        }
    }
}
