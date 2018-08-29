package com.haozhuo.rcmd.model;

import lombok.Setter;
import lombok.Getter;

/**
 * Created by Lucius on 8/28/18.
 */
@Getter
@Setter
public class Video {
    private long id;
    private String title;
    private int status;
    private String url;
    private String channelId;
    private String categoryId;
    private String tags;
    private int timeLen;
    private String description;
    private String createTime;
    private String updateTime;
}