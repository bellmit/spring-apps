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

    public Map<String, String> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<String, String> valueMap) {
        this.valueMap = valueMap;
    }

    public Map<String, String> getTextRefMap() {
        return textRefMap;
    }

    public void setTextRefMap(Map<String, String> textRefMap) {
        this.textRefMap = textRefMap;
    }

    public Map<String, String> getFlagIdMap() {
        return flagIdMap;
    }

    public void setFlagIdMap(Map<String, String> flagIdMap) {
        this.flagIdMap = flagIdMap;
    }

    public String getRsval() {
        return rsval;
    }

    public void setRsval(String rsval) {
        this.rsval = rsval;
    }

    public List getList1() {
        return list1;
    }

    public void setList1(List list1) {
        this.list1 = list1;
    }

    List list1 = new ArrayList();
}
