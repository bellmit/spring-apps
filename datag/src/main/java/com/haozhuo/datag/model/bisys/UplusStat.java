package com.haozhuo.datag.model.bisys;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
/**
 * Created by Lucius on 2/21/19.
 */
@AllArgsConstructor
@Setter
@Getter
public class UplusStat {
    private String date;
    private int orderNum;
    private double orderAmount;
}
