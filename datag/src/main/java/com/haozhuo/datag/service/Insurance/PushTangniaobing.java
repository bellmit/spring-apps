package com.haozhuo.datag.service.Insurance;

import com.haozhuo.datag.model.report.InsuranceMap;

import java.util.Map;

public class PushTangniaobing {
    public String Pushtangniaobing(InsuranceMap insuranceMap){

        Map<String, String> valueMap = insuranceMap.getValueMap();
        Map<String, String> textRefMap = insuranceMap.getTextRefMap();
        Map<String, String> flagIdMap = insuranceMap.getFlagIdMap();

        for (String a : valueMap.keySet()){
           if (a == ""){

           }

        }

        return "";
    }
}
