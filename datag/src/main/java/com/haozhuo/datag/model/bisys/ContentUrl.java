package com.haozhuo.datag.model.bisys;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lucius on 1/17/19.
 */
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentUrl {
    private String date;
    private String location;
    private String url;
    private int url_pv;
    private int url_uv;

}
