package com.haozhuo.datag.model.bisys;

import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Lucius on 1/21/19.
 */
@Getter
@Setter
@AllArgsConstructor
public class ManagePolicyStat {
    private String date;

    //点击步步保bannerUV
    private int bannerNum;

    //点击确认授权UV
    private int clickNum;
    //回传的保单数
    private int policyNum;
    //保单转化率
    private double policyRate;
}
