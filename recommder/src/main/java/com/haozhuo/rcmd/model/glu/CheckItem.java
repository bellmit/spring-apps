package com.haozhuo.rcmd.model.glu;

import lombok.Setter;
import lombok.Getter;

/**
 * Created by Lucius on 8/27/18.
 */
@Getter
@Setter
public class CheckItem {
    private String checkItemName;
    private String departmentName;
    private String salePrice;
    private String checkStateId;
    private String checkUserName;
    private CheckResult[] checkResults;
}
