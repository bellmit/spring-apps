package com.haozhuo.datag.model.bisys.virus;

import lombok.Getter;
import lombok.Setter;
import springfox.documentation.spring.web.SpringGroupingStrategy;

import java.util.PrimitiveIterator;

@Getter
@Setter
public class SiWangLv {
    //死亡率= 死亡人数/确诊总人数；
    //private String contry_confirm_total;//全国总计确诊；
    //private String contry_dead_total;//全国总计死亡人数；
    private String contry_dead_lv;//全国死亡率

    //private String wuhan_confirm_total;//武汉总计确诊
    //private String wuhan_dead_total;//武汉总计死亡
    private String wuhan_dead_lv;//武汉死亡率

    //private String hubei_confirm_total;//湖北总计确诊；
    //private String hubei_dead_total;//湖北总计死亡
    private String hubei_dead_lv;//湖北死亡率
   //非湖北死亡率     contry_dead_lv;//全国死亡率 -  private String hubei_dead_lv;//湖北死亡率
    private String not_hubei_lv;//非湖北死亡率
    private String time;
}
