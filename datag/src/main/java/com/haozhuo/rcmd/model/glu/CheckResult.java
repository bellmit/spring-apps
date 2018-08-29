package com.haozhuo.rcmd.model.glu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Setter;
import lombok.Getter;

/**
 * Created by Lucius on 8/27/18.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckResult {
    private String checkIndexName;
    private String resultValue;
    private String appendInfo;
//    private String isCalc;
    private String unit;
    private String textRef;
    private String isAbandon;
    private String resultTypeId;
    private int resultFlagId;
    private String lowValueRef;
    private String highValueRef;
    private String showIndex;
    private int canExplain;

}
