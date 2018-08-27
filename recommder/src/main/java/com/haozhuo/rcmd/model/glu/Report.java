package com.haozhuo.rcmd.model.glu;

import lombok.Setter;
import lombok.Getter;

/**
 * Created by Lucius on 8/27/18.
 */
@Getter
@Setter
public class Report {
    CheckItem[] checkItems;
    String[] generalSummarys;
    String[] generalSummarys2;
}
