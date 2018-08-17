package com.haozhuo.datagservice.web;


import com.haozhuo.datagservice.model.Goods;
import com.haozhuo.datagservice.model.LiveInfo;
import com.haozhuo.datagservice.model.VideoInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * video、live、goods对应原来developer-api项目中的InsertEntityController中的insertAll()方法,只有这个方法有用，其他没用
 * 旧的接口：
 * 测试环境提交一个live(直播):
 *
 curl -XPOST -H "Content-Type: application/json" http://192.168.1.152:8085/insertAll -d '{
 "content_id":"1",
 "conetnt_name":"1",
 "first_label":"1",
 "second_label":"1",
 "label":"1",
 "basic_label":"1",
 "keyword":"1",
 "category":"水果",
 "sourceType":"live",
 "isPay":0,
 "playTime":"2017-11-11 11:11:11"
 }'
 */
@RestController
@RequestMapping(value = "/service/table-syn")
public class TableSynController {

    /**
     * 对应原来developer-api项目中的InsertEntityController中的
     * @param videoInfo
     * @return
     */
    @PostMapping("/video/update")
    @ApiOperation(value = "视频更新接口")
    public Object videoUpdate(@RequestBody VideoInfo videoInfo) {
        return null;
    }

    @PostMapping("/video/delete")
    @ApiOperation(value = "视频删除接口")
    public Object videoDelete(@RequestBody VideoInfo videoInfo) {
        return null;
    }

    @PostMapping("/live/update")
    @ApiOperation(value = "直播更新接口")
    public Object liveUpdate(@RequestBody LiveInfo liveInfo) {
        return null;
    }

    @PostMapping("/live/delete")
    @ApiOperation(value = "直播删除接口")
    public Object liveDelete(@RequestBody LiveInfo liveInfo) {
        return null;
    }


    @PostMapping("/goods/update")
    @ApiOperation(value = "商品更新接口")
    public Object goodsUpdate(@RequestBody Goods goods) {
        return null;
    }

    @PostMapping("/goods/delete")
    @ApiOperation(value = "商品删除接口")
    public Object goodDelete(@RequestBody Goods goods) {
        return null;
    }

}
