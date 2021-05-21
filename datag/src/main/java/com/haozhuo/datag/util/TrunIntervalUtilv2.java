package com.haozhuo.datag.util;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class TrunIntervalUtilv2 {

    public static String run(String interval) throws ScriptException {
        return turnInterval(interval);
    }


    private static String turnInterval(String interval) throws ScriptException {
        String newInterval = interval;
        ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");
        if(!interval.contains("*")){
            return interval;
        }
       String exp = interval.replaceAll("\\(|\\)|[|]","");
       String exp1 = exp.split(",")[0];
       String exp2 = exp.split(",")[1];
       if(exp1.contains("*")) newInterval = newInterval.replace(exp1,jse.eval(exp1).toString());
       if(exp2.contains("*")) newInterval = newInterval.replace(exp2,jse.eval(exp2).toString());
       return newInterval;

    }




    public static String clacExpression(String expression) throws ScriptException {

        ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");

        return jse.eval(expression).toString();

    }




    public static void main(String[] args) throws ScriptException {
     String s =  clacExpression("144 * Math.pow(result / 88.4 / 0.7, -0.329) * Math.pow(0.993, age)");
      System.out.println(s);

    }

}
