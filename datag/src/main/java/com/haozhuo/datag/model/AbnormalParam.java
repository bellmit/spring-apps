package com.haozhuo.datag.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;


@JsonIgnoreProperties(ignoreUnknown = true)
public class AbnormalParam {
    private String exceptionItemName;
    private String exceptionItemAlias;
    private String possibleDiseases;
    private String possibleDiseaseAlias;
    private String possibleSymptoms;
    private String possibleSymptomAlias;

    public String getExceptionItemName() {
        return exceptionItemName;
    }

    public void setExceptionItemName(String exceptionItemName) {
        this.exceptionItemName = exceptionItemName;
    }

    public String getExceptionItemAlias() {
        return exceptionItemAlias;
    }

    public void setExceptionItemAlias(String exceptionItemAlias) {
        this.exceptionItemAlias = exceptionItemAlias;
    }

    public String getPossibleDiseases() {
        return possibleDiseases;
    }

    public void setPossibleDiseases(String possibleDiseases) {
        this.possibleDiseases = possibleDiseases;
    }

    public String getPossibleDiseaseAlias() {
        return possibleDiseaseAlias;
    }

    public void setPossibleDiseaseAlias(String possibleDiseaseAlias) {
        this.possibleDiseaseAlias = possibleDiseaseAlias;
    }

    public String getPossibleSymptoms() {
        return possibleSymptoms;
    }

    public void setPossibleSymptoms(String possibleSymptoms) {
        this.possibleSymptoms = possibleSymptoms;
    }

    public String getPossibleSymptomAlias() {
        return possibleSymptomAlias;
    }

    public void setPossibleSymptomAlias(String possibleSymptomAlias) {
        this.possibleSymptomAlias = possibleSymptomAlias;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s", exceptionItemName, exceptionItemAlias, possibleDiseases, possibleDiseaseAlias, possibleSymptoms, possibleSymptomAlias);
    }
}
