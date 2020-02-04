package com.haozhuo.datag.model.bisys.virus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VirusThirteen {
   // private String contry_cure_total;//全国总计治愈
   // private String contry_confirm_total;//全国总计确诊
    private String contry_lv;//全国出院率

   // private String hubei_cure_total;//湖北总计治愈
   // private String hubei_confirm_total;//湖北总计确诊
    private String hubei_lv;//湖北出院率

    private String not_hb_lv;//非湖北出院率
    private String time;//时间


}
