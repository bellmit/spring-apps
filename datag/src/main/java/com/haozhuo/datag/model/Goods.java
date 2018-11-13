package com.haozhuo.datag.model;


import lombok.Setter;
import lombok.Getter;
import java.util.List;

/**
 * Created by Lucius on 11/7/18.
 */
@Getter
@Setter
public class Goods {
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

    private String createTime;
}
