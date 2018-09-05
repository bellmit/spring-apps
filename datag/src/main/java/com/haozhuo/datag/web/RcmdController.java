package com.haozhuo.datag.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haozhuo.datag.model.AbnormalParam;
import com.haozhuo.datag.model.InfoALVArray;
import com.haozhuo.datag.service.*;
import com.haozhuo.datag.service.biz.InfoRcmdService;
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

    private final EsService esService;

    private final RedisService redisService;

    private final KafkaService kafkaService;

    private final DataetlJdbcService dataetlJdbcService;

    private final InfoRcmdService infoRcmdService;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public RcmdController(EsService esService, RedisService redisService, KafkaService kafkaService, DataetlJdbcService dataetlJdbcService) {
        this.esService = esService;
        this.redisService = redisService;
        this.kafkaService = kafkaService;
        this.dataetlJdbcService = dataetlJdbcService;
        infoRcmdService = new InfoRcmdService(esService, redisService, dataetlJdbcService);
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
        String[] alreadyPushedGoods = redisService.getPushedGoods(userId);
        //从mysql查出userId的label
        String userLabels = dataetlJdbcService.getLabelStrByUserId(userId);

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
        if ("".equals(labels))
            return null;
        int from = (pageNum - 1) * pageSize;
        String[] result = esService.getGoodsIdsByLabels(labels, from, pageSize);
        logger.info("/goods/reportId/{}?pageSize={}&pageNum={}  cost: {}ms", reportId, pageSize, pageNum, System.currentTimeMillis() - beginTime);
        return result;
    }

    /**
     * 根据
     * 对应原来good-recommender项目中的GoodsRecomController中的getRecomList()方法。
     * curl -X POST --header "ArticleInfo-Type: application/json" --header  "http://192.168.1.152:8087/goodsmatch/getRecom/list?healthReportId=2515473&pageSize=10&pageNum=1"
     *
     * @return
     */
    @GetMapping("/goods/labels/{labels}")
    @ApiOperation(value = "根据标签返回推荐的商品  【新增】",
            notes = "根据标签返回推荐的商品。  \n" +
                    "业务逻辑：  \n" +
                    "labels与ES中good索引的label字段进行匹配，得到推荐的商品id。")
    public Object getGoodsIdsByLabels(
            @PathVariable(value = "labels") String labels) {
        long beginTime = System.currentTimeMillis();
        String[] result = esService.getGoodsIdsByLabels(labels, new String[]{}, 10, "label", "content_name");
        logger.info("/goods/labels/{}  cost: {}ms", labels, System.currentTimeMillis() - beginTime);
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
        String userLabels = dataetlJdbcService.getLabelStrByUserId(userId);

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
    public String getLablesByUserId(@PathVariable(value = "userId") String userId) {
        long beginTime = System.currentTimeMillis();
        String result = dataetlJdbcService.getLabelStrByUserId(userId);
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
    public String getLablesByReportId(@PathVariable(value = "reportId") String reportId) {
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
     * 根据userId获取资讯、视频、直播的推荐列表
     * 旧接口：
     * recommender项目中的EsMatcherController中的enter(),getMatchContent()方法。这两个方法合并成一个。
     * ES 匹配时考虑不感兴趣的标签
     * curl -XGET "192.168.1.152:9200/article3/_search?pretty" -d '{"size":3,"query": {"bool": {"should": [{ "match": { "title": "风湿关节炎食疗方剂,肺炎,近视"}},{"match": {"tags": "肺炎,风湿,脂肪肝" }}],"must_not": {"match": { "tags":"肺炎,风湿"}}}}}'
     *
     * @return
     */
    @GetMapping("/mul/ALV/user_channel")
    @ApiOperation(value = "根据userId获取资讯、视频、直播的推荐列表  【/list/current/all,/list/current/scroll/all,...】",
            notes = "根据userId获取资讯、视频、直播的推荐列表  \n" +
                    "es-matcher-controller中的四个方法合并成一个  \n" +
                    "原接口: http://192.168.1.152:8078/swagger-ui.html#/  \n" +
                    "当前接口参数:  \n" +
                    "userId可以不传，那么随机从这个频道中取出{size}条数据，否则会考虑用户标签进行推荐  \n" +
                    "业务逻辑:  \n" +
                    "  \n" +
                    "1 视频频道(channelId==20000):  \n" +
                    "2 直播频道(channelId==30000):  \n" +
                    "  \n" +
                    "3 其他频道(其他的channelId):  \n" +
                    "3.1 传userId(对推过的资讯进行缓存，一段时间内不再推):  \n" +
                    "    步骤2.1中需要从Redis的PushedInfo:{userId}这个key中找到最近推荐过的视频和文章。  \n" +
                    "    PushedInfo:{userId}是一个Hash。HashKey是{a/v_channelId_categoryId}   \n" +
                    "3.1.1 推荐频道(channelId==10000,且categoryId==0,需要用户标签信息):  \n" +
                    "    点击行为+标签" +
                    "3.1.2 某频道下的所有(其他channelId,且categoryId==0,需要用户标签信息)  \n" +
                    "3.1.3 某频道下的某个分类(其他channelId,且categoryId>0,不需要用户标签信息)  \n"
    )
    public Object getInfosByUserChannel(
            @RequestParam(value = "channelId", defaultValue = InfoRcmdService.channelRcmdId) String channelId,
            @RequestParam(value = "categoryId", defaultValue = InfoRcmdService.allCategoryId) String categoryId,
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        long beginTime = System.currentTimeMillis();
        InfoALVArray result = infoRcmdService.channelRecommend(channelId, categoryId, userId, size);
        logger.info("/mul/ALV/user_channel?userId={}&channelId={}&categoryId={}  cost: {}ms", userId, channelId, categoryId, System.currentTimeMillis() - beginTime);
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
    @ApiOperation(value = "根据infoId获取相似的资讯、视频、直播、商品  【/getSimi/all】",
            notes = "输入infoId,返回相似的资讯、视频、直播、商品。  \n" +
                    "原接口: http://192.168.1.152:8085/getSimi/all?informationId=56  \n" +
                    "业务逻辑:  \n" +
                    "1.相似的资讯、视频和直播是从Redis中的'simi_{infoId}'这个key中获取。这是黄金宝之前就用的逻辑。  \n" +
                    "2.推荐商品的产生是通过查询传入资讯的tags，使用该tags匹配ES中good索引的label字段得到。  \n" +
                    "3.将上述的资讯、视频、直播和商品合并后进行返回。")
    @RequestMapping(value = "/mul/ALVG/infoId/{infoId}", method = RequestMethod.GET)
    public Object getMulAlvgByInfoId(
            @PathVariable(value = "infoId") String infoId,
            @RequestParam(value = "version", defaultValue = "1") int version) {
        long beginTime = System.currentTimeMillis();
        //资讯、视频、直播的结果
        Map<String, List<String>> map = redisService.getSimByInfoId(infoId);
        //商品的结果
        String tags;
        if (version == 1) {
            tags = dataetlJdbcService.getLabelsByInfoId(infoId); //TODO 大改版之后将这个条件去掉！！！！
        } else {
            tags = dataetlJdbcService.getTagsByInfoId(infoId);
        }

        logger.debug("tags:{}", tags);
        String[] goodsIds = esService.getGoodsIdsByLabels(tags, 0, 10);
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
        AbnormalParam abnormal = null;
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
     * TODO 注意：新的接口和旧的接口对jsonStr进行了变更
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
            String[] liveIds = esService.getLivesIds(keyword, 1);
            String[] videoIds = esService.getVideoIds(keyword, 3 - liveIds.length);
            String[] articleIds = esService.getArticleIds(keyword, size - liveIds.length - videoIds.length);
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
        List<String> list = new ArrayList<>();
        for (String keyword : keywordArray) {
            if (esService.isExistKeyword(keyword)) {
                list.add(keyword);
            }
        }
        logger.info("/existKeywords/{}  cost:{} ms", StringUtils.arrayToCommaDelimitedString(keywordArray), System.currentTimeMillis() - beginTime);
        return list;
    }


    @ApiOperation(value = "根据用户屏蔽的文章,存储用户不感兴趣的标签【新增，待实现！】",
            notes = "业务逻辑:  \n" +
                    "根据用户屏蔽的文章，得到用户不感兴趣的标签，将此标签存入Redis的hateTag:{userId}中。")
    @GetMapping(value = "/article/not_interested")
    public Object setNotInerestedTagsByInfoId(
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "InfoId") Long infoId) {
        //TODO
        return null;
    }


}
