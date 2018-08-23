package com.haozhuo.rcmd.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haozhuo.rcmd.service.KafkaService;
import com.haozhuo.rcmd.model.*;
import com.haozhuo.rcmd.service.EsService;
import com.haozhuo.rcmd.service.JdbcService;
import com.haozhuo.rcmd.service.RedisService;
import io.swagger.annotations.ApiOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * Created by Lucius on 8/16/18.
 */

@RequestMapping(value = "/rcmd")
@RestController
public class RcmdController {
    private static final Logger logger = LoggerFactory.getLogger(RcmdController.class);

    @Autowired
    private EsService esService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private JdbcService jdbcService;

    private ObjectMapper mapper = new ObjectMapper();


    /**
     * 输入userId,返回推荐商品的id
     * 对应原来good-recommender项目中的GoodsRecomController中的getMatchContent()方法。
     */
    @GetMapping("/goods/userId/{userId}")
    @ApiOperation(value = "输入userId,返回推荐商品的id  【/goodsmatch/list/all】", notes = "输入userId,返回推荐商品的id。\n原接口: http://192.168.1.152:8087/swagger-ui.html#!/goods-recom-controller/getMatchContentUsingPOST")
    public Object getGoodsIdsByUserId(
            @PathVariable(value = "userId") String userId,
            @RequestParam(value = "size", defaultValue = "10") int pageSize) {
        long beginTime = System.currentTimeMillis();
        String[] alreadyPushedGoods = redisService.getPushedGoods(userId);
        //从mysql查出userId的label
        String userLabels = jdbcService.getLabelStrByUserId(userId);

        //根据userId的label匹配es中good索引中的label，返回内容。
        String[] result = esService.getGoodsIdsByLabels(userLabels, alreadyPushedGoods, pageSize);

        //如果返回的数量小于pageSize，删除Redis中推过的商品列表的key
        redisService.setPushedGoods(userId, result);
        //如果返回的数据小于pageSize,则认为所有的视频都被推荐了，那么将Redis中推过的视频列表的key删除，使得所有视频可以重新推送
        if (result.length < pageSize) {
            redisService.deleteGoodsPushedKey(userId);
        }
        logger.info("/goods/userId/{}?size={}  cost: {}ms", userId, pageSize, System.currentTimeMillis() - beginTime);
        return result;
    }

    /**
     * 根据
     * 对应原来good-recommender项目中的GoodsRecomController中的getRecomList()方法。
     * curl -X POST --header "ArticleInfo-Type: application/json" --header  "http://192.168.1.152:8087/goodsmatch/getRecom/list?healthReportId=2515473&pageSize=10&pageNum=1"
     *
     * @return
     */
    @GetMapping("/goods/reportId/{reportId}")
    @ApiOperation(value = "根据报告Id返回推荐的商品  【/goodsmatch/getRecom/list】", notes = "根据报告Id返回推荐的商品。\n原接口: http://192.168.1.152:8087/swagger-ui.html#!/goods-recom-controller/getRecomListUsingPOST")
    public Object getGoodsIdsByReportId(
            @PathVariable(value = "reportId") String reportId,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum) {
        long beginTime = System.currentTimeMillis();
        String labels = esService.getLabelsByReportId(reportId);
        if ("".equals(labels))
            return null;
        int from = (pageNum - 1) * pageSize;
        String[] result = esService.getGoodsIdsByLabels(labels, from, pageSize);
        logger.info("/goods/reportId/{}?pageSize={}&pageNum={}  cost: {}ms", reportId, pageSize, pageNum, System.currentTimeMillis() - beginTime);
        return result;
    }

