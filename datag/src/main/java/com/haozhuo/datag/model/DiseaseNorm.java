package com.haozhuo.datag.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lucius on 1/3/19.
 */
@Getter
@Setter
@AllArgsConstructor
public class DiseaseNorm {
    private String unNorm;
    private String norm;

    @Override
    public String toString() {
        return "DiseaseNorm{" +
                "unNorm='" + unNorm + '\'' +
                ", norm='" + norm + '\'' +
                '}';
    }
}
