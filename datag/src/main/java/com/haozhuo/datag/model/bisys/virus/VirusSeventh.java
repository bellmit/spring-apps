package com.haozhuo.datag.model.bisys.virus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirusSeventh {
    @JsonIgnore
    private int hbServer;
    @JsonIgnore
    private int hbh;
    @JsonIgnore
    private int hbConfirm;
    private String nowtime;
    private Double first;
    private Double second;
}