    /**
     * 推荐视频列表
     * 旧接口：
     * video-recommender项目中的VideoRecomController中的getMatchContent()方法
     * 生产环境输入：
     * curl -XPOST -H "ArticleInfo-Type: application/json" http://datanode2:9090/api/video-recommder/videomatch/recommend/all -d '{
     * "userId":"c63fc45c-35d1-43d5-b864-2bdb82542dfd",
     * "size":10
     * }'
     * 返回：
     * ["575","591","588","339","311","303","309","317","324","331"]
     * 新接口：
     * 注意：旧接口中有一个flag参数，并没有用到，所以新接口中删除
     *
     * @param userId
     * @param size
     * @return
     */
    @GetMapping("/video/userId/{userId}")
    @ApiOperation(value = "根据userId,匹配推荐的视频列表  【/videomatch/recommend/all】", notes = "输入对应的userId,返回推荐视频列表。 \n原接口: http://192.168.1.152:8089/swagger-ui.html#!/video-recom-controller/getMatchContentUsingPOST")
    public Object getVideoListByUserId(
            @PathVariable(value = "userId") String userId,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        long beginTime = System.currentTimeMillis();
        String[] alreadyPushedVideos = redisService.getPushedVideos(userId);
        logger.debug("alreadyPushedVideos:{}", StringUtils.arrayToCommaDelimitedString(alreadyPushedVideos));
        String userLabels = jdbcService.getLabelStrByUserId(userId);

        //最后需要把查到的结果存入Redis的已推荐的key中
        String[] result = esService.getVideoIdsByLabel(userLabels, alreadyPushedVideos, size);
        redisService.setPushedVideos(userId, result);
        //如果返回的数据小于pageSize,则认为所有的视频都被推荐了，那么将Redis中推过的视频列表的key删除，使得所有视频可以重新推送
        if (result.length < size) {
            redisService.deleteVideoPushedKey(userId);
        }
        logger.info("/video/userId/{}?size={}  cost: {}ms", userId, size, System.currentTimeMillis() - beginTime);
        return result;
    }


    /**
     * 搜索视频列表
     * 旧接口：
     * video-recommender项目中的VideoRecomController中的getSearchContent()方法
     * 请求是POST：
     * curl -X POST --header "ArticleInfo-Type: application/json"  "http://datanode2:9090/api/video-recommder/videomatch/search/all?keyword=%E9%AB%98%E8%A1%80%E5%8E%8B"
     * <p>
     * 新接口：
     */
    @GetMapping("/video/keyword/{keyword}")
    @ApiOperation(value = "根据关键词返回视频列表  【/videomatch/search/all】", notes = "根据输入的关键词，和视频的标题进行匹配，得到相应的视频id列表。\n原接口: http://192.168.1.152:8089/swagger-ui.html#!/video-recom-controller/getSearchContentUsingPOST")
    public Object getVideoListByKeyword(
            @PathVariable(value = "keyword") String keyword,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        long beginTime = System.currentTimeMillis();
        String[] result = esService.getVideoIdsByTitle(keyword, size);
        logger.info("/video/keyword/{}?size={}  cost: {}ms", keyword, size, System.currentTimeMillis() - beginTime);
        return result;
    }


    /**
     * 旧的接口：
     * curl -X POST --header "ArticleInfo-Type: application/json"  "http://datanode2:9090/api/video-recommder/videomatch/relative/all?vid=570"
     */
    @GetMapping("/video/videoId/{videoId}")
    @ApiOperation(value = "根据videoId返回与它相似的视频列表  【/videomatch/relative/all】", notes = "根据videoId返回与它相似的视频列表。\n原接口: http://192.168.1.152:8089/swagger-ui.html#!/video-recom-controller/getRelativeContentUsingPOST")
    public Object getVideoListBySimilarity(
            @PathVariable(value = "videoId") String videoId,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        long beginTime = System.currentTimeMillis();
        String videoLabels = jdbcService.getVideoLabelsById(videoId);
        String[] result = esService.getSimilarVideoIds(videoId, videoLabels, size);
        logger.info("/video/videoId/{}?size={}  cost: {}ms", videoId, size, System.currentTimeMillis() - beginTime);
        return result;
    }


    /**
     * 根据userId获取用户的标签
     * 旧的接口，
     * 对应developerApi中GetUserLabelController中的getMatchContent()方法
     * 测试环境：http://47.98.165.120:8085/get?userId=00007d91-fefe-4234-bc08-2496ea8360c6
     */
    @ApiOperation(value = "根据userId获取用户的标签  【/get】", notes = "原接口: http://192.168.1.152:8085/swagger-ui.html#!/get-user-label-controller/getMatchContentUsingGET")
    @GetMapping(value = "/labels/userId/{userId}")
    public String getLablesByUserId(@PathVariable(value = "userId") String userId) {
        long beginTime = System.currentTimeMillis();
        String result = jdbcService.getLabelStrByUserId(userId);
        logger.info("/labels/userId/{}  cost: {}ms", userId, System.currentTimeMillis() - beginTime);
        return result;
    }

