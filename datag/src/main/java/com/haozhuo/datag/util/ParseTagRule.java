package com.haozhuo.datag.util;

import com.alibaba.fastjson.JSON;


import com.haozhuo.datag.com.service.bean.CheckResult;
import com.haozhuo.datag.com.service.stdrpt.CleanMethod;

import javax.script.ScriptException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseTagRule {


    public static   Byte isNum0(CheckResult checkResult, String unit, String abn_rule, int clacId, int age) throws ScriptException {
        Byte is_yc = 0;
        if(clacId==1&&analysisClacType(abn_rule,checkResult.resultValue(),age)){
            is_yc = 1;
            return is_yc;
        }
        if(!unit.equals("NA") || unit.equals(checkResult.unit())) return is_yc;
        if(CleanMethod.is_number(checkResult.lowValueRef())!=1 || CleanMethod.is_number(checkResult.highValueRef())!=1) return is_yc;
        String stdValue = CleanMethod.result_value_replace(checkResult.resultValue()).trim();
        if(CleanMethod.is_number(stdValue)!=1){
            return is_yc;
        }
        IntervalUtil a = new IntervalUtil();
        try{
            a.dealPercent(checkResult.resultValue());
        }catch (Exception e){
            is_yc = -99;
        }
        String tmp_abn_rule = abn_rule.replaceAll("MINRANGE",checkResult.lowValueRef()).replaceAll("MAXRANGE",checkResult.highValueRef());
        tmp_abn_rule = TrunIntervalUtilv2.run(tmp_abn_rule);
        if (a.isInTheInterval(stdValue,tmp_abn_rule) == true ){
            is_yc = 1 ;
        }else{
            is_yc = 0 ;
        }
        return is_yc;

    }

    public   static Boolean analysisClacType(String ruleDef,String result,int age) throws ScriptException {
        Boolean bool = false;
        if(CleanMethod.is_number(result)==1){
            IntervalUtil interValUtil = new IntervalUtil();
            if(ruleDef.split("∈").length<2) return bool;
            String expression = ruleDef.split("∈")[0].replace("result",result).replace("age",String.valueOf(age));
            String rule =  ruleDef.split("∈")[1];
            bool = interValUtil.isInTheInterval(TrunIntervalUtilv2.clacExpression(expression),rule);
        }
        return bool;
    }

    /**
     * 0 纯数值型解析，判断是否在范围内
     * @param std_value
     * @param abn_rule
     * @param lower_value
     * @param high_value
     * @return 0：不在区间 ；1：在区间；-99：数值异常
     */

    public static Byte isAbnNum0(String std_value,String abn_rule,String lower_value, String high_value) throws ScriptException {
        Byte is_yc = 0;
        String stdValue = CleanMethod.result_value_replace(std_value).trim();
        if(CleanMethod.is_number(stdValue)!=1){
            return is_yc;
        }
        IntervalUtil a = new IntervalUtil();
        try{
            a.dealPercent(std_value);
        }catch (Exception e){
            is_yc = -99;
        }
        String tmp_abn_rule = abn_rule.replaceAll("MINRANGE",lower_value).replaceAll("MAXRANGE",high_value);
        tmp_abn_rule = TrunIntervalUtilv2.run(tmp_abn_rule);
        if (a.isInTheInterval(stdValue,tmp_abn_rule) == true ){
            is_yc = 1 ;
        }else{
            is_yc = 0 ;
        }

        return is_yc;

    }

    /**
     * 10 范围数值型判断是否在范围内
     * @param std_value
     * @param abn_rule
     * @param lower_value
     * @param high_value
     * @return 0：不在区间 ；1：在区间；-99：数值异常
     */
    public static Byte isAbnNum10(String std_value, String abn_rule,String lower_value, String high_value) throws ScriptException {
        Byte is_yc = 0;
        String[] valueList = std_value.split("-");
        IntervalUtil a = new IntervalUtil();
        for(String value:valueList) {
            is_yc = isAbnNum0(value,abn_rule,lower_value,high_value);
            if(is_yc != 1){
                break;
            }
        }
        return is_yc;

    }


    /**
     * 2枚举类型判断是否在枚举列表内
     * @param std_value
     * @param abn_rule
     * @return 0：未在枚举值范围； 1：在枚举值范围
     */
    public static Byte isAbnEnum(String std_value, String abn_rule) {
        Byte is_yc = 0;
        String[] enumArr = abn_rule.split(",");
        List<String> enumList= Arrays.asList(enumArr);
        if(enumList.contains(std_value) == true){
            is_yc = 1;
        }
        return is_yc;
    }





    public static void main(String[] args) throws ScriptException {


    }
}
