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
    private String keywords;
    private String cityId;
    private String goodsType;
    private int from = 0;
    private int size;
    private String[] excludeSkuIds;

    public GoodsSearchParams keywords(String keywords) {
        this.keywords = keywords;
        return this;
    }

    public GoodsSearchParams cityId(String cityId) {
        this.cityId = cityId;
        return this;
    }

    public GoodsSearchParams goodsType(String goodsType) {
        this.goodsType = goodsType;
        return this;
    }

    public GoodsSearchParams from(int from) {
        this.from = from;
        return this;
    }

    public GoodsSearchParams size(int size) {
        this.size = size;
        return this;
    }

    public GoodsSearchParams excludeSkuIds(String[] excludeSkuIds) {
        this.excludeSkuIds = excludeSkuIds;
        return this;
    }
}
