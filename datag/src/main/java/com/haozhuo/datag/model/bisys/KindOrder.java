package com.haozhuo.datag.model.bisys;

import lombok.Getter;
import lombok.Setter;
/**
 * Created by Lucius on 2/21/19.
 */
@Setter
@Getter
public class KindOrder {
    private String date;
    //渠道
    private String channelType;
    //支付笔数
    private int payNum;
    //支付金额
    private double payAmount;
    //用户数
    private int userNum;
    //客单价
    private double price;
    //成本
    private double cost;
    //毛利润
    private double profit;
    //利润率
    private double profitRate;
    //退款笔数
    private double refundNum;
    //退款金额
    private double refundAmount;
    //实际营收
    private double totalFee;

    //上传时间
    private String uploadTime;
    //操作账号
    private String operateAccount;

}
