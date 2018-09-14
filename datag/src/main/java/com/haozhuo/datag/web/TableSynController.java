package com.haozhuo.datag.web;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.model.*;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.DataetlJdbcService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * Created by Lucius on 8/22/18.
 */
@RequestMapping(value = "/table-syn")
@RestController
public class TableSynController {
    private static final Logger logger = LoggerFactory.getLogger(TableSynController.class);
    @Autowired
    private EsService esService;
    @Autowired
    private DataetlJdbcService dataetlJdbcService;

    /**
     * 对应原来developer-api项目中的InsertEntityController中的
     */
    @PostMapping("/video")
    @ApiOperation(value = "视频更新接口【/insertAll】",
            notes = "http://192.168.1.152:8085/swagger-ui.html#!/insert-entity-controller/insertAllUsingPOST")
    public String updateVideo(@RequestBody Video video) {
        long beginTime = System.currentTimeMillis();
        if(video.getStatus()!=1){ //只有2种状态1和0。1表示发布，0表示删除。如果传入的是其他的状态，表示删除，设置为0
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

    @DeleteMapping("/video/{id}")
    @ApiOperation(value = "视频删除接口")
    public Object deleteVideo(@PathVariable(value = "id") long id) {
        long beginTime = System.currentTimeMillis();
        dataetlJdbcService.deleteVideo(id);
        esService.deleteVideo(id);
        logger.info("DELETE /video/{} cost:{} ms", id, System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @PostMapping("/article")
    @ApiOperation(value = "资讯更新接口")
    public Object updateArticle(@RequestBody Article article) {

        article.setUpdateTime(JavaUtils.getCurrent());
        long beginTime = System.currentTimeMillis();
        if(article.getStatus()!=1){ //只有2种状态1和0。1表示发布，0表示删除。如果传入的是其他的状态，表示删除，设置为0
            article.setStatus(0);
        }
        article.setUpdateTime(JavaUtils.getCurrent());
        dataetlJdbcService.updateArticle(article);
        if (article.getStatus() == 1) { //只有状态是1的才认为是发布的文章。其他一切状态都认为是删除
            esService.updateArticle(article);
        } else {
            esService.deleteArticle(article.getInformationId());
        }
        logger.info("POST /article  id:{}  cost:{} ms", article.getInformationId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @DeleteMapping("/article/{id}")
    @ApiOperation(value = "资讯删除接口", notes = "mysql中的article4表中的status字段设置为0,ES中删除article4中的该文档")
    public Object deleteArticle(@PathVariable(value = "id") long id) {
        long beginTime = System.currentTimeMillis();
        dataetlJdbcService.deleteArticle(id);
        esService.deleteArticle(id);

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
    public Object updateLive(@RequestBody LiveInfo liveInfo) {
        long beginTime = System.currentTimeMillis();
        liveInfo.setLabelIds(dataetlJdbcService.getLabelIdsByNames(liveInfo.getLabels()));
        dataetlJdbcService.updateLive(liveInfo);
        esService.updateLive(liveInfo);
        logger.info("POST /live  goodsId:{}  cost:{} ms", liveInfo.getId(), System.currentTimeMillis() - beginTime);
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
    @ApiOperation(value = "商品更新接口【/insertAll】", notes = "http://192.168.1.152:8085/swagger-ui.html#!/insert-entity-controller/insertAllUsingPOST")
    public Object updateGoods(@RequestBody Goods goods) {
        long beginTime = System.currentTimeMillis();
        esService.updateGoods(goods);
        logger.info("POST /goods  goodsId:{}  cost:{} ms", goods.getContentId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @DeleteMapping("/goods/{id}")
    @ApiOperation(value = "商品删除接口")
    public Object deleteGoods(@PathVariable(value = "id") long id) {
        long beginTime = System.currentTimeMillis();
        esService.deleteGoods(id);
        logger.info("DELETE /goods/{} cost:{} ms", id, System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @PostMapping("/infoHeat")
    @ApiOperation(value = "资讯热度接口")
    public Object updateInfoHeat(@RequestBody InfoHeat infoHeat) {
        long beginTime = System.currentTimeMillis();
        esService.updateInfoHeat(infoHeat);
        logger.info("POST /infoHeat  Id:{}  cost:{} ms", infoHeat.getInfoId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }
}
