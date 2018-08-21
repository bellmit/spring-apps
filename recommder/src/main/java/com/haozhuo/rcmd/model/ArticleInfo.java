package com.haozhuo.rcmd.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lucius on 8/20/18.
 */
@Setter
@Getter
@AllArgsConstructor
public class ArticleInfo {
    String contentId;

    String title;

    String abstracts;

    String content;

    String date;

    Double score;

    String contentType;

}
