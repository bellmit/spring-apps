package com.haozhuo.datag.model.bisys;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lucius on 2/20/19.
 */
@Setter
@Getter
@AllArgsConstructor
public class UserRetention {
    private String date;
    private double afterDay1;
    private double afterDay3;
    private double afterDay7;
}
