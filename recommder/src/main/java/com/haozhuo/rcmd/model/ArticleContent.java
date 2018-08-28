package com.haozhuo.rcmd.model;

import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Setter
@AllArgsConstructor
@Deprecated
public class ArticleContent {
    String contentId;

    String title;

    String abstracts;

    String content;

    String date;

    Double score;
//
   String contentType;
}
