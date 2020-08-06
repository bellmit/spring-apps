package com.haozhuo.datag.model.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class AbnormalPxPo {
    private List<String> abnormalList;
}
