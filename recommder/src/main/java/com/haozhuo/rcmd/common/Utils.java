package com.haozhuo.rcmd.common;

/**
 * Created by Lucius on 8/20/18.
 */
public class Utils {
    public static String removeStopWords(String text){
        return text.replaceAll("(增高|降低|阳性|阴性|偏低|偏高)", "");
    }

}
