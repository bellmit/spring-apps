package com.haozhuo.rcmd.model.glu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
/**
 * Created by Lucius on 8/27/18.
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportHttpMsg {
    String msg;
    ReportStrData data;
    String code;
    String tid;
}
