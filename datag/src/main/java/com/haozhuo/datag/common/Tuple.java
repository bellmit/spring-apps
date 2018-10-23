package com.haozhuo.datag.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lucius on 8/17/18.
 */
@Getter
@Setter
@AllArgsConstructor
public class Tuple<T1,T2> {
    T1 t1;
    T2 t2;

    @Override
    public String toString() {
        return "Tuple{" +
                "t1=" + t1 +
                ", t2=" + t2 +
                '}';
    }
}
