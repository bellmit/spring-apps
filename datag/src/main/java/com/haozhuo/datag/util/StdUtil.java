package com.haozhuo.datag.util;

public class StdUtil {

    private final static String regEx = "[\\s+`~!@#$%^&*()+=|{}':;'\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
    public static String getSingleNormTag(String abnormal) {

        String regexNormTag = abnormal.replaceAll(regEx, "")
                .replaceAll("Ⅰ", "1")
                .replaceAll("Ⅱ", "2")
                .replaceAll("Ⅲ", "3")
                .toLowerCase();
        return regexNormTag;
    }

    public static void main(String... aarg){
        String a = StdUtil.getSingleNormTag("的地方是AA");
        System.out.println(a);
    }
}
