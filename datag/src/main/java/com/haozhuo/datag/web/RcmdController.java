package com.haozhuo.datag.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haozhuo.datag.model.AbnormalParam;
import com.haozhuo.datag.model.GoodsSearchParams;
import com.haozhuo.datag.model.PrefUpdateMsg;
import com.haozhuo.datag.model.SkuIdGoodsIds;
import com.haozhuo.datag.service.*;
import com.haozhuo.datag.service.biz.InfoRcmdService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Created by Lucius on 8/16/18.
 */

@SuppressWarnings({"SameReturnValue", "unused"})
@RequestMapping(value = "/rcmd")
@RestController
public class RcmdController {
    private static final Logger logger = LoggerFactory.getLogger(RcmdController.class);

    private final EsService esService;

    private final RedisService redisService;

    private final KafkaService kafkaService;

    private final DataEtlJdbcService dataetlJdbcService;

    private final InfoRcmdService infoRcmdService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RcmdController(Environment env,EsService esService, RedisService redisService, KafkaService kafkaService, DataEtlJdbcService dataetlJdbcService) {
        this.esService = esService;
        this.redisService = redisService;
        this.kafkaService = kafkaService;
        this.dataetlJdbcService = dataetlJdbcService;
        infoRcmdService = new InfoRcmdService(Boolean.valueOf(env.getProperty("app.biz.send-rcmd-msg", "false")), esService, redisService, dataetlJdbcService, kafkaService);
    }

    /**
     * 输入userId,返回推荐商品的id
     * 对应原来good-recommender项目中的GoodsRecomController中的getMatchContent()方法。
     */
    @GetMapping("/goods/userId/{userId}")
    @ApiOperation(value = "输入userId,返回推荐商品的id  【/goodsmatch/list/all】",
            notes = "输入userId,返回推荐商品的id。  \n " +
                    "原接口: http://192.168.1.152:8087/swagger-ui.html#!/goods-recom-controller/getMatchContentUsingPOST  \n" +
                    "业务逻辑:  \n" +
                    "1.从mysql中的dynamic_userid_label表和report_userid_label中查出用户的label。  \n" +
                    "2.从Redis中的'goods-pushed:{userId}:{date}'这个key得到最近15天已经推荐过的商品。  \n" +
                    "3.步骤1中得到的label与ES中good索引的label字段进行匹配，排除步骤2中最近已经推荐过的商品，得到可以推荐的商品id。  \n" +
                    "4.将这次推荐的商品id存入Redis中的'goods-pushed:{userId}:{date}'  \n" +
                    "5.如果所有商品都已经推荐完，那么清除Redis中的key，让商品可以重新加入到推荐队列中。")
    public Object getGoodsIdsByUserId(
            @PathVariable(value = "userId") String userId,
            @RequestParam(value = "size", defaultValue = "10") int pageSize) {
        long beginTime = System.currentTimeMillis();
        String[] alreadyPushedSkuIds = redisService.getPushedGoodsSkuIds(userId);
        //从mysql查出userId的label
        String userLabels = esService.getPortraitDiseaseLabelsByUserId(userId);

        GoodsSearchParams params = new GoodsSearchParams().size(pageSize).excludeSkuIds(alreadyPushedSkuIds);
        if (userLabels != null && !"".equals(userLabels)) {
            params.keywords(userLabels);
        }
        logger.debug(params.toString());
        //根据userId的label匹配es中good索引中的label，返回内容。
        List<SkuIdGoodsIds> result = esService.getSkuIdGoodsIdsByLabels(params);
        String[] skuIds = result.stream().map(SkuIdGoodsIds::getSkuId).toArray(String[]::new);
        String[] goodsIds = result.stream().map(SkuIdGoodsIds::getRandomGoodsId).toArray(String[]::new);
        //如果返回的数量小于pageSize，删除Redis中推过的商品列表的key
        redisService.addPushedGoodsSkuIds(userId, skuIds);
        //如果返回的数据小于pageSize,则认为所有的视频都被推荐了，那么将Redis中推过的视频列表的key删除，使得所有视频可以重新推送
        if (result.size() < pageSize) {
            redisService.deletePushedGoodsSkuIds(userId);
        }
        logger.info("/goods/userId/{}?size={}  cost: {}ms", userId, pageSize, System.currentTimeMillis() - beginTime);
        return goodsIds;
    }

