package com.es.dto;

import lombok.Data;

import java.util.List;

@Data
public class Checksinkname {
    private String sinkName;
    private String customerRiskGradeName;
    private List<Lable> lables;
}
