package com.haozhuo.datag.web;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.model.*;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.DataEtlJdbcService;
import com.haozhuo.datag.service.RedisService;
import com.haozhuo.datag.model.textspilt.SimpleArticle;
import com.haozhuo.datag.model.textspilt.TFIDF;
import com.haozhuo.datag.service.YjkMallJdbcService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Created by Lucius on 8/22/18.
 */

@SuppressWarnings({"SameReturnValue", "WeakerAccess"})
@RequestMapping(value = "/table-syn")
@RestController
public class TableSynController {
    private static final Logger logger = LoggerFactory.getLogger(TableSynController.class);
    @Autowired
    private EsService esService;
    @Autowired
    private DataEtlJdbcService dataetlJdbcService;

    @Autowired
    private YjkMallJdbcService yjkMallJdbcService;

    @Autowired
    private RedisService redisService;

    @Value("${app.biz.admin-password:321}")
    private String adminPassword;
    /**
     * 对应原来developer-api项目中的InsertEntityController中的
     */

    @PostMapping("/video")
    @ApiOperation(value = "视频更新接口【/insertAll】",
            notes = "http://192.168.1.152:8085/swagger-ui.html#!/insert-entity-controller/insertAllUsingPOST")
    public String updateVideo(@RequestBody Video video) {
        long beginTime = System.currentTimeMillis();
        if (video.getStatus() != 1) { //只有2种状态1和0。1表示发布，0表示删除。如果传入的是其他的状态，表示删除，设置为0
            video.setStatus(0);
        }
        video.setUpdateTime(JavaUtils.getCurrent());
        dataetlJdbcService.updateVideo(video);
        if (video.getStatus() == 1) { //只有状态是1的才认为是发布的视频。其他一切状态都认为是删除
            esService.updateVideo(video);
        } else {
            esService.deleteVideo(video.getId());
        }
        logger.info("POST /video  videoId:{}  cost:{} ms", video.getId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }


    @PostMapping("/article")
    @ApiOperation(value = "资讯更新接口")
    public Object updateArticle(@RequestBody Article article) {

        article.setUpdateTime(JavaUtils.getCurrent());
        long beginTime = System.currentTimeMillis();
        if (article.getStatus() != 1) { //只有2种状态1和0。1表示发布，0表示删除。如果传入的是其他的状态，表示删除，设置为0
            article.setStatus(0);
        }
        article.setUpdateTime(JavaUtils.getCurrent());

        // must delete first!
        redisService.deleteKeywordsOfArticleInRedis(String.valueOf(article.getInformationId()));

        dataetlJdbcService.updateArticle(article);
        if (article.getStatus() == 1) { //只有状态是1的才认为是发布的文章。其他一切状态都认为是删除
            esService.updateArticle(article);
            SimpleArticle simpleArticle = getSimpleArticle(article);
            redisService.setKeywordsOfArticleInRedis(simpleArticle);
            dataetlJdbcService.updateKeywordsOfArticle(simpleArticle);
        } else {
            esService.deleteArticle(article.getInformationId());
            // already deleted in the redis!
        }
        logger.info("POST /article  id:{}  cost:{} ms", article.getInformationId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }

    /**
     *
     * @param article 只更新redis
     * @return
     */
    @PostMapping("/articleredis")
    @ApiOperation(value = "资讯更新接口")
    public Object updateArticleForredis(@RequestBody Article article) {

        article.setUpdateTime(JavaUtils.getCurrent());
        long beginTime = System.currentTimeMillis();
        if (article.getStatus() != 1) { //只有2种状态1和0。1表示发布，0表示删除。如果传入的是其他的状态，表示删除，设置为0
            article.setStatus(0);
        }
        article.setUpdateTime(JavaUtils.getCurrent());

        // must delete first!
        redisService.deleteKeywordsOfArticleInRedis(String.valueOf(article.getInformationId()));

        //dataetlJdbcService.updateArticle(article);
        if (article.getStatus() == 1) { //只有状态是1的才认为是发布的文章。其他一切状态都认为是删除
            esService.updateArticle(article);
            SimpleArticle simpleArticle = getSimpleArticle(article);
            redisService.setKeywordsOfArticleInRedis(simpleArticle);
            //dataetlJdbcService.updateKeywordsOfArticle(simpleArticle);
        } else {
            esService.deleteArticle(article.getInformationId());
            // already deleted in the redis!
        }
        logger.info("POST /article  id:{}  cost:{} ms", article.getInformationId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }

    // 与离线程序统一
    private SimpleArticle getSimpleArticle(Article article) {
        SimpleArticle simpleArticle = new SimpleArticle();
        simpleArticle.setChannelId(String.valueOf(article.getChannelId()));
        simpleArticle.setInformationId(String.valueOf(article.getInformationId()));
        simpleArticle.setKeywords(TFIDF.getMyKeywords(article.getTitle(), article.getContent()));
        return simpleArticle;
    }

    @DeleteMapping("/article/{id}")
    @ApiOperation(value = "资讯删除接口", notes = "mysql中的article4表中的status字段设置为0,ES中删除article4中的该文档")
    public Object deleteArticle(@PathVariable(value = "id") long id) {
        long beginTime = System.currentTimeMillis();
        dataetlJdbcService.deleteArticle(id);
        esService.deleteArticle(id);
        redisService.deleteKeywordsOfArticleInRedis(String.valueOf(id));
        logger.info("DELETE /article/{} cost:{} ms", id, System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @PostMapping("/channel")
    @ApiOperation(value = "频道更新接口", notes = "同步到mysql的表中")
    public Object updateChannel(@RequestBody Channel channel) {
        long beginTime = System.currentTimeMillis();
        dataetlJdbcService.updateChannel(channel);
        logger.info("POST /channel  id:{}  cost:{} ms", channel.getChannelId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @DeleteMapping("/channel/{id}")
    @ApiOperation(value = "频道删除接口", notes = "从mysql的表中删除")
    public Object deleteChannel(@PathVariable(value = "id") long id) {
        long beginTime = System.currentTimeMillis();
        dataetlJdbcService.deleteChannel(id);
        logger.info("DELETE /channel/{} cost:{} ms", id, System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @PostMapping("/live")
    @ApiOperation(value = "直播更新接口【/insertAll】", notes = "http://192.168.1.152:8085/swagger-ui.html#!/insert-entity-controller/insertAllUsingPOST")
    public Object updateLive(@RequestBody Live liveInfo) {
        long beginTime = System.currentTimeMillis();
        //liveInfo.setLabelIds(dataetlJdbcService.getLabelIdsByNames(liveInfo.getLabels()));
        dataetlJdbcService.updateLive(liveInfo);
        esService.updateLive(liveInfo);
        logger.info("POST /live  id:{}  cost:{} ms", liveInfo.getId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @DeleteMapping("/video/{id}")
    @ApiOperation(value = "视频删除接口")
    public Object deleteVideo(@PathVariable(value = "id") long id) {
        long beginTime = System.currentTimeMillis();
        dataetlJdbcService.deleteVideo(id);
        esService.deleteVideo(id);
        logger.info("DELETE /video/{} cost:{} ms", id, System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @DeleteMapping("/live/{id}")
    @ApiOperation(value = "直播删除接口")
    public Object deleteLive(@PathVariable(value = "id") long id) {
        long beginTime = System.currentTimeMillis();
        dataetlJdbcService.deleteLive(id);
        esService.deleteLive(id);
        logger.info("DELETE /live/{} cost:{} ms", id, System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @PostMapping("/goods")
    @ApiOperation(value = "商品更新接口【/insertAll】", notes = "rcmdScore和salesNum这两个字段 java组不需要传过来，由数据组自己同步维护")
    public Object updateGoods(@RequestBody Goods goods) {
        long beginTime = System.currentTimeMillis();
        //推荐得分全程由数据端维护
        try {
            Goods existGoods = esService.getGoodsBySkuId(goods.getSkuId());

            if (goods.getRcmdScore() <= 0) {
                if (existGoods == null) {
                    goods.setRcmdScore(Goods.SCORE_DEFAULT);
                } else {
                    goods.setRcmdScore(existGoods.getRcmdScore());
                }
            }
            if (goods.getSalesNum() <= 0) {
                if (existGoods == null) {
                    goods.setSalesNum(0);
                } else {
                    goods.setSalesNum(existGoods.getSalesNum());
                }
            }

            esService.updateGoods(goods);
            dataetlJdbcService.updateGoods(goods);
            logger.info("POST /goods skuId:{}  cost:{} ms", goods.getSkuId(), System.currentTimeMillis() - beginTime);
        } catch (Exception ex) {
            logger.error("goods update error:", ex);
            return "failed!";
        }
        return "success!";
    }


    @DeleteMapping("/goods/{skuId}")
    @ApiOperation(value = "商品删除接口")
    public Object deleteGoods(@PathVariable(value = "skuId") String skuId) {
        long beginTime = System.currentTimeMillis();
        esService.deleteGoods(skuId);
        dataetlJdbcService.deleteGoods(skuId);
        logger.info("DELETE /goods/{} cost:{} ms", skuId, System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @GetMapping("/goods/updateScore")
    @ApiOperation(value = "更新商品推荐权重")
    public Object updateGoodsScore(@RequestParam(value = "skuId") String skuId, @RequestParam(value = "rcmdScore") int rcmdScore) {
        Goods goods = esService.getGoodsBySkuId(skuId);
        if (goods != null) {
            goods.setRcmdScore(rcmdScore);
            esService.updateGoods(goods);
            dataetlJdbcService.updateGoods(goods);
        }
        return "success!";
    }

    @GetMapping("/goods/synSaleNum/{password}")
    @ApiOperation(value = "同步商品销量", notes = "该接口用于每天定时同步更新商品的销量。开销大，不能随便调用。只有项目开发者才知道密码，才能调用。")
    public Object synGoodsSaleNum(@PathVariable(value = "password") String password) {
        if (adminPassword.equals(password)) {
            int from = 0;
            int size = 100;
            List<Goods> goodsList;
            do {
                goodsList = dataetlJdbcService.getGoodsList(from, size);
                from = from + size;
                for (Goods goods : goodsList) {
                    int salesNum = yjkMallJdbcService.getGoodsSaleNum(goods.getGoodsIds());
                    goods.setSalesNum(salesNum);
                    esService.updateGoods(goods);
                    dataetlJdbcService.updateGoods(goods);
                }
            } while (goodsList.size() == size);
            return "同步完成";
        } else {
            return "密码错误!";
        }
    }


    @GetMapping("/goods/updateScoresByLikeStr")
    @ApiOperation(value = "根据字符匹配批量更新商品推荐权重", notes = "只有项目开发者才知道密码，才能调用。")
    public Object updateGoodsScoresByLikeStr(
            @RequestParam(value = "password") String password,
            @RequestParam(value = "likeStr") String likeStr,
            @RequestParam(value = "rcmdScore") int rcmdScore,
            @RequestParam(value = "field", defaultValue = "name") String field) {
        List<String> ids = dataetlJdbcService.getGoodsSkuIdsByLikeStr(field, likeStr);
        if (adminPassword.equals(password)) {
            for (String id : ids) {
                updateGoodsScore(id, rcmdScore);
            }
            return ids;
        } else {
            return "密码错误!";
        }
    }
}

