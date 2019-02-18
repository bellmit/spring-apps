package com.haozhuo.datag.model.bisys;

import lombok.Getter;
import lombok.Setter;
/**
 * Created by Lucius on 2/18/19.
 */
@Getter
@Setter
public class Register {
    private String date;
    //下载用户合计
    private int downloadUsers;
    //累计下载用户
    private int totalDownloadUsers;
    //注册用户
    private int registerUsers;
    //累计注册用户
    private int totalRegisterUsers;
    //下载未注册
    private int downloadUnregister;
    //活跃用户
    private int activeUsers;
    //启动次数
    private int startNum;
}
