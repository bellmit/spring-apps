package com.haozhuo.datag.model.bisys;

import com.haozhuo.datag.common.JavaUtils;

/**
 * Created by Lucius on 2/15/19.
 */
public class HealthCheck {
    private String date;
    private String src;
    //订单总数
    private int orderNum;
    //退款笔数
    private int refundWinNum;
    //退款金额
    private double refundWinAmount;
    //成交成本
    private double payProfitAmount;
    //退款成本
    private double refundSuccessAmount;
    //成交笔数
    private int payOrderNum;
    //成交金额
    private double payOrderAmount;
    //用户数
    private int payUseNum;

    private String uploadTime;

    private String operateAccount;

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getOperateAccount() {
        return operateAccount;
    }

    public void setOperateAccount(String operateAccount) {
        this.operateAccount = operateAccount;
    }

    public int getPayOrderNum() {
        return payOrderNum;
    }

    public void setPayOrderNum(int payOrderNum) {
        this.payOrderNum = payOrderNum;
    }

    public double getPayOrderAmount() {
        return payOrderAmount;
    }

    public void setPayOrderAmount(double payOrderAmount) {
        this.payOrderAmount = payOrderAmount;
    }

    public int getPayUseNum() {
        return payUseNum;
    }

    public void setPayUseNum(int payUseNum) {
        this.payUseNum = payUseNum;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    public int getRefundWinNum() {
        return refundWinNum;
    }

    public void setRefundWinNum(int refundWinNum) {
        this.refundWinNum = refundWinNum;
    }

    public double getRefundWinAmount() {
        return refundWinAmount;
    }

    public void setRefundWinAmount(double refundWinAmount) {
        this.refundWinAmount = refundWinAmount;
    }

    public double getPayProfitAmount() {
        return payProfitAmount;
    }

    public void setPayProfitAmount(double payProfitAmount) {
        this.payProfitAmount = payProfitAmount;
    }

    public double getRefundSuccessAmount() {
        return refundSuccessAmount;
    }

    public void setRefundSuccessAmount(double refundSuccessAmount) {
        this.refundSuccessAmount = refundSuccessAmount;
    }

    //订单单价 = 成交金额 / 成交笔数
    public double getOrderPrice() {
        if (payOrderNum == 0) {
            return 0;
        }
        return JavaUtils.retainDecimal(payOrderAmount / payOrderNum,2);
    }

    //客单价 = 成交金额 / 用户数
    public double getPerCustomerTransaction() {
        if (payUseNum == 0) {
            return 0;
        }
        return JavaUtils.retainDecimal(payOrderAmount / payUseNum,2);
    }

    //毛利润 = 成交金额-成交成本
    public double getGrossProfit() {
        return JavaUtils.retainDecimal(payOrderAmount - payProfitAmount,2);
    }

    //毛利润率 = 毛利润/成交金额
    public double getGrossProfitRate() {
        if (payProfitAmount == 0) {
            return 0;
        }
        return JavaUtils.retainDecimal(getGrossProfit() / payProfitAmount,4);
    }

    //实收金额 = 成交金额 - 退款金额
    public double getActualPayAmount() {
        return JavaUtils.retainDecimal(payOrderAmount - refundWinAmount,2);
    }

    //实际利润 = 毛利润 - 退款成本
    public double getActualProfit() {
        return JavaUtils.retainDecimal(getGrossProfit() - refundSuccessAmount, 2);
    }

    //实际利润率 = 实际利润 / 实收金额
    public double getActualProfitRate() {
        double actualPayAmount = getActualPayAmount();
        if (actualPayAmount == 0) {
            return 0;
        }
        return JavaUtils.retainDecimal(getActualProfit() / actualPayAmount,4);
    }

}
