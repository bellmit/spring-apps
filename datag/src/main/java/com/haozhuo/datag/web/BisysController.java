package com.haozhuo.datag.web;

import com.haozhuo.datag.common.ResultCodeBase;
import com.haozhuo.datag.common.TipConstBase;
import com.haozhuo.datag.model.ResponseEntity;
import com.haozhuo.datag.model.bisys.*;
import com.haozhuo.datag.service.BisysJdbcService;
import com.haozhuo.datag.service.UserBehaviorService;
import com.haozhuo.datag.service.WeChat.WeChatService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by Lucius on 9/3/18.
 */
@RequestMapping(value = "/bisys")
@RestController
public class BisysController {
    private static final Logger logger = LoggerFactory.getLogger(BisysController.class);
    @Autowired
    private WeChatService weChatService;
    @Autowired
    private BisysJdbcService bisysJdbcService;
    @Autowired
    private UserBehaviorService userBehaviorService;
    private static final String uploadInfoNotes = "tableId -> 1:app-优健康App数据, 2:app-注册用户数据, 3:检后组-键管服务, 4:检后组-绿通, 5:检后组-美维口腔,6:体检组-体检渠道 8:实物组-实物交易 9:检后组-基因检测";

    private static final String  healthCheckNotes =  "注释: date:日期, orderNum:订单总数, payOrderNum:成交笔数, " +
            "payOrderAmount:成交金额, refundWinNum:退款笔数, refundWinAmount:退款金额, payUseNum:用户数, payProfitAmount:成交成本, " +
            "refundSuccessAmount:退款成本, orderPrice:订单单价, perCustomerTransaction:客单价, 毛利润:grossProfit, " +
            "毛利润率:grossProfitRate, actualPayAmount:实收金额, actualProfit:实际利润, actualProfitRate:实际利润率";

    private static final String kindNotes = "注释：渠道:channelType; 支付笔数:payNum; 支付金额: payAmount; 用户数:userNum; 客单价:price; " +
            "成本:cost; 毛利润:profit; 利润率:profitRate; 退款笔数:refundNum; 退款金额:refundAmount; 实际营收:totalFee;";


    private static final String smsNotes = "factorySmsNum:体检中心数量,oneSmsNum:短信数量,oneSmsRegisterNum:短信注册量,oldUserNum:老用户数," +
            "oneRate:转化率,oneSmsCost:用户成本,twoSmsNum:二次短信,twoSmsRegisterNum:二次注册,twoRate:二次转化, twoSmsCost:二次成本," +
            "smsRegisterNum:汇总注册,totalRate:总体转化,totalCost:总体成本";

    @GetMapping("/ProdRiskEvaluation")
    @ApiOperation(value = "直接调用接口", notes = "业务：查询prod_risk_evaluation的点击首页_风险评估的次数(PV，累加)")
    public Object getProdRiskEvaluation() {
        return bisysJdbcService.getProdRiskEvaluation();
    }


    @GetMapping("/dailyReport/jhz/opsMallOrder")
    @ApiOperation(value = "各个报表", notes = "id = 1:电话解读; 2:解读复购; 3:电话问诊; 4:深度解读; 5:专项解读; 6:一元听听;" +
            " 7:uplus会员; 8:高血糖风险评估; 9:冠心病风险评估; 10:键管服务; 11:绿通; 12:美维口腔; 13:基因检测; 14:健康问答    15.公众号数据  \n" +
            " 返回结果的字段注释: date: 日期; orderNum:下单笔数; orderAmount:下单金额; payOrderNum: 支付笔数; payOrderAmount: 支付金额;" +
            " applyRefundOrderNum: 申请退款笔数; applyRefundOrderAmount: 申请退款金额; refundOrderNum: 提款成功笔数; " +
            "refundOrderAmount: 退款成功金额; grossProfit: 毛利润; grossProfitRate: 毛利率; refundRate: 退款率; refundGrossProfit: 退款毛利" +
            " PayConversionRate: 支付转化率\n     ")
    public Object getOpsMallOrder(@RequestParam(value = "id") int id,
                                  @RequestParam(value = "date") String date,
                                  @RequestParam(value = "endDate", defaultValue = "null") String endDate
    ) {
        if ("null".equals(endDate))
            endDate = date;
        if(id==14){
            return bisysJdbcService.getQuestion(id,date,endDate);
        }else if(id==15){
            return weChatService.getWechatDate(id,date,endDate);
        }else {
            return bisysJdbcService.getOpsMallOrder(id, date, endDate);
        }
    }

