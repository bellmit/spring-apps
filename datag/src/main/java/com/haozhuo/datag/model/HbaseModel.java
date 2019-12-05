package com.haozhuo.datag.model;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class HbaseModel {
    private String key;
    private String value;
    private String rowName;
}
