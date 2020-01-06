package com.haozhuo.datag.model.bisys;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeapReport {
    int id;
    String date;
    //下单笔数
    int orderNum;
    //下单金额
    double orderAmount;
    //支付笔数
    int payOrderNum;
    //支付金额
    double payOrderAmount;
    //申请退款笔数
    int applyRefundOrderNum;
    //申请退款金额
    double applyRefundOrderAmount;
    //退款完成笔数
    int refundOrderNum;
    //退款完成金额
    double refundOrderAmount;

    public DeapReport(int id, String date, int orderNum, double orderAmount, int payOrderNum, double payOrderAmount, int applyRefundOrderNum, double applyRefundOrderAmount, int refundOrderNum, double refundOrderAmount) {
        this.id = id;
        this.date = date;
        this.orderNum = orderNum;
        this.orderAmount = orderAmount;
        this.payOrderNum = payOrderNum;
        this.payOrderAmount = payOrderAmount;
        this.applyRefundOrderNum = applyRefundOrderNum;
        this.applyRefundOrderAmount = applyRefundOrderAmount;
        this.refundOrderNum = refundOrderNum;
        this.refundOrderAmount = refundOrderAmount;
    }
}
