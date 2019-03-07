package com.haozhuo.datag.model.bisys;

import java.util.List;

/**
 * 日报统计管理-健康管理/绿通统计数据增加支持批量
 * @author shuaijun
 * @since 2019.03.07
 * @version 5.4.2
 */
public class OpsMallOrderListParam {

    private List<OpsMallOrder> dataList;

    public List<OpsMallOrder> getDataList() {
        return dataList;
    }

    public void setDataList(List<OpsMallOrder> dataList) {
        this.dataList = dataList;
    }
}