    /**
     * 根据healthReportId返回报告的标签
     * 旧的接口，
     * 对应developerApi中GetUserLabelController中的getBasicInfoByReport()方法
     * 测试环境：http://192.168.1.152:8085/getByReport?healthReportId=1
     */
    @ApiOperation(value = "根据healthReportId返回报告的标签  【/getByReport】", notes = "原接口: http://192.168.1.152:8085/swagger-ui.html#!/get-user-label-controller/getBasicInfoByReportUsingGET")
    @GetMapping(value = "/labels/reportId/{reportId}")
    public String getLablesByReportId(@PathVariable(value = "reportId") String reportId) {
        long beginTime = System.currentTimeMillis();
        String result = esService.getLabelsByReportId(reportId);
        logger.info("/labels/reportId/{}  cost: {}ms", reportId, System.currentTimeMillis() - beginTime);
        return result;
    }


    /**
     * 旧的接口：
     * 对应developer-api项目中的GetSimiIdController的getSimiIdNew()方法
     * /getSimi/all?informationId=11
     * <p>
     * 生产环境:
     * http://47.98.165.120:8085/getSimi/all?informationId=123273
     * 测试环境:
     * http://192.168.1.152:8085/getSimi/all?informationId=56
     * <p>
     * 新接口：
     * 除了资讯、视频、直播外，还新增返回相似商品
     * 根据infoId从获取article的labels，根据这个labels从good索引的label，content_name，display_label
     * 中匹配出相应的商品
     */
    @ApiOperation(value = "根据infoId获取相似的资讯、视频、直播、商品  【/getSimi/all】", notes = "输入infoId,返回相似的资讯、视频、直播、商品。\n原接口: http://192.168.1.152:8085/getSimi/all?informationId=56")
    @RequestMapping(value = "/mul/ALVG/infoId/{infoId}", method = RequestMethod.GET)
    public Object getMulAlvgByInfoId(@PathVariable(value = "infoId") String infoId) {
        long beginTime = System.currentTimeMillis();
        //资讯、视频、直播的结果
        Map<String, List<String>> map = redisService.getSimByInfoId(infoId);
        //商品的结果
        String labels = jdbcService.getLabelNameByInfoId(infoId);
        String[] goodsIds = esService.getGoodsIdsByLabels(labels, 0, 10);
        map.put("g", Arrays.asList(goodsIds));
        logger.info("/mul/ALVG/infoId/{}  cost: {}ms", infoId, System.currentTimeMillis() - beginTime);
        return map;
    }

