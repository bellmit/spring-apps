package com.haozhuo.rcmd.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haozhuo.rcmd.config.kafka.KafkaProducer;
import com.haozhuo.rcmd.model.AbnormalParam;
import com.haozhuo.rcmd.model.ArticleInfo;
import com.haozhuo.rcmd.model.RcmdInfo;
import com.haozhuo.rcmd.model.RcmdRequestMsg;
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
    private KafkaProducer kafkaProducer;

    private JdbcService jdbcService;

    private final Map<String, Integer> categoryNameIdMap;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public RcmdController(JdbcService jdbcService) {
        this.jdbcService = jdbcService;
        this.categoryNameIdMap = this.jdbcService.getCategoryNameIdMap();
    }

    /**
     * 输入userId,返回推荐商品的id
     * 对应原来good-recommender项目中的GoodsRecomController中的getMatchContent()方法。
     * */
    @GetMapping("/goods/userId")
    @ApiOperation(value = "输入userId,返回推荐商品的id  【/goodsmatch/list/all】", notes = "输入userId,返回推荐商品的id。\n原接口: http://192.168.1.152:8087/swagger-ui.html#!/goods-recom-controller/getMatchContentUsingPOST")
    public Object getGoodsIdsByUserId(
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "size", defaultValue = "10") int pageSize) {
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
        return result;
    }

    /**
     * 根据
     * 对应原来good-recommender项目中的GoodsRecomController中的getRecomList()方法。
     * curl -X POST --header "ArticleInfo-Type: application/json" --header  "http://192.168.1.152:8087/goodsmatch/getRecom/list?healthReportId=2515473&pageSize=10&pageNum=1"
     * @return
     */
    @GetMapping("/goods/reportId")
    @ApiOperation(value = "根据报告Id返回推荐的商品  【/goodsmatch/getRecom/list】", notes = "根据报告Id返回推荐的商品。\n原接口: http://192.168.1.152:8087/swagger-ui.html#!/goods-recom-controller/getRecomListUsingPOST")
    public Object getGoodsIdsByReportId(
            @RequestParam(value = "healthReportId") String healthReportId,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum) {
        String labels = esService.getLabelsByReportId(healthReportId);
        if ("".equals(labels))
            return null;
        int from = (pageNum - 1) * pageSize;
        return esService.getGoodsIdsByLabels(labels, from, pageSize);
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
     * @param pageSize
     * @return
     */
    @GetMapping("/video/userId")
    @ApiOperation(value = "根据userId,匹配推荐的视频列表  【/videomatch/recommend/all】", notes = "输入对应的userId,返回推荐视频列表。 \n原接口: http://192.168.1.152:8089/swagger-ui.html#!/video-recom-controller/getMatchContentUsingPOST")
    public Object getVideoListByUserId(
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "size", defaultValue = "10") int pageSize) {
        String[] alreadyPushedVideos = redisService.getPushedVideos(userId);
        logger.debug("alreadyPushedVideos:{}", StringUtils.arrayToCommaDelimitedString(alreadyPushedVideos));
        String userLabels = jdbcService.getLabelStrByUserId(userId);

        //最后需要把查到的结果存入Redis的已推荐的key中
        String[] result = esService.getVideoIdsByLabel(userLabels, alreadyPushedVideos, pageSize);
        redisService.setPushedVideos(userId, result);
        //如果返回的数据小于pageSize,则认为所有的视频都被推荐了，那么将Redis中推过的视频列表的key删除，使得所有视频可以重新推送
        if (result.length < pageSize) {
            redisService.deleteVideoPushedKey(userId);
        }
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
    @GetMapping("/video/keyword")
    @ApiOperation(value = "根据关键词返回视频列表  【/videomatch/search/all】", notes = "根据输入的关键词，和视频的标题进行匹配，得到相应的视频id列表。\n原接口: http://192.168.1.152:8089/swagger-ui.html#!/video-recom-controller/getSearchContentUsingPOST")
    public Object getVideoListByKeyword(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return esService.getVideoIdsByTitle(keyword, size);
    }


    /**
     * 旧的接口：
     * curl -X POST --header "ArticleInfo-Type: application/json"  "http://datanode2:9090/api/video-recommder/videomatch/relative/all?vid=570"
     */
    @GetMapping("/video/videoId")
    @ApiOperation(value = "根据videoId返回与它相似的视频列表  【/videomatch/relative/all】", notes = "根据videoId返回与它相似的视频列表。\n原接口: http://192.168.1.152:8089/swagger-ui.html#!/video-recom-controller/getRelativeContentUsingPOST")
    public Object getVideoListBySimilarity(
            @RequestParam(value = "videoId") String videoId,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        String videoLabels = jdbcService.getVideoLabelsById(videoId);
        logger.debug("videoId:{}, videoLabels:{}", videoId, videoLabels);
        return esService.getSimilarVideoIds(videoId, videoLabels, size);
    }


    /**
     * 根据userId获取用户的标签
     * 旧的接口，
     * 对应developerApi中GetUserLabelController中的getMatchContent()方法
     * 测试环境：http://47.98.165.120:8085/get?userId=00007d91-fefe-4234-bc08-2496ea8360c6
     */
    @ApiOperation(value = "根据userId获取用户的标签  【/get】", notes = "原接口: http://192.168.1.152:8085/swagger-ui.html#!/get-user-label-controller/getMatchContentUsingGET")
    @GetMapping(value = "/labels/userId")
    public String getLablesByUserId(@RequestParam(value = "userId") String userId) {
        return jdbcService.getLabelStrByUserId(userId);
    }

    /**
     * 根据healthReportId返回报告的标签
     * 旧的接口，
     * 对应developerApi中GetUserLabelController中的getBasicInfoByReport()方法
     * 测试环境：http://192.168.1.152:8085/getByReport?healthReportId=1
     */
    @ApiOperation(value = "根据healthReportId返回报告的标签  【/getByReport】", notes = "原接口: http://192.168.1.152:8085/swagger-ui.html#!/get-user-label-controller/getBasicInfoByReportUsingGET")
    @GetMapping(value = "/labels/reportId")
    public String getLablesByReportId(@RequestParam(value = "healthReportId") String healthReportId) {
        return esService.getLabelsByReportId(healthReportId);
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
    @RequestMapping(value = "/mul/ALVG/infoId", method = RequestMethod.GET)
    public Object getMulAlvgByInfoId(@RequestParam(value = "infoId") String infoId) {
        //资讯、视频、直播的结果
        Map<String, List<String>> map = redisService.getSimByInfoId(infoId);
        //商品的结果
        String labels = jdbcService.getLabelNameByInfoId(infoId);
        String[] goodsIds = esService.getGoodsIdsByLabels(labels, 0, 10);
        map.put("g", Arrays.asList(goodsIds));
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
        return result;
    }

    /**
     * 根据userId获取资讯、视频、直播的推荐列表
     * 旧接口：
     * recommender项目中的EsMatcherController中的enter(),getMatchContent()方法。这两个方法合并成一个。
     *
     * @return
     */
    @GetMapping("/mul/ALV/userId")
    @ApiOperation(value = "根据userId获取资讯、视频、直播的推荐列表  【/list/current/all,/list/current/scroll/all,...】", notes = "原接口: http://192.168.1.152:8078/swagger-ui.html#/\n es-matcher-controller中的四个方法合并成一个")
    public Object getInfoList(
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "size", defaultValue = "10") int pageSize,
            @RequestParam(value = "contentType", defaultValue = "推荐") String categoryName) {
        long beginTime = System.currentTimeMillis();
        int rmcdType = categoryNameIdMap.getOrDefault(categoryName, 1);
        kafkaProducer.sendRcmdRequestMsg(new RcmdRequestMsg(userId, rmcdType));
        //目前size不管传入多少，返回都是10条。因为flink-data-etl的推荐中固定每次产生10条
        RcmdInfo rcmdInfo = redisService.getRcmdInfo(userId, rmcdType);
        int compSize = pageSize - rcmdInfo.size();
        //如果上述推荐的结果小于默认的推荐结果，则进行补充
        if (compSize > 0) {
            rcmdInfo.add(jdbcService.getRandomInfos(rmcdType, pageSize, compSize));
        }
        logger.debug("getInfoList: cost {} ms", (System.currentTimeMillis() - beginTime) / 1000);
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
        String[] liveIds = esService.getLivesIdsByLabel(labels + keywords, 1);
        String[] videoIds = esService.getVideoIdsByLabel(labels + keywords, new String[]{excludeVideoId}, 3 - liveIds.length);
        String[] articleIds = esService.getArticleIdsByLabel(labels + keywords, size - liveIds.length - videoIds.length);
        List<String> result = new ArrayList<>();
        result.addAll(addTypeForIds(Arrays.asList(videoIds), "video"));
        result.addAll(addTypeForIds(Arrays.asList(liveIds), "live"));
        result.addAll(addTypeForIds(Arrays.asList(articleIds), "article"));
        return result;
    }


    /**
     * 旧的接口：
     * abnormal-recommder的ArticleRecomController中的getRecomByKwList()方法
     * 新的接口如下：
     */
    @ApiOperation(value = "根据关键词推荐资讯、视频、直播  【/getRecom/reportHelp】", notes = "原接口: http://192.168.1.152:8002/swagger-ui.html#!/article-recom-controller/getRecomByKwListUsingPOST")
    @GetMapping(value = "/mul/ALV/keywordArray")
    public Object getMulAlvByKeywords(
            @RequestParam(value = "pageSize", defaultValue = "20") int size,
            @RequestParam(value = "keyword") String[] keywordArray) {
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
        return esService.getArticleIdsByAbnormStr(abnormialStr, abnormialAliasStr, size, true);
    }


    /**
     * 根据标签返回文章
     * 旧的接口，
     * 对应developerApi中GetUserLabelController中的getMatchList()方法
     * 测试环境：curl -X GET --header  "http://47.98.165.120:8085/get/list?label=%E9%98%B4%E9%81%93%E7%82%8E&pageSize=10"
     * 返回：
     * [
     * {
     * "contentId": "258634",
     * "title": "近视爸爸+近视妈妈=近视宝宝？",
     * "abstracts": "众所周知，近视是世界上最常见的眼病之一，中国又是近视大国之一。目前研究认为，近视是一种多因子调控、多细胞参与的复杂眼病，其发生发展受遗传和环境的共同影响。",
     * "content": "父母近视会不会影响宝宝？作者｜琳琅来源｜医学界妇产科频道目前研究认为，近视是一种多因子调控、多细胞参与的复杂眼病，其发生发展受遗传和环境的共同影响，遗传因素起着稳定、持久的作用，特别是对高度近视的学生。1父母近视对孩子的影响多大？近视是属于多因子遗传病，一个个体是否易于患某种疾病是由遗传因素和环境因素的共同作用决定的，这种不易于或易于患某种疾病的属性变量称为易患性；遗传对易患性所起作用的大小称为遗传度。调查结果显示，近视程度越高，遗传度越高，说明遗传因素在近视发病中起的作用越重要。Stoodley等研究发现处于相同环境因素条件下，父母患近视的人发生近视的可能性是父母为非近视的10倍。Jones等研究表明，遗传是视力低下重要因素之一，青少年视力低下与父母近视有关。父母一方或双方有近视，其子女发生近视的危险性较父母没有近视的倍数高；有近视家族史的学生比无家族史的学生在相同时间平均增加的近视屈光度高。还有研究表明：父母双方均是高度近视眼（一般指600度以上），遗传给宝宝的近视几率非常大。若其中一方高度近视，其遗传的几率可降低一半左右。如果父母均是低度近视，遗传的几率要小得多。另外，排除后天环境的原因，3~6 岁的宝宝出现高度近视，多与遗传有关，属于常染色体隐性遗传病。 当然，如果父母亲都不是近视眼，但他们都是高度近视基因携带者，在他们本人不显示近视。但他们俩的致病基因遗传给孩子，使孩子具备了两个近视基因，故而会使孩子成为近视眼。 因此我们要注意的是，如果爸爸妈妈都有高度近视的，应意识到宝宝会有遗传近视眼的倾向。在日常生活中，应多注意观察宝宝和同龄的孩子在视觉上的差异。观察宝宝看东西时，眼睛是否会眯起来，是否会歪头，是否会将脸靠近物体等。如果出现这样的情况，应尽早带宝宝去眼科检查。2个人行为对近视影响更重要！遗传因素虽是近视眼发生发展过程中的生物学前提，但是遗传因素稳定且无法改变、视觉环境大致确定的情况下，个人行为因素在青少年近视的发生发展过程中显得尤为重要，起到了很大的影响作用。3预防宝宝近视，要做到以下几点！1. 在饮食方面要均衡，不挑食、不偏食。多吃富含维生素 A、B1、B2、C及 E的食品。常见富含维生素的食品有蛋、奶、鱼、肉、肝脏和新鲜的蔬菜、水果。2. 多带宝宝到户外运动，少看电视。目前，环境因素造成的近视已远超过遗传近视的比例。 3. 保持一个良好的姿势“三个一” ：眼睛与书本距离一尺，胸与桌子距离一拳，手握笔时和笔尖距离一寸。这是一个标准的阅读和写字姿势，可以避免距离书本太近。4. 避免长时间看近。避免让宝宝看手机、看 iPad的时间过长，眼睛得不到放松，眼内的肌肉，特别是调节睫状肌得不到松弛，最后就近视了或助推近视的发展。5. 定期带宝宝到医院进行体检。若发现宝宝已经近视了，要及时配戴眼镜，保证宝宝的正常用眼。 [1] Stoodley CJ,Stein JF.Cerebellar function in delelopmentalDyslexia[J].The Cerebellum,2013,12(2):267-368.[2] Jones LA,Sinnott LT,Mutti DO,et al.Parental history of myopia,sports and outdoor activities,and future myopia[J].Invest Ophthalmol VisSci,2007,48(8)：3524-3532.[3] 王婷婷，木尼热·热孜 等.伊宁市中小学生近视遗传度分析.中国儿童保健杂志，2017.08（25）：834-836.",
     * "date": "2018-06-18 23:35:03",
     * "score": 9.981064796447754,
     * "contentType": "慢病"
     * },
     * {
     * "contentId": "155180",
     * "title": "高度近视和普通近视，是两码事！",
     * "abstracts": "600度以上就是高度近视，这时候考虑的就不仅是度数上的量变，而需要关心眼底发生病变情况，一些眼部并发症甚至可能会致盲！所以，高度近视并不能单纯地只当做近视看待，而需要给予高度重视！然而，这还是有很大的个体差异！",
     * "content": "600度以上就是高度近视，这时候考虑的就不仅是度数上的量变，而需要关心眼底发生病变情况，一些眼部并发症甚至可能会致盲！所以，高度近视并不能单纯地只当做近视看待，而需要给予高度重视！然而，这还是有很大的个体差异！600度并不是很严格的风水岭，有些人超过600度也不会并发眼底病，有些人没有600度反而患上眼底病。无论视力好坏，关心眼健康都是每个人必须做的命题，只是到了600度就要特别注意眼底的情况。因为，大多数人的近视属于轴性近视：这类患者的眼球前后径变长，就像乒乓球被拉成了鸡蛋的形状。通常，近视度数越高，眼球被「拉」得越长，发生眼底病变的可能就越大。由于眼球被「拉长」，眼球壁的各层组织都会相应变薄、萎缩。尤其是视网膜变薄后，就容易使得视网膜周边部发生变性带，产生裂孔，还会进一步导致视网膜脱离；视网膜最中心的黄斑区，也可能发生裂孔，或者出血等各种病理改变，这时就会严重影响视力。这些病变都可以致盲。有些可以通过及时进行激光或手术来防治，有些却没有很好的治疗办法，视力可能会变得很差，戴眼镜也不管用。另外，除了眼底视网膜病变，高度近视的人也容易伴有晶状体脱位、白内障、开角型青光眼等问题。因为高度近视随时有眼底病变的危险，和普通的近视相比，同时也意味着这些东西基本就你绝缘：1. 剧烈运动比如跳水、蹦极、拳击、跳高、游乐园里一些剧烈摇晃的游戏，高度近视就算了！至于篮球、足球、羽毛球等，是可以进行的，但是一定要保护好眼睛，别受外伤。2.顺产3.献血献血时血压会轻微波动，正常情况下对人的眼球供血影响不大。可对于高度近视者（近视度数大于800度）眼球长轴变长，脉络膜被拉得更薄更细，轻微的血压波动对于他们可能会造成血管痉挛，引起眼底的变化，从而影响视力。因此，从保护献血者的角度出发不建议高度近视患者献血。高度近视并发眼底病的防治1)避免眼球的外伤及剧烈爆发性运动2)平时多补充适量的维生素A、C、E。3)至少每半年到眼科作眼底散瞳检查，了解眼部情况!4)高度近视眼的视力低下，难以矫正，戴完全矫正的眼镜又往往不能耐受，所以配镜时应低度矫正，争取视力有些提高而又能保持舒适为宜，无须一味追求最好的矫正视力。高度近视眼配戴角膜接触镜矫正视力比普通眼镜好，而且可以减少普通眼镜的三棱镜效应、视野受限等不适。5)高度近视眼的另一特点是病程呈进行性，发育期进展明显，成人后仍不停止。这主要由遗传因素决定，但后天的视觉环境、工作性质、生活习惯、全身健康、营养状况也起一定作用，所以，应比其他人更加注意用眼卫生。",
     * "date": "2018-01-05 15:59:41",
     * "score": 8.910865783691406,
     * "contentType": "慢病"
     * }
     * ]
     */
    @ApiOperation(value = "根据标签返回文章的详细内容  【/get/list】", notes = "根据标签返回文章的详细内容。\n原接口: http://192.168.1.152:8085/swagger-ui.html#!/get-user-label-controller/getMatchListUsingGET")
    @RequestMapping(value = "/detailsOfInfos/labels", method = RequestMethod.GET)
    public List<ArticleInfo> getMatchList(
            @RequestParam(value = "label") String labelName,
            @RequestParam(value = "pageSize", defaultValue = "10") int size) {
        //TODO 代码实现
        return null;
    }

}
