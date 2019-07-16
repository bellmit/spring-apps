package com.haozhuo.datag.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SqlSplit {
    public static  String getSql(String label) {

        StringBuffer groupsSplit = new StringBuffer();
        String result = "";
        if (label.contains(",")) {
            String[] split = label.split(",");
            for (int i = 0; i < split.length; i++) {
                if (!"".equals(split[i]) && split[i] != null)
                    groupsSplit.append("'" + split[i] + "',");
            }
            result = groupsSplit.toString();
            String tp = result.substring(result.length() - 1, result.length());
            if (",".equals(tp)) {
                result = result.substring(0, result.length() - 1);
            }
        }else {
            result = groupsSplit.append("'" + label + "'").toString();
        }
        return  result;
    }
    public static void main(String[] args){
        String[] bodys = {"肺部","妇科","肝脏","化验","甲状腺","口腔","内科","尿液","泌尿生殖系统","肾脏","外科","胃肠","心脏","眼耳鼻喉","一般检查","影像","肿瘤标志物","其他"};
        // System.out.println("人体"+list2.toString());
        List<String> list2 = Arrays.stream( bodys ).collect(Collectors.toList());
        System.out.println(list2);
    }
}
