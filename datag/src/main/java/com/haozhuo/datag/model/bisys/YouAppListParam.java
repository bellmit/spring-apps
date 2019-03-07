package com.haozhuo.datag.model.bisys;

import java.util.List;

/**
 * 日报统计管理-注册用户统计数据增加支持批量
 * @author shuaijun
 * @since 2019.03.07
 * @version 5.4.2
 */
public class YouAppListParam {

    private List<YouApp> dataList;

    public List<YouApp> getDataList() {
        return dataList;
    }

    public void setDataList(List<YouApp> dataList) {
        this.dataList = dataList;
    }
}
