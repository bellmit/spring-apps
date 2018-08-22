package com.haozhuo.rcmd.web;

import com.haozhuo.rcmd.model.Goods;
import com.haozhuo.rcmd.model.LiveInfo;
import com.haozhuo.rcmd.model.VideoInfo;
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
    private static final Logger logger = LoggerFactory.getLogger(RcmdController.class);
    @Autowired
    private EsService esService;
    @Autowired
    private JdbcService jdbcService;

    /**
     * 对应原来developer-api项目中的InsertEntityController中的
     *
     * @param videoInfo
     * @return
     */
    @PostMapping("/video/update")
    @ApiOperation(value = "视频更新接口")
    public String videoUpdate(@RequestBody VideoInfo videoInfo) {
        videoInfo.setLabelIds(jdbcService.getLabelIdsByNames(videoInfo.getLabels()));
        jdbcService.updateVideo(videoInfo);
        esService.updateVideo(videoInfo);
        return "success!";
    }

    @GetMapping("/video/delete")
    @ApiOperation(value = "视频删除接口")
    public Object videoDelete(@RequestParam String id) {
        jdbcService.deleteVideo(Integer.parseInt(id));
        esService.deleteVideo(id);
        return "success!";
    }

    @PostMapping("/live/update")
    @ApiOperation(value = "直播更新接口")
    public Object liveUpdate(@RequestBody LiveInfo liveInfo) {
        liveInfo.setLabelIds(jdbcService.getLabelIdsByNames(liveInfo.getLabels()));
        jdbcService.updateLive(liveInfo);
        esService.updateLive(liveInfo);
        return "success!";
    }

    @GetMapping("/live/delete")
    @ApiOperation(value = "直播删除接口")
    public Object liveDelete(@RequestParam String id) {
        jdbcService.deleteLive(Integer.parseInt(id));
        esService.deleteLive(id);
        return "success!";
    }

    @PostMapping("/goods/update")
    @ApiOperation(value = "商品更新接口")
    public Object goodsUpdate(@RequestBody Goods goods) {
        esService.updateGoods(goods);
        return "success!";
    }

    @GetMapping("/goods/delete")
    @ApiOperation(value = "商品删除接口")
    public Object goodDelete(@RequestParam String id) {
        esService.deleteGoods(id);
        return "success!";
    }
}