    /**
     * 旧的接口：
     * 对应
     * abnormal-recommder的ArticleRecomController中的getRecomByAbnormialAll()方法
     * http://47.98.165.120:8002/getRecom/abnormial/all?userId=c63fc45c-35d1-43d5-b864-2bdb82542dfd&pageSize=10&jsonStr=%7B%22exceptionItemName%22%3A%22%E8%BF%91%E8%A7%86%22%2C%22exceptionItemAlias%22%3A%20%22%E8%82%A5%E8%83%96%22%2C%22possibleDiseases%22%3A%20%22%E9%AB%98%E8%A1%80%E5%8E%8B%22%2C%22possibleDiseaseAlias%22%3A%20%22%E8%BF%91%E8%A7%86%22%2C%22possibleSymptoms%22%3A%22%E8%BF%91%E8%A7%86%22%2C%22possibleSymptomAlias%22%3A%20%22%E8%BF%91%E8%A7%86%22%20%7D
     * jsonStr的格式如下：
     * {"exceptionItemName":"近视","exceptionItemAlias": "肥胖","possibleDiseases": "高血压","possibleDiseaseAlias": "近视","possibleSymptoms":"近视","possibleSymptomAlias": "近视" }
     * 去除旧接口中的userId请求参数,
     *
     * @return
     */
    @ApiOperation(value = "根据异常项信息(jsonStr)获取相似的资讯、视频、直播  【/getRecom/abnormial/all】",
            notes = "根据异常项信息(jsonStr)获取相似的资讯、视频、直播。jsonStr格式如下：\n" +
                    " {\"exceptionItemName\":\"近视\",\"exceptionItemAlias\": \"肥胖\"," +
                    " \"possibleDiseases\": \"高血压\",\"possibleDiseaseAlias\": \"近视\"," +
                    " \"possibleSymptoms\":\"近视\",\"possibleSymptomAlias\": \"近视\"} 。" +
                    "\n原接口: http://47.98.165.120:8002/swagger-ui.html#!/article-recom-controller/getRecomByAbnormialAllUsingPOST")
    @GetMapping(value = "/mul/ALV/abnorm")
    public Object getMulAlvByAbnorm(
            @RequestParam(value = "pageSize", defaultValue = "20") int size,
            @RequestParam(value = "jsonStr") String jsonStr) {
        long beginTime = System.currentTimeMillis();
        AbnormalParam abnormal = null;
        try {
            abnormal = mapper.readValue(jsonStr, AbnormalParam.class);
        } catch (IOException e) {
            logger.error("getMulAlvByAbnorm parse Json error:{}", e);
            return null;
        }
        List<String> livesIds = esService.getLiveIdsByAbnorm(abnormal, 1);
        List<String> videoIds = esService.getVideoIdsByAbnorm(abnormal, 3 - livesIds.size());
        List<String> articleIds = esService.getLiveIdsByAbnorm(abnormal, size - videoIds.size() - livesIds.size());
        List<String> result = new ArrayList<>();
        result.addAll(addTypeForIds(videoIds, "video"));
        result.addAll(addTypeForIds(livesIds, "live"));
        result.addAll(addTypeForIds(articleIds, "article"));
        logger.info("/mul/ALV/abnorm?jsonStr={}  cost: {}ms", jsonStr, System.currentTimeMillis() - beginTime);
        return result;
    }

    /**
     * 根据userId获取资讯、视频、直播的推荐列表
     * 旧接口：
     * recommender项目中的EsMatcherController中的enter(),getMatchContent()方法。这两个方法合并成一个。
     *
     * @return
     */
    @GetMapping("/mul/ALV/userId/{userId}")
    @ApiOperation(value = "根据userId获取资讯、视频、直播的推荐列表  【/list/current/all,/list/current/scroll/all,...】", notes = "原接口: http://192.168.1.152:8078/swagger-ui.html#/\n es-matcher-controller中的四个方法合并成一个")
    public Object getInfoList(
            @PathVariable(value = "userId") String userId,
            @RequestParam(value = "size", defaultValue = "10") int pageSize,
            @RequestParam(value = "contentType", defaultValue = "推荐") String categoryName) {
        long beginTime = System.currentTimeMillis();
        int rmcdType = jdbcService.categoryNameIdMap.getOrDefault(categoryName, 1);
        kafkaService.sendRcmdRequestMsg(new RcmdRequestMsg(userId, rmcdType));
        //目前size不管传入多少，返回都是10条。因为flink-data-etl的推荐中固定每次产生10条
        RcmdInfo rcmdInfo = redisService.getRcmdInfo(userId, rmcdType);
        int compSize = pageSize - rcmdInfo.size();
        //如果上述推荐的结果小于默认的推荐结果，则进行补充
        if (compSize > 0) {
            rcmdInfo.add(jdbcService.getRandomInfos(rmcdType, pageSize, compSize));
        }
        logger.debug("getInfoList: cost {} ms", (System.currentTimeMillis() - beginTime) / 1000);

        logger.info("/mul/ALV/userId/{}?contentType={}  cost: {}ms", userId, categoryName, System.currentTimeMillis() - beginTime);
        return rcmdInfo;
    }