    @GetMapping("/dailyReport/jhz/buBuBao")
    @ApiOperation(value = "步步保", notes = "返回结果的字段注释: date: 日期; bannerNum: banner UV; clickNum: 授权页UV; policyNum: 回传的保单数; policyRate: 保单转化率;")
    public Object getBuBuBao(@RequestParam(value = "date") String date,
                             @RequestParam(value = "endDate", defaultValue = "null") String endDate
    ) {
        if ("null".equals(endDate))
            endDate = date;
        return bisysJdbcService.getBuBuBao(date, endDate);
    }

    @GetMapping("/dailyReport/jhz/uplusGoods")
    @ApiOperation(value = "Uplus会员-消费订单", notes = "返回结果的字段注释: goods_name:uplus会员商品名称; order_num:下单笔数; order_amount:下单金额")
    public Object getUplusGoods(@RequestParam(value = "date") String date,
                                @RequestParam(value = "endDate", defaultValue = "null") String endDate) {
        return bisysJdbcService.getUplusGoods();
    }

    @GetMapping("/dailyReport/jhz/uplusStat")
    @ApiOperation(value = "Uplus会员-消费统计", notes = "返回结果的字段注释: order_num:下单笔数; order_amount:下单金额")
    public Object getUplusStat(@RequestParam(value = "date") String date,
                               @RequestParam(value = "endDate", defaultValue = "null") String endDate) {
        if ("null".equals(endDate))
            endDate = date;
        return bisysJdbcService.getUplusStat(date, endDate);
    }

    @PostMapping("/dailyReport/jhz/add/mallOrderInput")
    @ApiOperation(value = "手动填写的表单：id -> 10:键管服务; 11:绿通; 12:美维口腔 13:基因检测")
//    public Object addMallOrderInput(@RequestBody OpsMallOrder mallOrder) {
    public Object addMallOrderInput(@RequestBody OpsMallOrderListParam mallOrders) throws Exception {
        bisysJdbcService.updateMallOrderInput(mallOrders);
        return "success!";
    }

    @GetMapping("/dailyReport/tjz/healthCheck")
    @ApiOperation(value = "体检渠道数据", notes = healthCheckNotes)
    public Object getHealthCheck(
            @RequestParam(value = "isTotal", defaultValue = "false") boolean isTotal,
            @RequestParam(value = "date") String date,
            @RequestParam(value = "endDate", defaultValue = "null") String endDate
    ) {
        if ("null".equals(endDate))
            endDate = date;
        return bisysJdbcService.getHealthCheck(isTotal, date, endDate);
    }

    @PostMapping("/dailyReport/tjz/add/HealthCheckFromWeChat")
    @ApiOperation(value = "添加体检渠道统计(微信)", notes = "{\"date\": \"2019-10-10\", \"orderNum\": 0, \"payOrderNum\": 10, \"payOrderAmount\": 0, \"refundWinNum\": 0, \"refundWinAmount\": 0, \"payUseNum\": 10, \"payProfitAmount\": 10,\"refundSuccessAmount\": 0,\"upload_time\":\"2019-01-01 11:11:11\", \"operate_account\":\"123\"}  ;  " + healthCheckNotes)
//    public Object addHealthCheckFromWeChat(@RequestBody HealthCheck healthCheck) {
    public Object addHealthCheckFromWeChat(@RequestBody HealthCheckListParam healthChecks) {
        bisysJdbcService.updateServiceTransactionWeChat(healthChecks);
        return "success!";
    }

    @GetMapping("/dailyReport/app/sms")
    @ApiOperation(value = "短信效果统计", notes = smsNotes)
    public Object getSms(@RequestParam(value = "date") String date,
                         @RequestParam(value = "endDate", defaultValue = "null") String endDate) {
        if ("null".equals(endDate))
            endDate = date;
        return bisysJdbcService.getSms(date, endDate);
    }

