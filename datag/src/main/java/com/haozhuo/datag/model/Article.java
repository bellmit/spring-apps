package com.haozhuo.datag.model;

import lombok.Setter;
import lombok.Getter;

/**
 * Created by Lucius on 8/28/18.
 */
@Getter
@Setter
public class Article {
    private long informationId;
    private int status;
    private String title;
    private String image;
    private String images;
    private String content;
    private int channelId;
    private int categoryId;
    private String tags;
    private String createTime;
    private String updateTime;
}
