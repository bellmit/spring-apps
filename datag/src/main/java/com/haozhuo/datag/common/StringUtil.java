package com.haozhuo.datag.common;

import java.lang.reflect.Field;

public class StringUtil {


    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public Boolean isEmpty2(){
        int fieldLength = this.getClass().getDeclaredFields().length;
        int count = 0;
        for (Field f : this.getClass().getDeclaredFields()) {
            //设置访问性，反射类的方法，设置为true就可以访问private修饰的东西，否则无法访问
            f.setAccessible(true);
            try {
                if (f.get(this) == null) { //判断字段是否为空，并且对象属性中的基本都会转为对象类型来判断
                    count++;
                }
            } catch (IllegalAccessException e) {
                return true;
            }
        }
        if (count >= fieldLength) {
            return true;
        }
        return false;
    }

}