    /**
     * 旧的接口：
     * abnormal-recommder的ArticleRecomController中的getRecomByVideoALL()方法
     * curl -X POST --header "Content-Type: application/json" --header  "http://47.98.165.120:8002/getRecom/videoTags?userId=c63fc45c-35d1-43d5-b864-2bdb82542dfd&pageSize=10&jsonStr=%7B%22labels%22%3A%22%E8%82%BE%E5%8A%9F%E8%83%BD%E9%9A%9C%E7%A2%8D%22%2C%22keywords%22%3A%20%22%E8%82%BE%E7%97%85%2C%E8%82%BE%E5%8A%9F%E8%83%BD%2C%E8%A1%80%E5%8E%8B%2C%E8%82%BE%E8%A1%B0%22%2C%22title%22%3A%22%E5%89%8D%E5%88%97%E8%85%BA%E9%92%99%E5%8C%96%E6%98%AF%E6%80%8E%E4%B9%88%E5%9B%9E%E4%BA%8B-%E6%9D%8E%E6%B5%B7%E6%9D%BE%22%7D"
     * jsonStr的格式: {"labels":"肾功能障碍","keywords": "肾病,肾功能,血压,肾衰","title":"前列腺钙化是怎么回事-李海松"}
     * TODO 注意：新的接口和旧的接口对jsonStr进行了变更
     * 原来的
     * jsonStr : {"labels":"肾功能障碍", "keywords": "肾病,肾功能,血压,肾衰", "title":"前列腺钙化是怎么回事-李海松"}
     * 变更成请求参数，用excludeVideoId替换上面的title。
     * 去除旧接口中的userId请求参数,
     *
     * @return
     * @RequestParam(value = "labels") String labels,
     * @RequestParam(value = "keywords") String keywords,
     * @RequestParam(value = "excludeVideoId") int excludeIds
     * <p>
     * 匹配labels和keywords,但是过滤掉excludeInfoId的视频
     * 新的接口如下：
     */
    @ApiOperation(value = "根据传过来的video的标签关键词信息获取相似的资讯、视频、直播  【/getRecom/videoTags】", notes = "原接口: http://192.168.1.152:8002/swagger-ui.html#!/article-recom-controller/getRecomByVideoUsingPOST")
    @GetMapping(value = "/mul/ALV/videoInfo")
    public Object getMulAlvByVideoInfo(
            @RequestParam(value = "pageSize", defaultValue = "20") int size,
            @RequestParam(value = "labels", defaultValue = "") String labels,
            @RequestParam(value = "keywords", defaultValue = "") String keywords,
            @RequestParam(value = "excludeVideoId", defaultValue = "") String excludeVideoId) {
        long beginTime = System.currentTimeMillis();
        String[] liveIds = esService.getLivesIdsByLabel(labels + keywords, 1);
        String[] videoIds = esService.getVideoIdsByLabel(labels + keywords, new String[]{excludeVideoId}, 3 - liveIds.length);
        String[] articleIds = esService.getArticleIdsByLabel(labels + keywords, size - liveIds.length - videoIds.length);
        List<String> result = new ArrayList<>();
        result.addAll(addTypeForIds(Arrays.asList(videoIds), "video"));
        result.addAll(addTypeForIds(Arrays.asList(liveIds), "live"));
        result.addAll(addTypeForIds(Arrays.asList(articleIds), "article"));
        logger.info("/mul/ALV/videoInfo?labels={}&keywords={}&excludeVideoId={}&pageSize={}  cost: {}ms", labels, keywords, excludeVideoId, size, System.currentTimeMillis() - beginTime);

        return result;
    }


    /**
     * 旧的接口：
     * abnormal-recommder的ArticleRecomController中的getRecomByKwList()方法
     * 新的接口如下：
     */
    @ApiOperation(value = "根据关键词推荐资讯、视频、直播  【/getRecom/reportHelp】", notes = "原接口: http://192.168.1.152:8002/swagger-ui.html#!/article-recom-controller/getRecomByKwListUsingPOST")
    @GetMapping(value = "/mul/ALV/keywordArray/{keyword}")
    public Object getMulAlvByKeywords(
            @RequestParam(value = "pageSize", defaultValue = "20") int size,
            @PathVariable(value = "keyword") String[] keywordArray) {
        long beginTime = System.currentTimeMillis();
        Map<String, List<String>> map = new HashMap<>();
        for (String keyword : keywordArray) {
            String[] liveIds = esService.getLivesIdsByLabel(keyword, 1);
            String[] videoIds = esService.getVideoIdsByLabel(keyword, 3 - liveIds.length);
            String[] articleIds = esService.getArticleIdsByLabel(keyword, size - liveIds.length - videoIds.length);
            List<String> result = new ArrayList<>();
            result.addAll(addTypeForIds(Arrays.asList(videoIds), "video"));
            result.addAll(addTypeForIds(Arrays.asList(liveIds), "live"));
            result.addAll(addTypeForIds(Arrays.asList(articleIds), "article"));
            map.put(keyword, result);
        }
        logger.info("/mul/ALV/keywordArray/{} cost: {}ms", StringUtils.arrayToCommaDelimitedString(keywordArray), System.currentTimeMillis() - beginTime);
        return map;
    }

