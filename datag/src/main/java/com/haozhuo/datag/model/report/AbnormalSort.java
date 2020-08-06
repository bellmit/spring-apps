package com.haozhuo.datag.model.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AbnormalSort {
    private String exception;
    private String level;
    private String abnormal;

    @Override
    public String toString() {
        return "AbnormalSort{" +
                "exception='" + exception + '\'' +
                ", level='" + level + '\'' +
                ", abnormal='" + abnormal + '\'' +
                '}';
    }
}
