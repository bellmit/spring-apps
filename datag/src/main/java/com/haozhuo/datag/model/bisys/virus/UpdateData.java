package com.haozhuo.datag.model.bisys.virus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateData {
    private String nowtime;
    private int hubei_severe;//湖北重症
    private int hubei_server_h;//湖北危重症
    private int hubei_touch;//湖北密切接触者人数
    private int hubei_observe;//湖北医学观察人数
    private int contry_touch_total;//全国总计密切接触者人数
    private int contry_observe_total;//全国总计医学观察人数
    private int contry_confirm_today;//全国新增确诊人数
    private int contry_suspect_today;//全国新增疑似人数
    private int contry_deobserve_today;//全国当日解除医学观察人数
    private int contry_severe;//全国重症人数
}
