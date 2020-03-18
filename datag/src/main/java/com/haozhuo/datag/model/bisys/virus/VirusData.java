package com.haozhuo.datag.model.bisys.virus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VirusData {
    private String province;
    private int confirm;
    private int suspect;
    private int heal;
    private int dead;
    private String updateTime;
}
