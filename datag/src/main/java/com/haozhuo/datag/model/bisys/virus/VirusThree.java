package com.haozhuo.datag.model.bisys.virus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirusThree {
    private String nowtime;
    @JsonIgnore
    private int hubeiNewComfire;
    @JsonIgnore
    private int countHubeiComfire;
    @JsonIgnore
    private int wuhanNewComfir;
    @JsonIgnore
    private int countWhComfire;
    private int HbexceptWhNewComfir;
    private int hbexceptWhCountComfire;
}
