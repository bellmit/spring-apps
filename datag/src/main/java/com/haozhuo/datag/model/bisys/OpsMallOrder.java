package com.haozhuo.datag.model.bisys;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haozhuo.datag.common.JavaUtils;

import java.math.BigDecimal;

/**
 * Created by Lucius on 1/17/19.
 */


@JsonIgnoreProperties(ignoreUnknown = true)
public class OpsMallOrder {
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

    public OpsMallOrder(int id, String date, int orderNum, double orderAmount, int payOrderNum, double payOrderAmount, int applyRefundOrderNum, double applyRefundOrderAmount, int refundOrderNum, double refundOrderAmount) {
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

    public void setId(int id) {
        this.id = id;
    }

    double grossProfit = -1;
    double grossProfitRate = -1;
    double payConversionRate = -1;
    double refundRate = -1;
    double refundGrossProfit = -1;



    public void setRefundGrossProfit(double refundGrossProfit) {
        this.refundGrossProfit = refundGrossProfit;
    }

    public double getRefundGrossProfit() {
        return refundGrossProfit;
    }

    public void setGrossProfit(double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public void setGrossProfitRate(double grossProfitRate) {
        this.grossProfitRate = grossProfitRate;
    }

    public void setPayConversionRate(double payConversionRate) {
        this.payConversionRate = payConversionRate;
    }

    public void setRefundRate(double refundRate) {
        this.refundRate = refundRate;
    }

    public OpsMallOrder(int id) {
        this.id = id;
    }

    @JsonIgnore
    public boolean isInputMallOrder(){
        return isInputMallOrder(id);
    }

    public static boolean  isInputMallOrder(int id ) {
        return id >= 10 && id <= 12;
    }

    //毛利润
    public Double getGrossProfit() {

        if(isInputMallOrder() && grossProfit >= 0) {
            return  grossProfit;
        }

        double value = 0D;
        switch (id) {
            case 1:
            case 2:
                value = payOrderAmount - payOrderNum * 22;break;
            case 3:
                value = payOrderAmount * 0.3;break;
            case 4:
                value =payOrderAmount - payOrderNum * 5;break;
            case 5:
                value = payOrderAmount - payOrderNum * 15;break;
            case 6:
            case 7:
            case 8:
                value = payOrderAmount;break;
            case 9:
                if (payOrderNum > 0) {
                    value = payOrderAmount - 15;
                }
                break;
        }
        return JavaUtils.retainDecimal(value,2);
    }

    public double round(double value){
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    //毛利润率
    public Double getGrossProfitRate() {
        if(isInputMallOrder() && grossProfitRate >= 0) {
            return grossProfitRate;
        }

        if (payOrderNum == 0) {
            return 0D;
        } else {
            return JavaUtils.retainDecimal(getGrossProfit() / payOrderAmount,4);
        }
    }

    //支付转化率
    public Double getPayConversionRate() {
        if(isInputMallOrder() && payConversionRate >= 0) {
            return payConversionRate;
        }

        if (orderNum == 0) {
            return 0D;
        } else {
            return JavaUtils.retainDecimal(Double.valueOf(payOrderNum) / orderNum,2);
        }

    }

    //退款率
    public Double getRefundRate() {
        if(isInputMallOrder() && refundRate >= 0) {
            return  refundRate;
        }

        if (payOrderNum == 0) {
            return 0D;
        } else {
            return JavaUtils.retainDecimal(Double.valueOf(refundOrderNum) / payOrderNum,4);
        }
    }

    @JsonIgnore
    public String getGenre(){
        return getGenre(id);
    }

    public static String getGenre(int id) {
        switch (id) {
            case 1:
                return "电话解读";
            case 2:
                return "解读复购";
            case 3:
                return "电话问诊";
            case 4:
                return "深度解读";
            case 5:
                return "专项解读";
            case 6:
                return "一元听听";
            case 7:
                return "uplus会员";
            case 8:
                return "高血糖风险评估";
            case 9:
                return "冠心病";
            case 10:
                return "健管服务";
            case 11:
                return "绿通";
            case 12:
                return "美维口腔";
            default:
                return "";
        }
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

    public double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(double orderAmount) {
        this.orderAmount = orderAmount;
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

    public int getApplyRefundOrderNum() {
        return applyRefundOrderNum;
    }

    public void setApplyRefundOrderNum(int applyRefundOrderNum) {
        this.applyRefundOrderNum = applyRefundOrderNum;
    }

    public double getApplyRefundOrderAmount() {
        return applyRefundOrderAmount;
    }

    public void setApplyRefundOrderAmount(double applyRefundOrderAmount) {
        this.applyRefundOrderAmount = applyRefundOrderAmount;
    }

    public int getRefundOrderNum() {
        return refundOrderNum;
    }

    public void setRefundOrderNum(int refundOrderNum) {
        this.refundOrderNum = refundOrderNum;
    }

    public double getRefundOrderAmount() {
        return refundOrderAmount;
    }

    public void setRefundOrderAmount(double refundOrderAmount) {
        this.refundOrderAmount = refundOrderAmount;
    }

    public int getId() {
        return id;
    }
}
