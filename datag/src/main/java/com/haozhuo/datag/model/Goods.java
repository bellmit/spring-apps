package com.haozhuo.datag.model;



import lombok.Getter;

import java.util.List;

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

    private int score = SCORE_DEFAULT;

    private String createTime;

    public void setScore(int score) {
        if (score > SCORE_MAX) {
            this.score = SCORE_MAX;
        } else if (score < SCORE_MIN) {
            this.score = SCORE_MIN;
        } else {
            this.score = score;
        }
    }

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

    public void setGoodTags(List<String> goodTags) {
        this.goodTags = goodTags;
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
                ", score=" + score +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
