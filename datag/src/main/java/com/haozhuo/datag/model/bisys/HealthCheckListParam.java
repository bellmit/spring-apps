package com.haozhuo.datag.model.bisys;

import java.util.List;

/**
 * 日报统计管理-体检数据增加支持批量
 * @author shuaijun
 * @since 2019.03.07
 * @version 5.4.2
 */
public class HealthCheckListParam {

    private List<HealthCheck> dataList;

    public List<HealthCheck> getDataList() {
        return dataList;
    }

    public void setDataList(List<HealthCheck> dataList) {
        this.dataList = dataList;
    }
}
