package com.haozhuo.datag.service.Insurance;

import com.haozhuo.datag.model.report.InsuranceMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PushGan {
    @Autowired
    private UserReport userReport;
    public String PushGan(InsuranceMap insuranceMap){
       /* InsuranceMap insuranceMap = userReport.UserRep(rptid);*/
        Map<String, String> valueMap = insuranceMap.getValueMap();
        Map<String, String> textRefMap = insuranceMap.getTextRefMap();
        Map<String, String> flagIdMap = insuranceMap.getFlagIdMap();

        for (String a : valueMap.keySet()){
            String s = valueMap.get(a);
            String s1 = textRefMap.get(a);
            String s2 = flagIdMap.get(a);
            System.out.println(s+"-"+s1+"-"+s2);

        }

        return "";
    }

    public String Push(String rptid){
        InsuranceMap insuranceMap = userReport.UserRep(rptid);
        String s = PushGan(insuranceMap);

        return "";
    }

}
