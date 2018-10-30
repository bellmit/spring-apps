package com.haozhuo.datag.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lucius on 8/17/18.
 */
@Getter
@Setter
@Data
public class Live {
    private Long id;
    private String title;
    private int status;
    private String description;
    private int channelId;
    private int categoryId;
    private String tags;
    private int isPay;
    private String playTime;
    private String updateTime;
}
