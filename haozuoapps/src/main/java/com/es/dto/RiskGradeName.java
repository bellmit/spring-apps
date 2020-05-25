package com.es.dto;

import lombok.Data;
import java.util.List;


@Data
public class RiskGradeName {

    private String abnormal_label;

    private List<Sugresult> sugresult;
}
