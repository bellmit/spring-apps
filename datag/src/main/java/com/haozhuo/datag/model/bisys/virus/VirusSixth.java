package com.haozhuo.datag.model.bisys.virus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirusSixth {
    @JsonIgnore
    private int nationalServer;
    @JsonIgnore
    private int nationalConfirm;
    @JsonIgnore
    private int hubeiServer;
    @JsonIgnore
    private int hubeiConfirm;

    private String nowtime;
    private Double nationalSeverity;  //nationalServer/nationalConfirm
    private Double HbSeverity; //hubeiConfirm/hubeiServer
    private Double exceptHbSeverity; //(nationalServer-hubeiServer)/(nationalConfirm-hubeiConfirm)
}
