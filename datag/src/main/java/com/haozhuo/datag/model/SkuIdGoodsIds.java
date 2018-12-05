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

    private double score;

    public String getRandomGoodsId() {
        if (goodsIds != null && goodsIds.size() > 0) {
            return goodsIds.get(random.nextInt(goodsIds.size()));
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SkuIdGoodsIds that = (SkuIdGoodsIds) o;

        if (rcmdScore != that.rcmdScore) return false;
        if (!skuId.equals(that.skuId)) return false;
        return goodsIds.equals(that.goodsIds);

    }

    @Override
    public int hashCode() {
        int result = skuId.hashCode();
        result = 31 * result + goodsIds.hashCode();
        result = 31 * result + rcmdScore;
        return result;
    }

    @Override
    public String toString() {
        return "SkuIdGoodsIds{" +
                "skuId='" + skuId + '\'' +
                ", goodsIds=" + goodsIds +
                ", rcmdScore=" + rcmdScore +
                ", score=" + score +
                '}';
    }
}
