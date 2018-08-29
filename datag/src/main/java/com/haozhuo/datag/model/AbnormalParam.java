package com.haozhuo.datag.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbnormalParam {
    private String exceptionItemName;
    private String exceptionItemAlias;
    private String possibleDiseases;
    private String possibleDiseaseAlias;
    private String possibleSymptoms;
    private String possibleSymptomAlias;

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s", exceptionItemName, exceptionItemAlias, possibleDiseases, possibleDiseaseAlias, possibleSymptoms, possibleSymptomAlias);
    }
}
