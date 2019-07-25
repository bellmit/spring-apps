package com.haozhuo.datag.model.bisys;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterUM {
    private String date;
    //下载用户合计
    private int downloadUsers;
    //累计下载用户
    private int totalDownloadUsers;
    //下载未注册
    private int downloadUnregister;
    //启动次数
    private int startNum;

    @Override
    public String toString() {
        return "RegisterUM{" +
                "date='" + date + '\'' +
                ", downloadUsers=" + downloadUsers +
                ", totalDownloadUsers=" + totalDownloadUsers +
                ", downloadUnregister=" + downloadUnregister +
                ", startNum=" + startNum +
                '}';
    }
}
