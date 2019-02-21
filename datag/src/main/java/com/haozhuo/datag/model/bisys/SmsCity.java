package com.haozhuo.datag.model.bisys;
import lombok.Setter;
import lombok.Getter;
/**
 * Created by Lucius on 2/20/19.
 */
@Setter
@Getter
public class SmsCity {
    //城市名称
    private String cityName;
    //体检中心数量
    private int factorySmsNum;
    //短信数量
    private int oneSmsNum;
    //短信注册量
    private int oneSmsRegisterNum;
    //老用户数
    private int oldUserNum;
    //转化率
    private double oneRate;
    //用户成本
    private double oneSmsCost;
    //二次短信
    private int twoSmsNum;
    //二次注册
    private int twoSmsRegisterNum;
    //二次转化
    private double twoRate;
    //二次成本
    private double twoSmsCost;
    //汇总注册
    private double smsRegisterNum;
    //总体成本
    private double totalCost;
    //总体转化率
    private double totalRate;
}
