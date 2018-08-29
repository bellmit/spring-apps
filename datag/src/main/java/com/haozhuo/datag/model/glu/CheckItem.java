package com.haozhuo.datag.model.glu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Setter;
import lombok.Getter;

/**
 * Created by Lucius on 8/27/18.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckItem {
    private String checkItemName;
    private String departmentName;
    private String salePrice;
    private String checkStateId;
    private String checkUserName;
    private CheckResult[] checkResults;
}
