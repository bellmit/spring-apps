package com.haozhuo.datag.model.bisys.virus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VirusNine {
   // private String hubei_confirm_total;//湖北总计确诊
   // private String hubei_touch;//湖北密切接触人数
    private String hubei_lv;//湖北 确诊病例/密切接触者比例
    private String not_hubei_lv;// 非湖北 确诊病例/密切接触者比例
    private String time;
}
