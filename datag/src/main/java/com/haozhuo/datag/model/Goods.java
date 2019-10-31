package com.haozhuo.datag.model;


import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Lucius on 11/7/18.
 */
@Getter
public class Goods {
    public static final int SCORE_MAX = 100;
    public static final int SCORE_MIN = 0;
    public static final int SCORE_DEFAULT = 60;
    private String skuId;
    //商品ID
    private List<String> goodsIds;

    //商品名称
    private String goodsName;

    //商品描述
    private String goodsDescription;

    //商品类别
    private String category;

    //二级分类
    private String subCategory;

    //商品标签
    private List<String> goodsTags;

    //三级标签
    private String searchKeywords;

    //所属城市编码
    private List<String> cityIds;

    //商品类别
    private int goodsType;

    private int rcmdScore = -1;

    private int salesNum = -1;

    private String createTime;

    public void setRcmdScore(int rcmdScore) {
        if (rcmdScore > SCORE_MAX) {
            this.rcmdScore = SCORE_MAX;
        } else if (rcmdScore < SCORE_MIN) {
            this.rcmdScore = SCORE_MIN;
        } else {
            this.rcmdScore = rcmdScore;
        }
    }

    public static String listToStr(List<String> list) {
        if (list == null) {
            return "";
        } else {
            return String.join(",", list);
        }
    }

    public static int getScoreMax() {
        return SCORE_MAX;
    }

    public static int getScoreMin() {
        return SCORE_MIN;
    }

    public static int getScoreDefault() {
        return SCORE_DEFAULT;
    }

    public String getSkuId() {
        return skuId;
    }

    public List<String> getGoodsIds() {
        return goodsIds;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public String getGoodsDescription() {
        return goodsDescription;
    }

    public String getCategory() {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public List<String> getGoodsTags() {
        return goodsTags;
    }

    public String getSearchKeywords() {
        return searchKeywords;
    }

    public List<String> getCityIds() {
        return cityIds;
    }

    public int getGoodsType() {
        return goodsType;
    }

    public int getRcmdScore() {
        return rcmdScore;
    }

    public int getSalesNum() {
        return salesNum;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setGoodsIds(List<String> goodsIds) {
        this.goodsIds = goodsIds;
    }

    public void setGoodsTags(List<String> goodsTags) {
        this.goodsTags = goodsTags;
    }

    public void setSearchKeywords(String searchKeywords) {
        this.searchKeywords = searchKeywords;
    }

    public void setCityIds(List<String> cityIds) {
        this.cityIds = cityIds;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public void setGoodsDescription(String goodsDescription) {
        this.goodsDescription = goodsDescription;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public void setCreateTime(String createTime) {
        if (createTime.length() > 19) {
            this.createTime = createTime.substring(0, 19);
        } else {
            this.createTime = createTime;
        }
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public void setGoodsType(int goodsType) {
        this.goodsType = goodsType;
    }

    public void setSalesNum(int salesNum) {
        this.salesNum = salesNum;
    }

    @Override
    public String toString() {
        return "Goods{" +
                "skuId='" + skuId + '\'' +
                ", goodsIds=" + goodsIds +
                ", goodsName='" + goodsName + '\'' +
                ", goodsDescription='" + goodsDescription + '\'' +
                ", category='" + category + '\'' +
                ", subCategory='" + subCategory + '\'' +
                ", goodsTags=" + goodsTags +
                ", searchKeywords=" + searchKeywords +
                ", cityIds=" + cityIds +
                ", goodsType=" + goodsType +
                ", rcmdScore=" + rcmdScore +
                ", salesNum=" + salesNum +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
