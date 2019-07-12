package com.haozhuo.datag.model.bisys;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ManageQuestion {

    String date;
    //发布问题量
    int questionNum;
    //已支付金额
    double payAmount;
    //分享次数
    int shareNum;
    //收藏回答次数
    int collectNum;
    //感谢回答次数
    int thankNum;
    //围观次数
    int circuseeNum;
    //围观支付金额
    double circuseeAmount;
    //超时退款订单
    int timeOutNum;
    //追问次数
    int questionCloselyNum;


    public ManageQuestion( String date, int questionNum, double payAmount, int shareNum, int collectNum, int thankNum, int circuseeNum, double circuseeAmount, int timeOutNum, int questionCloselyNum) {
        this.date = date;
        this.questionNum = questionNum;
        this.payAmount = payAmount;
        this.shareNum = shareNum;
        this.collectNum = collectNum;
        this.thankNum = thankNum;
        this.circuseeNum = circuseeNum;
        this.circuseeAmount = circuseeAmount;
        this.timeOutNum = timeOutNum;
        this.questionCloselyNum = questionCloselyNum;
    }

}
