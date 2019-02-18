package com.haozhuo.datag.model.bisys;

import com.haozhuo.datag.common.JavaUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lucius on 2/18/19.
 */
@Getter
@Setter
public class Sms {
    private String date;
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

    //总体转化=汇总注册/(短信数量-老用户数）
    private double getTotalRate() {
        int a = oneSmsNum - oldUserNum;
        if (a == 0) {
            return 0;
        }
        return JavaUtils.retainDecimal(smsRegisterNum / a, 4);
    }

    //短信效果统计：总体成本=（短信数量*0.03*2+二次短信*0.03）/汇总注册
    private double getTotalCost() {
        if (smsRegisterNum == 0) {
            return 0;
        }
        return JavaUtils.retainDecimal((oneSmsNum * 0.06 + twoSmsNum * 0.03) / smsRegisterNum, 4);
    }
}
