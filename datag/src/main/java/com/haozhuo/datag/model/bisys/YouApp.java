package com.haozhuo.datag.model.bisys;

import lombok.Setter;
import lombok.Getter;

/**
 * Created by Lucius on 2/18/19.
 */
@Setter
@Getter
public class YouApp {
    private String date;
    //平台. 合计:0, Android:1, iOS:2
    private int os;
    //下载用户(打开)
    private int downloadUsers;
    //累计下载用户
    private int totalDownloadUsers;
    //活跃用户
    private int activeUsers;
    //启动次数
    private int startNum;

    @Override
    public String toString() {
        return "YouApp{" +
                "date='" + date + '\'' +
                ", os=" + os +
                ", downloadUsers=" + downloadUsers +
                ", totalDownloadUsers=" + totalDownloadUsers +
                ", activeUsers=" + activeUsers +
                ", startNum=" + startNum +
                '}';
    }
}
