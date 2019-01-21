package com.haozhuo.datag.model.bisys;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haozhuo.datag.common.JavaUtils;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Created by Lucius on 1/17/19.
 */

@AllArgsConstructor
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
    int applyOrderNum;
    //申请退款金额
    double applyOrderAmount;
    //退款完成笔数
    int refundOrderNum;
    //退款完成金额
    double refundOrderAmount;

    public OpsMallOrder(int id) {
        this.id = id;
    }

    //毛利润率
    public Double getGrossProfit() {
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
        if (payOrderNum == 0) {
            return 0D;
        } else {
            return JavaUtils.retainDecimal(getGrossProfit() / payOrderAmount,4);
        }
    }

    //支付转化率
    public Double getConversionRateOfPayment() {
        if (orderNum == 0) {
            return 0D;
        } else {
            return JavaUtils.retainDecimal(Double.valueOf(payOrderNum) / orderNum,2);
        }

    }

    //退款率
    public Double getRefundRate() {
        if (payOrderNum == 0) {
            return 0D;
        } else {
            return JavaUtils.retainDecimal(Double.valueOf(refundOrderNum) / payOrderNum,4);
        }
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

    public int getApplyOrderNum() {
        return applyOrderNum;
    }

    public void setApplyOrderNum(int applyOrderNum) {
        this.applyOrderNum = applyOrderNum;
    }

    public double getApplyOrderAmount() {
        return applyOrderAmount;
    }

    public void setApplyOrderAmount(double applyOrderAmount) {
        this.applyOrderAmount = applyOrderAmount;
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
}
