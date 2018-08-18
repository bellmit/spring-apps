package com.haozhuo.rcmd.web;

import com.haozhuo.rcmd.config.kafka.KafkaProducer;
import com.haozhuo.rcmd.model.InfoListParams;
import com.haozhuo.rcmd.model.RcmdInfo;
import com.haozhuo.rcmd.model.RcmdInfoMsg;
import com.haozhuo.rcmd.service.JdbcService;
import com.haozhuo.rcmd.service.RedisService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * Created by Lucius on 8/16/18.
 */

@RequestMapping(value = "/rcmd")
@RestController
public class RcmdController {
    private static final Logger logger = LoggerFactory.getLogger(RcmdController.class);
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
     *      recommender项目中的EsMatcherController中的enter(),getMatchContent()方法。这两个方法合并成一个。
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
     *
     * @param params
     * @return
     */
    @PostMapping("/goods/home")
    @ApiOperation(value = "首页商品推荐匹配相应的列表", notes = "输入userId,返回内容列表")
    public Object getGoodsHomeList(@RequestBody InfoListParams params) {
        return null;
    }

    /**
     * 报告解读商品推荐列表接口
     * 对应原来good-recommender项目中的GoodsRecomController中的getMatchContent()方法。
     *
     * @param params
     * @return
     */
    @PostMapping("/goods/report-parse")
    @ApiOperation(value = "报告解读商品推荐匹配相应的列表", notes = "输入healthReportId,返回内容列表")
    public Object getGoodsReportParseList(@RequestBody InfoListParams params) {
        return null;
    }

    /**
     * 推荐视频列表
     * 旧接口：
     *      video-recommender项目中的VideoRecomController中的getSearchContent()方法getMatchContent()方法
            生产环境输入：
             curl -XPOST -H "Content-Type: application/json" http://datanode2:9090/api/video-recommder/videomatch/recommend/all -d '{
             "userId":"c63fc45c-35d1-43d5-b864-2bdb82542dfd",
             "size":10
             }'
            返回：
            ["575","591","588","339","311","303","309","317","324","331"]
     * 新接口：
     * 注意：旧接口中有一个flag参数，并没有用到，所以新接口中删除
     * @param userId
     * @param pageSize
     * @return
     */
    @PostMapping("/video/userid")
    @ApiOperation(value = "匹配相应的推荐视频列表", notes = "输入对应的userid,返回推荐视频列表")
    public Object getVideoListByUserId(
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "size", defaultValue = "10") int pageSize) {
        Set<String> alreadyPushedVideos = redisService.getPushedVideos(userId);
        Set<String> userLabels = jdbcService.getLabelsByUserId(userId);
        //TODO 从mysql查找age和sex
        return userLabels;
    }


    /**
     * 搜索视频列表
     * 旧接口：
     *      video-recommender项目中的VideoRecomController中的getSearchContent()方法
     *      //TODO 对该接口有疑问
     *
     * 新接口：
     * @param keywords
     * @return
     */
    @PostMapping("/video/search")
    @ApiOperation(value = "匹配相应的搜素视频列表", notes = "输入搜索关键词，输出对应的符合视频title的搜素结果")
    public Object getVideoListBySerarch(@RequestParam(value = "keyword", defaultValue = "") String keywords) {
        return null;
    }


    @PostMapping("/video/similarity")
    @ApiOperation(value = "匹配相应的推荐视频列表", notes = "输入对于的userid,返回推荐视频列表")
    public Object getVideoListBySimilarity(@RequestBody InfoListParams params) {
        return null;
    }

}
