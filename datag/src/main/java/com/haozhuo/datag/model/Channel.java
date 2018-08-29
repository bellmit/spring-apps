package com.haozhuo.datag.model;

import lombok.Setter;
import lombok.Getter;
/**
 * Created by Lucius on 8/29/18.
 */
@Getter
@Setter
public class Channel {
    private int channelId;
    private String name;
    private int sortNum;
    private int parentId;
    private String createTime;
    private String createOperator;
    private String updateOperator;
    private String updateTime;
}
