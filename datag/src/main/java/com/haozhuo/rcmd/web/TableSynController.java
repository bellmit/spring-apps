package com.haozhuo.rcmd.web;

import com.haozhuo.rcmd.model.Goods;
import com.haozhuo.rcmd.model.LiveInfo;
import com.haozhuo.rcmd.model.Video;
import com.haozhuo.rcmd.service.EsService;
import com.haozhuo.rcmd.service.JdbcService;
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
    private JdbcService jdbcService;

    /**
     * 对应原来developer-api项目中的InsertEntityController中的
     */
    @PostMapping("/video")
    @ApiOperation(value = "视频更新接口【/insertAll】",
            notes = "http://192.168.1.152:8085/swagger-ui.html#!/insert-entity-controller/insertAllUsingPOST")
    public String videoUpdate(@RequestBody Video video) {
        long beginTime = System.currentTimeMillis();
//        videoInfo.setLabelIds(jdbcService.getLabelIdsByNames(videoInfo.getLabels()));
//        jdbcService.updateVideo(videoInfo);
//        esService.updateVideo(videoInfo);
       // logger.info("POST /video  videoId:{}  cost:{} ms", videoInfo.getId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @DeleteMapping("/video/{id}")
    @ApiOperation(value = "视频删除接口")
    public Object videoDelete(@PathVariable(value = "id") long id) {
        long beginTime = System.currentTimeMillis();
        jdbcService.deleteVideo(id);
       // esService.deleteVideo(id);
        logger.info("DELETE /video/{} cost:{} ms", id, System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @PostMapping("/live")
    @ApiOperation(value = "直播更新接口【/insertAll】", notes = "http://192.168.1.152:8085/swagger-ui.html#!/insert-entity-controller/insertAllUsingPOST")
    public Object liveUpdate(@RequestBody LiveInfo liveInfo) {
        long beginTime = System.currentTimeMillis();
        liveInfo.setLabelIds(jdbcService.getLabelIdsByNames(liveInfo.getLabels()));
        jdbcService.updateLive(liveInfo);
        esService.updateLive(liveInfo);
        logger.info("POST /live  goodsId:{}  cost:{} ms", liveInfo.getId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @DeleteMapping("/live/{id}")
    @ApiOperation(value = "直播删除接口")
    public Object liveDelete(@PathVariable(value = "id") String id) {
        long beginTime = System.currentTimeMillis();
        jdbcService.deleteLive(Integer.parseInt(id));
        esService.deleteLive(id);
        logger.info("DELETE /live/{} cost:{} ms", id, System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @PostMapping("/goods")
    @ApiOperation(value = "商品更新接口【/insertAll】", notes = "http://192.168.1.152:8085/swagger-ui.html#!/insert-entity-controller/insertAllUsingPOST")
    public Object goodsUpdate(@RequestBody Goods goods) {
        long beginTime = System.currentTimeMillis();
        esService.updateGoods(goods);
        logger.info("POST /goods  goodsId:{}  cost:{} ms",goods.getContentId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @DeleteMapping("/goods/{id}")
    @ApiOperation(value = "商品删除接口")
    public Object goodDelete(@PathVariable(value = "id") String id) {
        long beginTime = System.currentTimeMillis();
        esService.deleteGoods(id);
        logger.info("DELETE /goods/{} cost:{} ms", id, System.currentTimeMillis() - beginTime);
        return "success!";
    }


    @PostMapping("/article")
    @ApiOperation(value = "资讯更新接口")
    public Object articleUpdate(@RequestBody Goods goods) {
        long beginTime = System.currentTimeMillis();
        esService.updateGoods(goods);
        logger.info("POST /article  id:{}  cost:{} ms",goods.getContentId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @DeleteMapping("/article/{id}")
    @ApiOperation(value = "资讯删除接口")
    public Object articleDelete(@PathVariable(value = "id") String id) {
        long beginTime = System.currentTimeMillis();
        esService.deleteGoods(id);
        logger.info("DELETE /article/{} cost:{} ms", id, System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @PostMapping("/channel")
    @ApiOperation(value = "频道更新接口")
    public Object channelUpdate(@RequestBody Goods goods) {
        long beginTime = System.currentTimeMillis();
        esService.updateGoods(goods);
        logger.info("POST /channel  id:{}  cost:{} ms",goods.getContentId(), System.currentTimeMillis() - beginTime);
        return "success!";
    }

    @DeleteMapping("/channel/{id}")
    @ApiOperation(value = "频道删除接口")
    public Object channelDelete(@PathVariable(value = "id") String id) {
        long beginTime = System.currentTimeMillis();
        esService.deleteGoods(id);
        logger.info("DELETE /channel/{} cost:{} ms", id, System.currentTimeMillis() - beginTime);
        return "success!";
    }
}
