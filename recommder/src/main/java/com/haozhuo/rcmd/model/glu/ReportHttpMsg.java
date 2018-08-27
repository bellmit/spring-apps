package com.haozhuo.rcmd.model.glu;

import lombok.Getter;
import lombok.Setter;
/**
 * Created by Lucius on 8/27/18.
 */
@Setter
@Getter
public class ReportHttpMsg {
    String msg;
    ReportStrData data;
    String code;
    String tid;
}
