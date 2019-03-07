package com.haozhuo.datag.model.bisys;

import java.util.List;

/**
 * 日报统计管理-交易数据增加支持批量
 * @author shuaijun
 * @since 2019.03.07
 * @version 5.4.2
 */
public class KindOrderListParam {

    private List<KindOrder> dataList;

    public List<KindOrder> getDataList() {
        return dataList;
    }

    public void setDataList(List<KindOrder> dataList) {
        this.dataList = dataList;
    }
}
