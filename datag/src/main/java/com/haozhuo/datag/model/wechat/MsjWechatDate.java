package com.haozhuo.datag.model.wechat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsjWechatDate {
    private String date;
    private int cumulateuser;
    private int newuser;
    private String guanzhurate;
    private String downloadrate;
    private int canceluser;
    private int saoma;
    private int num;

    public MsjWechatDate(String date, int cumulateuser, int newuser, int canceluser, int saoma,int num) {
        this.date = date;
        this.cumulateuser = cumulateuser;
        this.newuser = newuser;
        this.canceluser = canceluser;
        this.saoma = saoma;
        this.num = num;
    }
}
