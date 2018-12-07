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
class Tuple3<T1,T2,T3> {
    T1 t1;
    T2 t2;
    T3 t3;
}
