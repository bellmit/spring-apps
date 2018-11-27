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
    //商品ID
    private String goodsId;

    //商品名称
    private String goodsName;

    //商品描述
    private String goodsDescription;

    //商品类别
    private String category;

    //二级分类
    private String subCategory;

    //商品标签
    private List<String> goodTags;

    //三级标签
    private List<String> thirdTags;

    //所属城市编码
    private List<String> cityIds;

    private int rcmdScore = -1;

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
            return list.stream().collect(Collectors.joining(","));
        }
    }

//    public String getThirdTagsStr() {
//        return thirdTags.stream().collect(Collectors.joining(","));
//    }
//
//    public String getCityIdsStr() {
//        return cityIds.stream().collect(Collectors.joining(","));
//    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
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

    public void setGoodsTags(List<String> goodsTags) {
        this.goodTags = goodsTags;
    }

    public void setThirdTags(List<String> thirdTags) {
        this.thirdTags = thirdTags;
    }

    public void setCityIds(List<String> cityIds) {
        this.cityIds = cityIds;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Goods{" +
                "goodsId='" + goodsId + '\'' +
                ", goodsName='" + goodsName + '\'' +
                ", goodsDescription='" + goodsDescription + '\'' +
                ", category='" + category + '\'' +
                ", subCategory='" + subCategory + '\'' +
                ", goodTags=" + goodTags +
                ", thirdTags=" + thirdTags +
                ", cityIds=" + cityIds +
                ", rcmdScore=" + rcmdScore +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
