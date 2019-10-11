package com.haozhuo.datag.model.report;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class InsuranceMap {
    private Map<String,String> valueMap;
    private Map<String,String> textRefMap;
    private Map<String,String> flagIdMap;
    private String rsval;
    List list1 = new ArrayList();
}
