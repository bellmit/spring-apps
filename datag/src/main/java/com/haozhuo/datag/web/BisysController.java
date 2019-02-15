package com.haozhuo.datag.web;

import com.haozhuo.datag.model.Live;
import com.haozhuo.datag.model.bisys.OpsMallOrder;
import com.haozhuo.datag.service.BisysJdbcService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Lucius on 9/3/18.
 */
@RequestMapping(value = "/bisys")
@RestController
public class BisysController {
    private static final Logger logger = LoggerFactory.getLogger(BisysController.class);

    @Autowired
    private BisysJdbcService bisysJdbcService;

    @GetMapping("/ProdRiskEvaluation")
    @ApiOperation(value = "直接调用接口", notes = "业务：查询prod_risk_evaluation的点击首页_风险评估的次数(PV，累加)")
    public Object getProdRiskEvaluation() {
        return bisysJdbcService.getProdRiskEvaluation();
    }


    @GetMapping("/dailyReport/opsMallOrder")
    @ApiOperation(value = "各个报表", notes = "id = 1:电话解读; 2:解读复购; 3:电话问诊; 4:深度解读; 5:专项解读; 6:一元听听;" +
            " 7:uplus会员; 8:高血糖风险评估; 9:冠心病风险评估; 10:键管服务; 11:绿通; 12:美维口腔     \n" +
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
        return bisysJdbcService.getOpsMallOrder(id, date, endDate);
    }

    @GetMapping("/dailyReport/buBuBao")
    @ApiOperation(value = "步步保", notes = "返回结果的字段注释: date: 日期; bannerNum: banner UV; clickNum: 授权页UV; policyNum: 回传的保单数; policyRate: 保单转化率;")
    public Object getBuBuBao(@RequestParam(value = "date") String date,
                             @RequestParam(value = "endDate", defaultValue = "null") String endDate
    ) {
        if ("null".equals(endDate))
            endDate = date;
        return bisysJdbcService.getBuBuBao(date, endDate);
    }

    @GetMapping("/dailyReport/healthCheck")
    @ApiOperation(value = "体检渠道数据", notes = "返回结果的字段注释: date:日期, orderNum:订单总数, payOrderNum:成交笔数, " +
            "payOrderAmount:成交金额, refundWinNum:退款笔数, refundWinAmount:退款金额, payUseNum:用户数, payProfitAmount:成交成本, " +
            "refundSuccessAmount:退款成本, orderPrice:订单单价, perCustomerTransaction:客单价, 毛利润:grossProfit, " +
            "毛利润率:grossProfitRate, actualPayAmount:实收金额, actualProfit:实际利润, actualProfitRate:实际利润率")
    public Object getHealthCheck(
            @RequestParam(value = "isTotal",defaultValue = "false") boolean isTotal,
            @RequestParam(value = "date") String date,
            @RequestParam(value = "endDate", defaultValue = "null") String endDate
    ) {
        if ("null".equals(endDate))
            endDate = date;
        return bisysJdbcService.getHealthCheck(isTotal, date, endDate);
    }

    @GetMapping("/dailyReport/uplusGoods")
    @ApiOperation(value = "Uplus会员-消费订单", notes = "返回结果的字段注释: goods_name:uplus会员商品名称; order_num:下单笔数; order_amount:下单金额")
    public Object getUplusGoods() {
        return bisysJdbcService.getUplusGoods();
    }


    @PostMapping("/dailyReport/mallOrderInput")
    @ApiOperation(value = "手动填写的表单：id -> 10:键管服务; 11:绿通; 12:美维口腔")
    public Object mallOrderInput(@RequestBody OpsMallOrder mallOrder) {
        long beginTime = System.currentTimeMillis();
        bisysJdbcService.updateMallOrderInput(mallOrder);
        return mallOrder;
    }
}