    @GetMapping("/dailyReport/app/YouApp")
    @ApiOperation(value = "优健康App数据统计", notes = "os：平台. 合计:0, Android:1, iOS:2; downloadUsers：下载用户(打开);  totalDownloadUsers：累计下载用户; activeUsers：活跃用户; startNum：启动次数")
    public Object getYouApp(@RequestParam(value = "date") String date,
                            @RequestParam(value = "endDate", defaultValue = "null") String endDate) {
        if ("null".equals(endDate))
            endDate = date;
        return bisysJdbcService.getYouApp(date, endDate);
    }

    @PostMapping("/dailyReport/app/add/YouApp")
    @ApiOperation(value = "添加优健康App数据", notes = "os：平台. 合计:0, Android:1, iOS:2; downloadUsers：下载用户(打开);  totalDownloadUsers：累计下载用户; activeUsers：活跃用户; startNum：启动次数")
//    public Object addYouApp(@RequestBody YouApp youApp) {
    public Object addYouApp(@RequestBody YouAppListParam youApps) throws Exception {
        return bisysJdbcService.updateYouApp(youApps);
    }

    @GetMapping("/dailyReport/app/register")
    @ApiOperation(value = "注册用户数据", notes = "downloadUsers:下载用户合计,totalDownloadUsers:累计下载用户,registerUsers:注册用户,totalRegisterUsers:累计注册用户, downloadUnregister:下载未注册,activeUsers:活跃用户,startNum:启动次数;")
    public Object getRegister(@RequestParam(value = "date") String date,
                              @RequestParam(value = "endDate", defaultValue = "null") String endDate) {
        if ("null".equals(endDate))
            endDate = date;
        return bisysJdbcService.getRegister(date, endDate);
    }

    @GetMapping("/dailyReport/app/userRetention")
    @ApiOperation(value = "优健康App用户留存率")
    public Object getUserRetention(@RequestParam(value = "date") String date,
                                   @RequestParam(value = "endDate", defaultValue = "null") String endDate) {
        if ("null".equals(endDate))
            endDate = date;
        return bisysJdbcService.getUserRetention(date, endDate);
    }

    @GetMapping("/dailyReport/app/accessData")
    @ApiOperation(value = "优知相关访问数据")
    public Object getAccessData(@RequestParam(value = "date") String date,
                                @RequestParam(value = "endDate", defaultValue = "null") String endDate) {
        if ("null".equals(endDate))
            endDate = date;
        return bisysJdbcService.getAccessData(date, endDate);
    }

    @GetMapping("/dailyReport/app/smsCity")
    @ApiOperation(value = "城市短信注册明细", notes = smsNotes)
    public Object getSmsCity() {
        return bisysJdbcService.getSmsCity();
    }

    @PostMapping("/dailyReport/app/add/register")
    @ApiOperation(value = "添加注册用户数据", notes = " 下载用户合计:downloadUsers, 累计下载用户:totalDownloadUsers, 注册用户:registerUsers, 累计注册用户:totalRegisterUsers, 下载未注册:downloadUnregister, 活跃用户:activeUsers, 启动次数:startNum;")
//    public Object setRegister(@RequestBody Register register) {
    public Object setRegister(@RequestBody RegisterListParam registers) {
        bisysJdbcService.updateRegister(registers);
        return "success!";
    }

    @GetMapping("/dailyReport/swz/kindOrder")
    @ApiOperation(value = "实物交易数据", notes = kindNotes)
    public Object getKindOrder(@RequestParam(value = "date") String date,
                               @RequestParam(value = "endDate", defaultValue = "null") String endDate) {
        if ("null".equals(endDate))
            endDate = date;
        return bisysJdbcService.getKindOrder(date, endDate);
    }

    @GetMapping("/dailyReport/swz/kindGoods")
    @ApiOperation(value = "当日商品交易明细")
    public Object getKindGoods() {
        return bisysJdbcService.getKindGoods();
    }

