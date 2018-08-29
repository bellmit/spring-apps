package com.haozhuo.rcmd.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lucius on 8/17/18.
 */
@Getter
@Setter
@Data
public class LiveInfo {
    private Long id;
    private String title;
    private String oneLevelTag;
    private String twoLevelTag;
    private String labels;
    private String labelIds;
    private String keywords;
    private String basicTags;
    private String category;
    private int isPay;
    private String playTime;
}
