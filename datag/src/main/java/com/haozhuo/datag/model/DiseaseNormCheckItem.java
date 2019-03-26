package com.haozhuo.datag.model;

import lombok.*;

import java.util.Objects;

/**
 * Created by Lucius on 1/3/19.
 */
@Getter
@Setter
@AllArgsConstructor
public class DiseaseNormCheckItem {
    private String unNorm;
    private String norm;
    private String checkItem;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiseaseNormCheckItem that = (DiseaseNormCheckItem) o;
        return Objects.equals(unNorm, that.unNorm) &&
                Objects.equals(norm, that.norm) &&
                Objects.equals(checkItem, that.checkItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unNorm, norm, checkItem);
    }
}
