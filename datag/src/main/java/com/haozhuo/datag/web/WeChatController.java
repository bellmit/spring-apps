package com.haozhuo.datag.web;

import com.haozhuo.datag.model.wechat.DownloadNum;
import com.haozhuo.datag.model.wechat.GetUserCumulate;
import com.haozhuo.datag.model.wechat.GetUserSummary;
import com.haozhuo.datag.model.wechat.SaoMa;
import com.haozhuo.datag.service.WeChat.WeChatService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = "/wx")
@RestController
public class WeChatController {
    @Value("${wechat.mp.appID:wx5356e6bdfb9676d0}")
    private String appID;

    @Value("${wechat.mp.secret:54ac5c85eac3ec65fda5120c952150f9}")
    private String secret;
    @Autowired
    private WeChatService weChatService;


    @GetMapping("/wechat/saoma")
    @ApiOperation("扫码数")
    public List<SaoMa> getNum(@RequestParam(value="date") String date){

        return weChatService.getSaomaNum(date);
    }
    @GetMapping("/wechat/data")
    @ApiOperation("加载数据到数据库")
    public void getData(@RequestParam(value="date") String date,
                        @RequestParam(value="enddate") String enddate){

        weChatService.saveData(date, enddate);
    }
    @GetMapping
    @ApiOperation(value="获取下载数目")
    public List<DownloadNum> getDownloadNum(@RequestParam(value="date") String date,
                                            @RequestParam(value="enddate") String enddate){

        return weChatService.getDownlodeNum(date,enddate);
    }
/*    @GetMapping("/wechat/token")
    @ApiOperation(value = "获取token")
    public AccessToken getToken() {

        return weChatService.getAccess_token();
    }*/

    @PostMapping("/wechat/getusersummary")
    public List<GetUserSummary> getUserSummary(@RequestParam(value = "date") String begindate,
                                               @RequestParam(value = "endDate") String enddate) {


        return weChatService.getuserSummary(begindate, enddate);
    }

    @PostMapping("/wechat/getusercumulate")
    public List<GetUserCumulate> getUserCumulate(@RequestParam(value = "date") String begindate,
                                                 @RequestParam(value = "endDate") String enddate) {


        return weChatService.getUserCumulateList(begindate, enddate);
    }

}