    @PostMapping("/dailyReport/swz/add/kindOrder")
    @ApiOperation(value = "添加实物交易数据(微信)",
            notes = "channelType 无需填" + kindNotes)
//    public Object addKindOrder(@RequestBody KindOrder kindOrder) {
    public Object addKindOrder(@RequestBody KindOrderListParam kindOrders) {
        bisysJdbcService.updateKindOrderWeChat(kindOrders);
        return "success!";
    }

//    @GetMapping("/dailyReport/swz/page/kindOrder")
//    @ApiOperation(value = "实物交易数据数据分页", notes="flag = 微信 : 0, APP : 1")
//    public Object getWeChatKindOrderPage(
//            @RequestParam(value = "pageNo") int pageNo,
//            @RequestParam(value = "pageSize") int pageSize,
//            @RequestParam(value = "flag ", defaultValue = "0") int flag) {
//        return bisysJdbcService.getKindOrderPage(pageNo, pageSize, flag);
//    }
//
//    @GetMapping("/dailyReport/tjz/page/healthCheck")
//    @ApiOperation(value = "体检渠道数据分页")
//    public Object getHealthCheckPage(
//            @RequestParam(value = "pageNo") int pageNo,
//            @RequestParam(value = "pageSize") int pageSize) {
//        return bisysJdbcService.getHealthCheckPage(pageNo, pageSize);
//    }
//
//    @GetMapping("/dailyReport/jhz/page/mallOrderInput")
//    @ApiOperation(value = "键管服务,绿通,美维口腔分页")
//    public Object getMallOrderPage(@RequestParam(value = "pageNo") int pageNo,
//                                   @RequestParam(value = "pageSize") int pageSize) {
//        return bisysJdbcService.getMallOrderInputPage(pageNo, pageSize);
//    }
//
//    @GetMapping("/dailyReport/app/page/register")
//    @ApiOperation(value = "注册用户数据分页")
//    public Object getRegisterPage(@RequestParam(value = "pageNo") int pageNo,
//                                  @RequestParam(value = "pageSize") int pageSize) {
//        return bisysJdbcService.getRegisterPage(pageNo, pageSize);
//    }
//
//    @GetMapping("/dailyReport/app/page/YouApp")
//    @ApiOperation(value = "优健康App数据统计分页")
//    public Object getYouAppPage(@RequestParam(value = "pageNo") int pageNo,
//                                @RequestParam(value = "pageSize") int pageSize) {
//        return bisysJdbcService.getYouAppPage(pageNo, pageSize);
//    }

    @PostMapping("/dailyReport/add/uploadInfo")
    @ApiOperation(value = "java后台数据录入记录", notes = uploadInfoNotes)
    public Object setUploadInfo(@RequestBody UploadInfo uploadInfo) {
        bisysJdbcService.updateUploadInfo(uploadInfo);
        return "success!";
    }

    @GetMapping("/dailyReport/page/uploadInfo")
    @ApiOperation(value = "java后台数据录入记录分页", notes = uploadInfoNotes)
    public Object getUploadInfoPage(@RequestParam(value = "pageNo") int pageNo,
                                @RequestParam(value = "pageSize") int pageSize,
                                @RequestParam(value = "ids") String ids
                                ) {
        return bisysJdbcService.getUploadInfoPage(pageNo, pageSize, ids);
    }
   @PostMapping(value = "/bi/userBehavior/upload")
   @ApiOperation(value = "码上检bi埋点上传接口")
    public ResponseEntity<Void> uploadUserBehavior(@Valid @RequestBody UserBehaviorDTO body) {
        userBehaviorService.save(body);
        return new ResponseEntity<>(ResultCodeBase.CODE_SUCCESS, TipConstBase.OPERATION_SAVE_SUCCESS);
    }
    @PostMapping(value = "/bi/blood/click")
    @ApiOperation(value = "血液按钮埋点次数统计")
    public ResponseEntity<Void> saveClick(@RequestParam(value = "click") int click) {
        //if()
        bisysJdbcService.saveClick(click);
        return new ResponseEntity<>(ResultCodeBase.CODE_SUCCESS, TipConstBase.OPERATION_SAVE_SUCCESS);
    }


/*    @GetMapping(value = "/bi/blood/getclick")
    @ApiOperation(value = "血液按钮埋点次数统计")
    public Object getClick(@RequestParam(value = "click") int click) {
        return bisysJdbcService.getClick(click);
    }*/

}


