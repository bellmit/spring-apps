package com.haozhuo.rcmd.web;

import com.haozhuo.rcmd.config.kafka.KafkaProducer;
import com.haozhuo.rcmd.model.InfoListParams;
import com.haozhuo.rcmd.model.RcmdInfo;
import com.haozhuo.rcmd.model.RcmdInfoMsg;
import com.haozhuo.rcmd.service.EsService;
import com.haozhuo.rcmd.service.JdbcService;
import com.haozhuo.rcmd.service.RedisService;
import io.swagger.annotations.ApiOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    @Value("${app.rcmd.info.default-rcmd-type:1}")
    private int defaultRcmdType;

    @Value("${app.rcmd.info.default-page-size:10}")
    private int defaultPageSize;

    @Autowired
    public RcmdController(JdbcService jdbcService) {
        this.jdbcService = jdbcService;
        this.categoryNameIdMap = this.jdbcService.getCategoryNameIdMap();
    }

    /**
     * 资讯推荐列表接口
     * 旧接口：
     * recommender项目中的EsMatcherController中的enter(),getMatchContent()方法。这两个方法合并成一个。
     *
     * @param params
     * @return
     */
    @PostMapping("/info")
    @ApiOperation(value = "获取资讯推荐列表")
    public Object getInfoList(@RequestBody InfoListParams params) {
        long beginTime = System.currentTimeMillis();
        int rmcdType = categoryNameIdMap.getOrDefault(params.getContentType(), defaultRcmdType);
        String userId = params.getUserId();
        kafkaProducer.sendRcmdInfoMsg(new RcmdInfoMsg(userId, rmcdType));
        //目前size不管传入多少，返回都是10条。因为flink-data-etl的推荐中固定每次产生10条
        int pageSize = params.getSize();
        RcmdInfo rcmdInfo = redisService.getRcmdInfo(userId, rmcdType);
        int compSize = defaultPageSize - rcmdInfo.size();
        //如果上述推荐的结果小于默认的推荐结果，则进行补充
        if (compSize > 0) {
            rcmdInfo.add(jdbcService.getRandomInfos(rmcdType, pageSize, compSize));
        }
        logger.debug("getInfoList: cost {} ms", (System.currentTimeMillis() - beginTime) / 1000);

        return rcmdInfo;
    }

    /**
     * 首页商品推荐列表接口
     * 对应原来good-recommender项目中的GoodsRecomController中的getMatchContent()方法。
     * curl -X POST --header "Content-Type: application/json" --header  "http://192.168.1.152:8087/goodsmatch/getRecom/list?healthReportId=2515473&pageSize=10&pageNum=1"
     */
    @GetMapping("/goods/userId")
    @ApiOperation(value = "首页商品推荐匹配相应的列表", notes = "输入userId,返回内容列表")
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
     * 对应原来good-recommender项目中的GoodsRecomController中的getMatchContent()方法。
     *
     * @param params
     * @return
     */
    @GetMapping("/goods/reportId")
    @ApiOperation(value = "报告解读商品推荐匹配相应的列表", notes = "输入healthReportId,返回内容列表")
    public Object getGoodsIdsByReportId(@RequestBody InfoListParams params) {
        return null;
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
    @ApiOperation(value = "根据infoId获取相似的资讯、视频、直播、商品", notes = "输入infoId,返回相似的资讯、视频、直播、商品")
    @RequestMapping(value = "/simAll/infoId", method = RequestMethod.GET)
    public Object getSimAllByInfoId(@RequestParam(value = "infoId") String infoId) {
        //资讯、视频、直播的结果
        Map<String, List<String>> map = redisService.getSimByInfoId(infoId);
        //商品的结果
        String labels = jdbcService.getLabelNameByInfoId(infoId);
        String[] goodsIds = esService.getGoodsIdsByLabels(labels, 10);
        map.put("g", Arrays.asList(goodsIds));
        return map;
    }

    /**
     * 推荐视频列表
     * 旧接口：
     * video-recommender项目中的VideoRecomController中的getMatchContent()方法
     * 生产环境输入：
     * curl -XPOST -H "Content-Type: application/json" http://datanode2:9090/api/video-recommder/videomatch/recommend/all -d '{
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
    @ApiOperation(value = "根据userId,匹配推荐的视频列表", notes = "输入对应的userId,返回推荐视频列表")
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
     * curl -X POST --header "Content-Type: application/json"  "http://datanode2:9090/api/video-recommder/videomatch/search/all?keyword=%E9%AB%98%E8%A1%80%E5%8E%8B"
     * <p>
     * 新接口：
     */
    @GetMapping("/video/search")
    @ApiOperation(value = "根据关键词返回视频列表", notes = "根据输入的关键词，和视频的标题进行匹配，得到相应的视频id列表")
    public Object getVideoListByTitle(
            @RequestParam(value = "title", defaultValue = "") String title,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return esService.getVideoIdsByTitle(title, size);
    }


    /**
     * 旧的接口：
     * curl -X POST --header "Content-Type: application/json"  "http://datanode2:9090/api/video-recommder/videomatch/relative/all?vid=570"
     */
    @GetMapping("/video/similarity")
    @ApiOperation(value = "根据video_id返回与它相似的视频列表", notes = "根据video_id返回与它相似的视频列表")
    public Object getVideoListBySimilarity(
            @RequestParam(value = "videoId") String videoId,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        String videoLabels = jdbcService.getVideoLabelsById(videoId);
        logger.debug("videoId:{}, videoLabels:{}", videoId, videoLabels);
        return esService.getSimilarVideoIds(videoId, videoLabels, size);
    }


}