    /**
     * 根据
     * 对应原来good-recommender项目中的GoodsRecomController中的getRecomList()方法。
     * curl -X POST --header "ArticleInfo-Type: application/json" --header  "http://192.168.1.152:8087/goodsmatch/getRecom/list?healthReportId=2515473&pageSize=10&pageNum=1"
     *
     * @return
     */
    @GetMapping("/goods/reportId/{reportId}")
    @ApiOperation(value = "根据报告Id返回推荐的商品  【/goodsmatch/getRecom/list】",
            notes = "根据报告Id返回推荐的商品。  \n" +
                    "原接口: http://192.168.1.152:8087/swagger-ui.html#!/goods-recom-controller/getRecomListUsingPOST  \n" +
                    "业务逻辑：  \n" +
                    "1.根据reportId从ES的reportlabel索引中得到报告的label。  \n" +
                    "2.步骤1中的label与ES中good索引的label字段进行匹配，得到推荐的商品id。")
    public Object getGoodsIdsByReportId(
            @PathVariable(value = "reportId") String reportId,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum) {
        long beginTime = System.currentTimeMillis();
        String labels = esService.getLabelsByReportId(reportId);
        if ("".equals(labels)) labels = null;
        String[] result = esService.getGoodsIdsByKeywords(new GoodsSearchParams().keywords(labels).pageNo(pageNum).size(pageSize));
        logger.info("/goods/reportId/{}?pageSize={}&pageNum={}  cost: {}ms", reportId, pageSize, pageNum, System.currentTimeMillis() - beginTime);
        return result;
    }

    @GetMapping("/goods/labels/{labels}")
    @ApiOperation(value = "根据标签返回推荐的商品【新增】",
            notes = "根据标签返回推荐的商品。  \n" +
                    "业务逻辑：  \n" +
                    "labels与ES中good索引的label字段进行匹配，得到推荐的商品id。")
    public Object getGoodsIdsByLabels(
            @PathVariable(value = "labels") String labels) {
        long beginTime = System.currentTimeMillis();
        String[] result = esService.getGoodsIdsByKeywords(new GoodsSearchParams().keywords(labels).size(10));
        logger.info("/goods/labels/{}  cost: {}ms", labels, System.currentTimeMillis() - beginTime);
        return result;
    }

