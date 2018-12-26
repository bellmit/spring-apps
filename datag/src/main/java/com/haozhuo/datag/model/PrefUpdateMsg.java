package com.haozhuo.datag.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lucius on 10/26/18.
 */

@Setter
@Getter
@AllArgsConstructor
public class PrefUpdateMsg {
    int msgId;
    String userId;
    String info;
}