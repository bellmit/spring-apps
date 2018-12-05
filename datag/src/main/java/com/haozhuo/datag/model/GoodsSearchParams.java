package com.haozhuo.datag.model;

import com.haozhuo.datag.common.Utils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by Lucius on 12/4/18.
 */
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
        this.keywords = Utils.removeStopWords(keywords);
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

    @Override
    public String toString() {
        return "GoodsSearchParams{" +
                "keywords='" + keywords + '\'' +
                ", cityId='" + cityId + '\'' +
                ", goodsType='" + goodsType + '\'' +
                ", from=" + from +
                ", size=" + size +
                ", excludeSkuIds=" + Arrays.toString(excludeSkuIds) +
                '}';
    }
}
