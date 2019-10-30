package com.haozhuo.datag.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Utf8 {

    //判断是否为16进制数
    public  boolean isHex(char c){
        if(((c >= '0') && (c <= '9')) ||
                ((c >= 'a') && (c <= 'f')) ||
                ((c >= 'A') && (c <= 'F')))
            return true;
        else
            return false;
    }

    public  String convertPercent(String str){
        StringBuilder sb = new StringBuilder(str);

        for(int i = 0; i < sb.length(); i++){
            char c = sb.charAt(i);
            //判断是否为转码符号%
            if(c == '%'){
                if(((i + 1) < sb.length() -1) && ((i + 2) < sb.length() - 1)){
                    char first = sb.charAt(i + 1);
                    char second = sb.charAt(i + 2);
                    //如只是普通的%则转为%25
                    if(!(isHex(first) && isHex(second)))
                        sb.insert(i+1, "25");
                }
                else{//如只是普通的%则转为%25
                    sb.insert(i+1, "25");
                }

            }
        }

        return sb.toString();
    }
    /*public  static void main(String[] args) throws UnsupportedEncodingException {
        String test = "%E2%98%85+%E7%BA%A2%E7%BB%86%E8%83%9E%E5%8E%8B%E7%A7%AF+%E5%A2%9E%E9%AB%98%3A++%28%E7%BB%93%E6%9E%9C%3A52.8+%E8%8C%83%E5%9B%B4%EF%BC%9A34-50+%25%29";
        //URLDecoder.decode(test, "utf8");//如直接接就会报如题的错误。
        String url = convertPercent(test);
        System.out.println(url);
        System.out.println(URLDecoder.decode(url, "utf8"));


    }*/
}