    private List<String> addTypeForIds(List<String> ids, String type) {
        List<String> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            result.add(String.format("%s:%s", id, type));
        }
        return result;
    }


    /**
     * 输入异常名和异常别名,返回匹配的文章
     * 对应abnorm-recommder项目中的ArticleRecomController中的getRecomByArticle()方法
     * curl -X POST --header "Content-Type: application/json" --header  "http://47.98.165.120:8002/getRecom/examGuide?userId=c63fc45c-35d1-43d5-b864-2bdb82542dfd&pageSize=10&abnormialStr=%E9%BC%BB%E7%82%8E&abnormialAliasStr=%E9%BC%BB%E7%82%8E"
     * 去除旧接口中的userId
     *
     * @param size
     * @param abnormialStr
     * @param abnormialAliasStr
     * @return
     */
    @ApiOperation(value = "输入异常名和异常别名,返回匹配的文章  【/getRecom/examGuide】", notes = "原接口: http://192.168.1.152:8002/swagger-ui.html#!/article-recom-controller/getRecomByExamGuideUsingPOST ")
    @GetMapping(value = "/article/abnorm")
    public String[] getArticleByAbnorm(
            @RequestParam(value = "pageSize", defaultValue = "20") int size,
            @RequestParam(value = "abnormialStr", defaultValue = "") String abnormialStr,
            @RequestParam(value = "abnormialAliasStr", defaultValue = "") String abnormialAliasStr) {
        long beginTime = System.currentTimeMillis();
        String[] result = esService.getArticleIdsByAbnormStr(abnormialStr, abnormialAliasStr, size, true);
        logger.info("/article/abnorm?abnormialStr={}&abnormialAliasStr={} cost: {}ms", abnormialStr, abnormialAliasStr, System.currentTimeMillis() - beginTime);
        return result;

    }

    @ApiOperation(value = "输入关键词数组，过滤出在视频、直播、文章的title、keywords或者labe中出现的关键词",
            notes = "输入关键词数组，过滤出视频、直播、文章的title、keywords、labe中出现的词。\n" +
                    "原接口: http://192.168.1.152:8002/swagger-ui.html#!/article-recom-controller/getExistsKwListUsingGET")
    @GetMapping(value = "/existKeywords/{keywordArray}")
    public List<String> getExistsKwList(@PathVariable(value = "keywordArray") String[] keywordArray) {
        long beginTime = System.currentTimeMillis();
        List<String> list = new ArrayList<>();
        for (String keyword : keywordArray) {
            if (esService.isExistKeyword(keyword)) {
                list.add(keyword);
            }
        }
        logger.info("/existKeywords/{}  cost:{} ms", StringUtils.arrayToCommaDelimitedString(keywordArray), System.currentTimeMillis() - beginTime);
        return list;
    }


    /**
     * 根据标签返回文章
     * 旧的接口，
     * 对应developerApi中GetUserLabelController中的getMatchList()方法
     * 测试环境：curl -X GET --header  "http://47.98.165.120:8085/get/list?label=%E9%98%B4%E9%81%93%E7%82%8E&pageSize=10"
     */
    @ApiOperation(value = "根据标签返回文章的详细内容  【/get/list】", notes = "根据标签返回文章的详细内容。注意:contentType现在已经没了\n原接口: http://192.168.1.152:8085/swagger-ui.html#!/get-user-label-controller/getMatchListUsingGET")
    @RequestMapping(value = "/detailsOfInfos/{label}", method = RequestMethod.GET)
    public Object getDetailsOfInfosByLabels(
            @PathVariable(value = "label") String labels,
            @RequestParam(value = "pageSize", defaultValue = "10") int size) {
        long beginTime = System.currentTimeMillis();
        List<ArticleContent> result = esService.getArticleContentByLabels(labels, size, jdbcService.categoryIdNameMap);
        logger.info("/detailsOfInfos/{}?pageSize={}  cost:{} ms", labels, size, System.currentTimeMillis() - beginTime);
        return result;
    }


}
