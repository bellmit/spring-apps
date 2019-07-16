package com.haozhuo.datag.model.bisys;


import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class UserBehavior {

    @Id
    private String id;
    private String bhvType; // 行为类型
    private String userId; // 用户ID
    private String actObj; // 行为对象
    private String objType; // 对象类型
    private Double bhvAmt; // 用户对物品的评分、消费、观看时长等
    private Double bhvCnt; // 行为次数，默认为1，消费可以埋购买件数
    private String content; // 行为的具体内容
    private String traceId; // 父级页面元素跟踪
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date bhvDatetime; // 行为时间

    private String from; // 来源 ios android
    private String appVersion; // app版本号
    private String systemVersion; // 系统版本号
    private String uuid; // 设备号


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBhvType() {
        return bhvType;
    }

    public void setBhvType(String bhvType) {
        this.bhvType = bhvType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActObj() {
        return actObj;
    }

    public void setActObj(String actObj) {
        this.actObj = actObj;
    }

    public String getObjType() {
        return objType;
    }

    public void setObjType(String objType) {
        this.objType = objType;
    }

    public Double getBhvAmt() {
        return bhvAmt;
    }

    public void setBhvAmt(Double bhvAmt) {
        this.bhvAmt = bhvAmt;
    }

    public Double getBhvCnt() {
        return bhvCnt;
    }

    public void setBhvCnt(Double bhvCnt) {
        this.bhvCnt = bhvCnt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Date getBhvDatetime() {
        return bhvDatetime;
    }

    public void setBhvDatetime(Date bhvDatetime) {
        this.bhvDatetime = bhvDatetime;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
