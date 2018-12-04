package com.haozhuo.datag.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

/**
 * Created by Lucius on 12/4/18.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GoodsSearchParams {
    String keywords;
    String cityId;
    String goodsType;
    int from = 0;
    int size;
    String[] excludeSkuIds;
}
