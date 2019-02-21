package com.haozhuo.datag.model.bisys;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

/**
 * Created by Lucius on 2/20/19.
 */
@Setter
@Getter
@AllArgsConstructor
public class AccessData {
    private String date;
    private String source;
    private int pv;
    private int uv;
}
