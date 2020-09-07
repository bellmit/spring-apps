package com.haozhuo.datag.model.bisys.abnormal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class AbPxData {
    private List<String> list;
    private int flag;

}
