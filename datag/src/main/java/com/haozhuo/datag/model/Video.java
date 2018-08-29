package com.haozhuo.datag.model;

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
    private int channelId;
    private int categoryId;
    private String tags;
    private int timeLen;
    private String description;
    private String createTime;
    private String updateTime;
}
