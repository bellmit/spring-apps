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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
     * 对应原来recommender项目中的EsMatcherController中的enter(),getMatchContent()方法。
     * 这两个方法合并成一个
     *
     * @param params
     * @return
     */
    @PostMapping("/info/list")
    @ApiOperation(value = "获取资讯推荐列表")
    public Object infoList(@RequestBody InfoListParams params) {
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
        logger.debug("infoList: cost {} ms", (System.currentTimeMillis() - beginTime) / 1000);

        return rcmdInfo;
    }

    /**
     * 首页商品推荐列表接口
     * 对应原来good-recommender项目中的GoodsRecomController中的getMatchContent()方法。
     *
     * @param params
     * @return
     */
    @PostMapping("/goods/home/list")
    @ApiOperation(value = "首页商品推荐匹配相应的列表", notes = "输入userId,返回内容列表")
    public Object goodsHomeList(@RequestBody InfoListParams params) {
        return null;
    }

    /**
     * 报告解读商品推荐列表接口
     * 对应原来good-recommender项目中的GoodsRecomController中的getMatchContent()方法。
     *
     * @param params
     * @return
     */
    @PostMapping("/goods/report_parse/list")
    @ApiOperation(value = "报告解读商品推荐匹配相应的列表", notes = "输入healthReportId,返回内容列表")
    public Object goodsReportParseList(@RequestBody InfoListParams params) {
        return null;
    }

    @PostMapping("/video/list")
    public Object videoList(@RequestBody InfoListParams params) {
        return null;
    }

}
