package com.haozhuo.datag.model.bisys.virus;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.util.pattern.PathPattern;

@Getter
@Setter
public class NotWH {
    //private String contry_confirm_total;//全国确诊病例
    //private String hubei_confirm_total;//湖北总计确诊
    private String feiHbSum;//全国非湖北累计确诊病例
    //private String contry_confirm_today;//全国新增确诊人数；
    //private String hubei_confirm_today;//湖北新增确诊人数；
    private String feiHbNew;//全国非湖北新增确诊病例
    private String time;//时间
}
