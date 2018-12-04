package com.haozhuo.datag.model;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.util.List;
import java.util.Random;

/**
 * Created by Lucius on 12/4/18.
 */
@Getter
@Setter
@AllArgsConstructor
public class SkuIdGoodsIds {
    private static final Random random = new Random();

    private String skuId;
    //商品ID
    private List<String> goodsIds;

    private int rcmdScore;

    public String getRandomGoodsId() {
        if (goodsIds != null && goodsIds.size() > 0) {
            return goodsIds.get(random.nextInt(goodsIds.size()));
        } else {
            return null;
        }
    }

}
