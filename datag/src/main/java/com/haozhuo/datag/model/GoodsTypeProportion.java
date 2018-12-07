package com.haozhuo.datag.model;

/**
 * Created by Lucius on 12/5/18.
 */

public class GoodsTypeProportion {
    private static final int MAX = 43200000;
    private long updateGoodsTypeNum = -1L;
    private double[] proportionGoodsType = {0.33, 0.33, 0.34};

    public boolean needUpdate() {
        return System.currentTimeMillis() - updateGoodsTypeNum > MAX;
    }

    public int[] getSizeArray(int totalSize) {
        int sizeOfGoodsType1 = (int) Math.round(proportionGoodsType[0] * totalSize);
        int sizeOfGoodsType2 = (int) Math.round(proportionGoodsType[1] * totalSize);
        int sizeOfGoodsType3 = totalSize - sizeOfGoodsType1 - sizeOfGoodsType2;
        return new int[]{sizeOfGoodsType1, sizeOfGoodsType2, sizeOfGoodsType3};
    }

    public void setProportionGoodsType(double[] proportionGoodsType) {
        if (proportionGoodsType.length == 3) {
            this.proportionGoodsType = proportionGoodsType;
        }
    }

    public double[] getProportionGoodsType() {
        return proportionGoodsType;
    }

    public void setUpdateGoodsTypeNum(long updateGoodsTypeNum) {
        this.updateGoodsTypeNum = updateGoodsTypeNum;
    }
}
