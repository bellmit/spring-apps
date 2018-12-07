package com.haozhuo.datag.model;

import com.haozhuo.datag.common.Utils;
import lombok.NoArgsConstructor;

import java.util.Arrays;

/**
 * Created by Lucius on 12/4/18.
 */
@NoArgsConstructor
public class GoodsSearchParams {
    private String keywords;
    private String cityId;
    private String goodsType;
    private int from = -1;
    private int size;
    private int pageNo = -1;
    private String[] excludeSkuIds;

    public GoodsSearchParams(String keywords, String cityId, String goodsType, int from, int size, String[] excludeSkuIds) {
        this.keywords = keywords;
        this.cityId = cityId;
        this.goodsType = goodsType;
        this.from = from;
        this.size = size;
        this.excludeSkuIds = excludeSkuIds;
    }

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

    public GoodsSearchParams pageNo(int pageNo) {
        this.pageNo = pageNo;
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

    public String getKeywords() {
        return keywords;
    }

    public String getCityId() {
        return cityId;
    }

    public String getGoodsType() {
        return goodsType;
    }

    public int getFrom() {
        if (from == -1 && pageNo == -1) {
            return 0;
        } else if (from >= 0) {
            return from;
        } else {
            return (pageNo - 1) * size;
        }
    }

    public int getSize() {
        return size;
    }

    public int getPageNo() {
        return pageNo;
    }

    public String[] getExcludeSkuIds() {
        return excludeSkuIds;
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
