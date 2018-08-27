package com.haozhuo.rcmd.model.glu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Setter;
import lombok.Getter;

/**
 * Created by Lucius on 8/27/18.
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
    private int age;
    private int sex;
}
