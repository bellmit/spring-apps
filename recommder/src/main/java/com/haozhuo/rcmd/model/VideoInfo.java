package com.haozhuo.rcmd.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Lucius on 8/17/18.
 */
@Getter
@Setter
@Data
@Deprecated
public class VideoInfo implements Serializable {
    private Long id;
    private String title;
    private String oneLevelTag;
    private String twoLevelTag;
    private String labels;
    private String labelIds;
    private String keywords;
    private String basicTags;
    private String category;

}
