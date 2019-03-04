package com.haozhuo.datag.model.bisys;

import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Lucius on 2/21/19.
 */
@AllArgsConstructor
@Setter
@Getter
public class KindGoods {
    private String date;
    private String goodsName;
    private int goodsNum;
    private double totalFee;
}
