package com.haozhuo.datag.model.bisys;

import lombok.Setter;
import lombok.Getter;

import java.util.List;

/**
 * Created by Lucius on 3/5/19.
 */
@Getter
@Setter
public class Page<T> {
    private int pageNo;
    private int pageSize;
    private int totalPageNum;
    private List<T> content;
}
