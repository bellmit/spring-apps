package com.haozhuo.datag.model;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

/**
 * Created by Lucius on 11/26/18.
 */
@Getter
@Setter
@AllArgsConstructor
public class DocIdScore {
    private String id;
    private double score;
}
