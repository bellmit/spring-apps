package com.haozhuo.datag.model.bisys;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
/**
 * Created by Lucius on 1/21/19.
 */
@Getter
@Setter
@AllArgsConstructor
public class UplusGoods {
    //uplus会员商品名称
    private String goodsName;
    //下单笔数
    private int orderNum;
    //下单金额
    private double orderAmount;
}
