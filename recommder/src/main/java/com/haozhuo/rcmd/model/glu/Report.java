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
public class Report {
    CheckItem[] checkItems;
//    String[] generalSummarys;
//    String[] generalSummarys2;
}