    @GetMapping("/goods/home")
    @ApiOperation(value = "首页商品推荐",
            notes = "当返回的数据条数小于size*0.75条(即当size是40时就是30条)时，说明已经推荐完了。下一次传递pageNo时，重新从1开始。返回结果30~60条之间，都认为是正常返回。")
    public Object getGoodsIdsHome(
            @RequestParam(value = "userId", defaultValue = "null") String userId,
            @RequestParam(value = "cityId", defaultValue = "000000") String cityId,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "size", defaultValue = "40") int size
    ) throws Exception {
        long beginTime = System.currentTimeMillis();
        if ("null".equals(cityId)) {
            cityId = "000000";
        }

        //根据商品销量和新品查找商品40篇
        double salesPercent = 0.75;
        Set<SkuIdGoodsIds> queryResult = esService.getSkuIdsBySalesAndNews(cityId, pageNo, size, salesPercent);

        if (!"null".equals(userId) && queryResult.size() > (size * salesPercent)) { // 有用户ID
            //从ES中查询用户疾病标签
            String labels = esService.getPortraitDiseaseLabelsByUserId(userId);

            //根据用户疾病标签查找商品10篇
            List<SkuIdGoodsIds> goodsByLabel = esService.getSkuIdGoodsIdsByLabels(
                    new GoodsSearchParams().keywords(labels).cityId(cityId).pageNo(pageNo).size((int) (size * 0.5)));
            logger.debug("goodsByLabel:{}", goodsByLabel.size());
            queryResult.addAll(goodsByLabel);
        }

        List<String> goodsIds = queryResult.stream().map(SkuIdGoodsIds::getRandomGoodsId).collect(toList());
        Collections.shuffle(goodsIds);
        logger.info("/goods/home?userId={}&cityId={}&pageNo={}&size={}   result.length={}  cost: {}ms",
                userId, cityId, pageNo, size, goodsIds.size(), System.currentTimeMillis() - beginTime);
        return goodsIds;
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
    @ApiOperation(value = "根据userId,匹配推荐的视频列表  【/videomatch/recommend/all】",
            notes = "输入对应的userId,返回推荐视频列表。  \n" +
                    "原接口: http://192.168.1.152:8089/swagger-ui.html#!/video-recom-controller/getMatchContentUsingPOST  \n" +
                    "业务逻辑:  \n" +
                    "1.从mysql中的dynamic_userid_label表和report_userid_label中查出用户的label。  \n" +
                    "2.从Redis中的'video-pushed:{userId}:{date}'这个key得到最近15天已经推荐过的视频。  \n" +
                    "3.步骤1中得到的label与ES中video4索引的tags、title字段进行匹配，排除步骤2中最近已经推荐过的视频，得到这次推荐的视频id。  \n" +
                    "4.将这次推荐的视频id存入Redis中的'video-pushed:{userId}:{date}'中  \n" +
                    "5.如果所有视频都已经推荐完，那么清除Redis中的key，让视频可以重新加入到推荐队列中。")
    public Object getVideoListByUserId(
            @PathVariable(value = "userId") String userId,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        long beginTime = System.currentTimeMillis();
        String[] alreadyPushedVideos = redisService.getPushedVideos(userId);
        logger.debug("alreadyPushedVideos:{}", StringUtils.arrayToCommaDelimitedString(alreadyPushedVideos));
        String userLabels = esService.getPortraitDiseaseLabelsByUserId(userId);

        //最后需要把查到的结果存入Redis的已推荐的key中
        String[] result = esService.getVideoIds(userLabels, alreadyPushedVideos, size);
        redisService.setPushedVideos(userId, result);
        //如果返回的数据小于pageSize,则认为所有的视频都被推荐了，那么将Redis中推过的视频列表的key删除，使得所有视频可以重新推送
        if (result.length < size) {
            redisService.deleteVideoPushedKey(userId);
        }
        logger.info("/video/userId/{}?size={}  cost: {}ms", userId, size, System.currentTimeMillis() - beginTime);
        return result;
    }


    /**
     * 旧的接口：
     * curl -X POST --header "ArticleInfo-Type: application/json"  "http://datanode2:9090/api/video-recommder/videomatch/relative/all?vid=570"
     */
    @GetMapping("/video/videoId/{videoId}")
    @ApiOperation(value = "根据videoId返回与它相似的视频列表  【/videomatch/relative/all】",
            notes = "根据videoId返回与它相似的视频列表。  \n" +
                    "原接口: http://192.168.1.152:8089/swagger-ui.html#!/video-recom-controller/getRelativeContentUsingPOST  \n" +
                    "1.根据videoId从mysql的video4表中找到它的tags。  \n" +
                    "2.将ES的video4中的tags与步骤1中的tags进行匹配，获取相似的videoIds。")
    public Object getVideoListBySimilarity(
            @PathVariable(value = "videoId") String videoId,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        long beginTime = System.currentTimeMillis();
        String videoTags = dataetlJdbcService.getVideoTagsById(videoId);
        String[] result = esService.getSimilarVideoIdsByTags(videoId, videoTags, size);
        logger.info("/video/videoId/{}?size={}  cost: {}ms", videoId, size, System.currentTimeMillis() - beginTime);
        return result;
    }


    /**
     * 根据userId获取用户的标签
     * 旧的接口，
     * 对应developerApi中GetUserLabelController中的getMatchContent()方法
     * 测试环境：http://47.98.165.120:8085/get?userId=00007d91-fefe-4234-bc08-2496ea8360c6
     */
    @ApiOperation(value = "根据userId获取用户的标签  【/get】",
            notes = "根据userId获取用户的标签  \n " +
                    "原接口: http://192.168.1.152:8085/swagger-ui.html#!/get-user-label-controller/getMatchContentUsingGET  \n" +
                    "业务逻辑:  \n" +
                    "从mysql中的dynamic_userid_label表和report_userid_label中查出用户的label。  ")
    @GetMapping(value = "/labels/userId/{userId}")
    public String getLabelsByUserId(@PathVariable(value = "userId") String userId) {
        long beginTime = System.currentTimeMillis();
        String result = esService.getPortraitDiseaseLabelsByUserId(userId);
        logger.info("/labels/userId/{}  cost: {}ms", userId, System.currentTimeMillis() - beginTime);
        return result;
    }

    /**
     * 根据healthReportId返回报告的标签
     * 旧的接口，
     * 对应developerApi中GetUserLabelController中的getBasicInfoByReport()方法
     * 测试环境：http://192.168.1.152:8085/getByReport?healthReportId=1
     */
    @ApiOperation(value = "根据healthReportId返回报告的标签  【/getByReport】",
            notes = "根据healthReportId返回报告的标签  \n" +
                    "原接口: http://192.168.1.152:8085/swagger-ui.html#!/get-user-label-controller/getBasicInfoByReportUsingGET  \n" +
                    "业务逻辑:  \n" +
                    "根据reportId从ES的reportlabel索引找到该报告的标签")
    @GetMapping(value = "/labels/reportId/{reportId}")
    public String getLabelsByReportId(@PathVariable(value = "reportId") String reportId) {
        long beginTime = System.currentTimeMillis();
        String result = esService.getLabelsByReportId(reportId);
        logger.info("/labels/reportId/{}  cost: {}ms", reportId, System.currentTimeMillis() - beginTime);
        return result;
    }


    /**
     * (该接口废除，请从java的mysql数据库中根据tags返回文章详细内容)
     * 根据标签返回文章
     * 旧的接口，
     * 对应developerApi中GetUserLabelController中的getMatchList()方法
     * 测试环境：curl -X GET --header  "http://47.98.165.120:8085/get/list?label=%E9%98%B4%E9%81%93%E7%82%8E&pageSize=10"
     */
    @ApiOperation(value = "(该接口废除，请从java的mysql数据库中根据tags返回文章详细内容) 根据标签返回文章的详细内容  【/get/list】",
            notes = "(该接口废除，请从java的mysql数据库中根据tags返回文章详细内容!!)   \n" +
                    "原接口: http://192.168.1.152:8085/swagger-ui.html#!/get-user-label-controller/getMatchListUsingGET")
    @RequestMapping(value = "/detailsOfInfos/{label}", method = RequestMethod.GET)
    @Deprecated
    public Object getDetailsOfInfosByLabels() {
        return null;
    }

    /**
     * 根据userId和channelId获取资讯、视频、直播的推荐列表
     * 旧接口：
     * recommender项目中的EsMatcherController中的enter(),getMatchContent()方法。这两个方法合并成一个。
     * ES 匹配时考虑不感兴趣的标签
     * curl -XGET "192.168.1.152:9200/article3/_search?pretty" -d '{"size":3,"query": {"bool": {"should": [{ "match": { "title": "风湿关节炎食疗方剂,肺炎,近视"}},{"match": {"tags": "肺炎,风湿,脂肪肝" }}],"must_not": {"match": { "tags":"肺炎,风湿"}}}}}'
     *
     * @return
     */
    @GetMapping("/mul/ALV/user_channel")
    @ApiOperation(value = "userId和channelId获取资讯、视频、直播的推荐列表  【/list/current/all,/list/current/scroll/all,...】",
            notes = "userId和channelId获取资讯、视频、直播的推荐列表  \n" +
                    "es-matcher-controller中的四个方法合并成一个  \n" +
                    "原接口: http://192.168.1.152:8078/swagger-ui.html#/  \n" +
                    "当前接口参数:  \n" +
                    "userId可以不传，那么随机从这个频道中取出{size}条数据，否则会考虑用户标签进行推荐  \n" +
                    "业务逻辑:  \n" +
                    "1 推荐频道(channelType==R):  \n" +
                    "  所有:需要用户标签  \n" +
                    "2 视频频道(channelType==V):  \n" +
                    "  所有:需要用户标签  \n" +
                    "3 直播频道(channelType==L):  \n" +
                    "  所有:需要用户标签  \n" +
                    "4 文章频道(channelType==A):  \n" +
                    "       categoryId==0,该频道下的所有,需要用户标签信息 \n" +
                    "       categoryId>0,该频道下的分类,不需要用户标签信息  \n"
    )
    public Object getInfosByUserChannel(
            @RequestParam(value = "channelType", defaultValue = "R") String channelType,
            @RequestParam(value = "channelId", defaultValue = "0") String channelId,
            @RequestParam(value = "categoryId", defaultValue = InfoRcmdService.allCategoryId) String categoryId,
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        if ("R".equalsIgnoreCase(channelType))  //推荐频道下没有分类
            categoryId = InfoRcmdService.allCategoryId;
        return infoRcmdService.channelRecommendNews(channelType, channelId, categoryId, userId, size);
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
    @ApiOperation(value = "根据infoId获取相似的资讯、视频、直播、商品  【/getSimi/all】")
    @RequestMapping(value = "/mul/ALVG/infoAndCity", method = RequestMethod.GET)
    public Object getMulAlvgByInfoAndCity(
            @RequestParam(value = "infoId") String infoId,
            @RequestParam(value = "cityId", defaultValue = "000000") String cityId,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        long beginTime = System.currentTimeMillis();
        String tags = dataetlJdbcService.getTagsKeywordsByInfoId(infoId);
        Map<String, List<String>> map = new HashMap<>();
        logger.debug("tags:{}", tags);
        String[] articleIds = esService.getArticleIds(tags, new String[]{infoId}, size);
        String[] liveIds = esService.getLivesIds(tags, new String[]{}, size);
        String[] videoIds = esService.getVideoIds(tags, new String[]{}, size);
        if ("null".equals(cityId)) {
            cityId = "000000";
        }

        map.put("a", Arrays.asList(articleIds));
        map.put("l", Arrays.asList(liveIds));
        map.put("v", Arrays.asList(videoIds));

        String goodsId = esService.getOneOfMatchedGoodsId(new GoodsSearchParams().keywords(tags).cityId(cityId).from(0).size(size));
        List<String> goodsIds = new ArrayList<>();
        if (goodsId != null) {
            goodsIds.add(goodsId);
        }
        map.put("g", goodsIds);
        logger.info("/mul/ALVG/infoAndCity?infoId={}&cityId={} resut->goodsId:{}  cost: {}ms", infoId, cityId, goodsId, System.currentTimeMillis() - beginTime);
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
            notes = "根据异常项信息(jsonStr)获取相似的资讯、视频、直播。jsonStr格式如下:  \n" +
                    " {\"exceptionItemName\":\"近视\",\"exceptionItemAlias\": \"肥胖\"," +
                    " \"possibleDiseases\": \"高血压\",\"possibleDiseaseAlias\": \"近视\"," +
                    " \"possibleSymptoms\":\"近视\",\"possibleSymptomAlias\": \"近视\"} 。  \n" +
                    "原接口: http://47.98.165.120:8002/swagger-ui.html#!/article-recom-controller/getRecomByAbnormialAllUsingPOST  \n" +
                    "1.根据jsonStr匹配ES中lives_info索引中的title、labels和keywords，找到匹配度最高的1条。  \n" +
                    "2.根据jsonStr匹配ES中video4索引中的title和tags，找到匹配度最高的2条。   \n" +
                    "3.根据jsonStr匹配ES中article4索引中的title和tags，找到匹配度最高的{size}-3条。  \n" +
                    "4.将上述结果放在一起返回。")
    @GetMapping(value = "/mul/ALV/abnorm")
    public Object getMulAlvByAbnorm(
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "jsonStr") String jsonStr) {
        long beginTime = System.currentTimeMillis();
        AbnormalParam abnormal;
        try {
            abnormal = mapper.readValue(jsonStr, AbnormalParam.class);
        } catch (IOException e) {
            logger.error("getMulAlvByAbnorm parse Json error:{}", e);
            return null;
        }
        List<String> livesIds = esService.getLiveIdsByAbnorm(abnormal, 1);
        List<String> videoIds = esService.getVideoIdsByAbnorm(abnormal, 3 - livesIds.size());
        List<String> articleIds = esService.getArticleIdsByAbnorm(abnormal, size - videoIds.size() - livesIds.size());
        List<String> result = new ArrayList<>();
        result.addAll(addTypeForIds(videoIds, "video"));
        result.addAll(addTypeForIds(livesIds, "live"));
        result.addAll(addTypeForIds(articleIds, "article"));
        logger.info("/mul/ALV/abnorm?jsonStr={}  cost: {}ms", jsonStr, System.currentTimeMillis() - beginTime);
        return result;
    }


    /**
     * 旧的接口：
     * abnormal-recommder的ArticleRecomController中的getRecomByVideoALL()方法
     * curl -X POST --header "Content-Type: application/json" --header  "http://47.98.165.120:8002/getRecom/videoTags?userId=c63fc45c-35d1-43d5-b864-2bdb82542dfd&pageSize=10&jsonStr=%7B%22labels%22%3A%22%E8%82%BE%E5%8A%9F%E8%83%BD%E9%9A%9C%E7%A2%8D%22%2C%22keywords%22%3A%20%22%E8%82%BE%E7%97%85%2C%E8%82%BE%E5%8A%9F%E8%83%BD%2C%E8%A1%80%E5%8E%8B%2C%E8%82%BE%E8%A1%B0%22%2C%22title%22%3A%22%E5%89%8D%E5%88%97%E8%85%BA%E9%92%99%E5%8C%96%E6%98%AF%E6%80%8E%E4%B9%88%E5%9B%9E%E4%BA%8B-%E6%9D%8E%E6%B5%B7%E6%9D%BE%22%7D"
     * jsonStr的格式: {"labels":"肾功能障碍","keywords": "肾病,肾功能,血压,肾衰","title":"前列腺钙化是怎么回事-李海松"}
     * 注意：新的接口和旧的接口对jsonStr进行了变更
     * 原来的
     * jsonStr : {"labels":"肾功能障碍", "keywords": "肾病,肾功能,血压,肾衰", "title":"前列腺钙化是怎么回事-李海松"}
     * 变更成请求参数，用excludeVideoId替换上面的title。
     * 去除旧接口中的userId请求参数,
     * <p>
     * <p>
     * 匹配labels和keywords,但是过滤掉excludeInfoId的视频
     * 新的接口如下：
     */
    @ApiOperation(value = "根据视频的相关信息获取相似的资讯、视频、直播  【/getRecom/videoTags】",
            notes = "原接口: http://192.168.1.152:8002/swagger-ui.html#!/article-recom-controller/getRecomByVideoUsingPOST  \n" +
                    "原来的jsonStr参数: {\"labels\":\"肾功能障碍\", \"keywords\": \"肾病,肾功能,血压,肾衰\", \"title\":\"前列腺钙化是怎么回事-李海松\"}  \n" +
                    "变更成请求参数tags和excludeVideoId  \n" +
                    "业务逻辑:  \n" +
                    "1.根据tags匹配ES中lives_info索引中的title、labels和keywords，找到匹配度最高的1条。  \n" +
                    "2.根据tags匹配ES中video4索引中的title和tags，但是排除id为{excludeVideoId}的视频，找到匹配度最高的2条。   \n" +
                    "3.根据tags匹配ES中article4索引中的title和tags，找到匹配度最高的{size}-3条。  \n" +
                    "4.将上述结果放在一起返回。")
    @GetMapping(value = "/mul/ALV/videoInfo")
    public Object getMulAlvByVideoInfo(
            @RequestParam(value = "pageSize", defaultValue = "20") int size,
            @RequestParam(value = "tags", defaultValue = "") String tags,
            @RequestParam(value = "excludeVideoId", defaultValue = "") String excludeVideoId) {
        long beginTime = System.currentTimeMillis();
        String[] liveIds = esService.getLivesIds(tags, 1);
        String[] videoIds = esService.getVideoIds(tags, new String[]{excludeVideoId}, 3 - liveIds.length);
        String[] articleIds = esService.getArticleIds(tags, size - liveIds.length - videoIds.length);
        List<String> result = new ArrayList<>();
        result.addAll(addTypeForIds(Arrays.asList(videoIds), "video"));
        result.addAll(addTypeForIds(Arrays.asList(liveIds), "live"));
        result.addAll(addTypeForIds(Arrays.asList(articleIds), "article"));
        logger.info("/mul/ALV/videoInfo?tags={}&excludeVideoId={}&pageSize={}  cost: {}ms", tags, excludeVideoId, size, System.currentTimeMillis() - beginTime);
        return result;
    }


    /**
     * 旧的接口：
     * abnormal-recommder的ArticleRecomController中的getRecomByKwList()方法
     * 新的接口如下：
     */
    @ApiOperation(value = "根据关键词推荐资讯、视频、直播  【/getRecom/reportHelp】",
            notes = "根据关键词推荐资讯、视频、直播  \n" +
                    "原接口: http://192.168.1.152:8002/swagger-ui.html#!/article-recom-controller/getRecomByKwListUsingPOST  \n" +
                    "业务逻辑：  \n" +
                    "传入的关键词是一个数组，对于数组中的每个关键词，进行以下操作：  \n" +
                    "  1.根据关键词匹配ES中lives_info索引中的title、labels和keywords，找到匹配度最高的1条。  \n" +
                    "  2.根据关键词匹配ES中video4索引中的title和tags，但是排除id为{excludeVideoId}的视频，找到匹配度最高的2条。   \n" +
                    "  3.根据关键词匹配ES中article4索引中的title和tags，找到匹配度最高的{size}-3条。  \n" +
                    "数组中所有关键词都执行完后，一起返回")
    @GetMapping(value = "/mul/ALV/keywordArray/{keyword}")
    public Object getMulAlvByKeywords(
            @RequestParam(value = "pageSize", defaultValue = "20") int size,
            @PathVariable(value = "keyword") String[] keywordArray) {
        long beginTime = System.currentTimeMillis();
        Map<String, List<String>> map = new HashMap<>();
        for (String keyword : keywordArray) {
            map.put(keyword, getAlvByKeywords(size, keyword));
        }
        logger.info("/mul/ALV/keywordArray/{} cost: {}ms", StringUtils.arrayToCommaDelimitedString(keywordArray), System.currentTimeMillis() - beginTime);
        return map;
    }

    private List<String> getAlvByKeywords(int size, String keyword) {
        String[] liveIds = esService.getLivesIds(keyword, 1);
        String[] videoIds = esService.getVideoIds(keyword, 3 - liveIds.length);
        String[] articleIds = esService.getArticleIds(keyword, size - liveIds.length - videoIds.length);
        List<String> result = new ArrayList<>();
        result.addAll(addTypeForIds(Arrays.asList(videoIds), "video"));
        result.addAll(addTypeForIds(Arrays.asList(liveIds), "live"));
        result.addAll(addTypeForIds(Arrays.asList(articleIds), "article"));
        return result;
    }

    private List<String> addTypeForIds(List<String> ids, String type) {
        return ids.stream().map(id -> String.format("%s:%s", id, type)).collect(toList());
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
    @ApiOperation(value = "输入异常名和异常别名,返回匹配的文章  【/getRecom/examGuide】",
            notes = "输入异常名和异常别名,返回匹配的文章  \n" +
                    "原接口: http://192.168.1.152:8002/swagger-ui.html#!/article-recom-controller/getRecomByExamGuideUsingPOST  \n" +
                    "业务逻辑:  \n" +
                    "abnormialStr和abnormialAliasStr去匹配Article4中的tags和title字段。abnormialStr权重高于abnormialAliasStr。" +
                    "将匹配度最高的size条数据返回。")
    @GetMapping(value = "/article/abnorm")
    public String[] getArticleByAbnorm(
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "abnormialStr", defaultValue = "") String abnormialStr,
            @RequestParam(value = "abnormialAliasStr", defaultValue = "") String abnormialAliasStr) {
        long beginTime = System.currentTimeMillis();
        String[] result = esService.getArticleIdsByAbnormStr(abnormialStr, abnormialAliasStr, size, true);
        logger.info("/article/abnorm?abnormialStr={}&abnormialAliasStr={} cost: {}ms", abnormialStr, abnormialAliasStr, System.currentTimeMillis() - beginTime);
        return result;
    }

    @ApiOperation(value = "输入关键词数组，过滤出在视频、直播、文章的title、tags中出现的关键词",
            notes = "输入关键词数组，过滤出视频、直播、文章的title、tags中出现的词。\n" +
                    "原接口: http://192.168.1.152:8002/swagger-ui.html#!/article-recom-controller/getExistsKwListUsingGET  \n" +
                    "业务逻辑:  \n" +
                    "只要满足以下任意条件，则认为该关键词存在：  \n" +
                    "1.该关键词能匹配到(matchPhraseQuery)ES中article4索引中的title或tags字段。  \n" +
                    "2.该关键词能匹配到(matchPhraseQuery)ES中video4索引中的title或tags字段。  \n" +
                    "3.该关键词能匹配到(matchPhraseQuery)ES中lives_info索引中的title或keywords或labels字段。")
    @GetMapping(value = "/existKeywords/{keywordArray}")
    public List<String> getExistsKwList(@PathVariable(value = "keywordArray") String[] keywordArray) {
        long beginTime = System.currentTimeMillis();
        List<String> list = stream(keywordArray).filter(esService::isExistKeyword).collect(toList());
        logger.info("/existKeywords/{}  cost:{} ms", StringUtils.arrayToCommaDelimitedString(keywordArray), System.currentTimeMillis() - beginTime);
        return list;
    }


    @ApiOperation(value = "根据用户屏蔽的文章,存储用户不感兴趣的标签【新增】")
    @GetMapping(value = "/article/not_interested")
    public Object setNotInterestedTagsByInfoId(
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "infoId") Long infoId) {
        //为视频、直播推荐
        //String tags = dataetlJdbcService.getInfoTagsById(infoId);
        //redisService.addHateTags(userId, tags); // 其他的推荐还是有用处的
        kafkaService.sendPrefUpdateMsg(new PrefUpdateMsg(5, userId, infoId.toString()));
        logger.info("/article/not_interested?userId={}&infoId={}", userId, infoId);
        return "success!";
    }

    @ApiOperation(value = "异常疾病标准化", notes="注意：现接口的参数和返回消息与原接口不同。   \n" +
            "现接口:   \n" +
            "curl -X GET --header 'Accept: application/json' 'http://192.168.20.227:8766/datag/rcmd/getNormTag?abnormals=肝功能异常,幽门螺旋杆菌抗体增高'   \n    " +
            "原接口:   \n" +
            "curl -X POST  --header 'Accept: application/json' -d '{\"abnormal\":[\"肝功能异常\", \"幽门螺旋杆菌抗体 增高\"]}' 'http://192.168.20.228:5020/getNormTag'")
    @GetMapping(value = "/getNormTag")
    public Object getNormTag(
            @RequestParam (value = "abnormals") String abnormals) {
        return dataetlJdbcService.getNormTags(abnormals);
    }

    @ApiOperation(value = "根据非标准化的异常项返回筛查项目以及标准异常项", notes="t1表示标准化的异常,t2表示建议筛查项 ")
    @GetMapping(value = "/getNormTagCheckItems")
    public Object getNormTagCheckItems(
            @RequestParam (value = "abnormals") String abnormals) {
        return dataetlJdbcService.getNormTagCheckItems(abnormals);
    }


    @ApiOperation(value = "异常疾病标准化2", notes="注意：现接口的不需要传userId。   \n" +
            "现接口:   \n" +
            "curl -X GET --header 'Accept: application/json' 'http://192.168.20.227:8766/datag/rcmd/getSingleNormTag?abnormals=幽门螺旋杆菌抗体增高'   \n    " +
            "原接口:   \n" +
            "curl  -H 'Accept: application/json' -H 'Content-type: application/json' -XGET http://192.168.20.228:5006/getNormTag?abnormial=幽门螺旋杆菌抗体增高&userId=c63fc45c-35d1-43d5-b864-2bdb82542dfd")
    @GetMapping(value = "/getSingleNormTag")
    public Object getSingleNormTag(
            @RequestParam (value = "abnormal") String abnormal) {
        return dataetlJdbcService.getSingleNormTag(abnormal);
    }
}
