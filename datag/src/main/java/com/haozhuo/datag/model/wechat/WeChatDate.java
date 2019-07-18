package com.haozhuo.datag.model.wechat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeChatDate {
    private String date;
    private int cumulateuser;
    private int newuser;
    private String guanzhurate;
    private String downloadrate;
    private int canceluser;
    private int saoma;
    private int num;

    public WeChatDate(String date, int cumulateuser, int newuser, int canceluser, int saoma, int num) {
        this.date = date;
        this.cumulateuser = cumulateuser;
        this.newuser = newuser;
        this.canceluser = canceluser;
        this.saoma = saoma;
        this.num = num;
    }

    public WeChatDate() {
        this.date = date;
        this.cumulateuser = cumulateuser;
        this.newuser = newuser;
        this.canceluser = canceluser;
        this.saoma = saoma;
        this.num = num;
    }

}
