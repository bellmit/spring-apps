package com.haozhuo.datag.util;



import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SqlDate {
    public static java.sql.Date strToDate(String strDate) {
        String str = strDate;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date d = null;
        try {
            d = format.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        java.sql.Date date = new java.sql.Date(d.getTime());
        return date;
    }
    public static java.sql.Timestamp strToDateTime(String strDate) {
        String str = strDate;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = null;

        try {
            //d = new Date();
            d=format.parse(str);
            // Timestamp tmp= Timestamp.v
        } catch (Exception e) {
            e.printStackTrace();
        }

        java.sql.Timestamp timestamp = new java.sql.Timestamp(d.getTime());
        // java.sql.Date date = new java.sql.Date(d.getTime());
        return timestamp;
    }

    public static java.sql.Time strToTime(String strDate) {
        String str = strDate;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date d = null;
        try {
            d = format.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        java.sql.Time time = new java.sql.Time(d.getTime());
        return time;
    }

    public static void main(String[] args){
        System.out.print(strToDateTime("2019-07-30 19:17:09"));
        //System.out.print(strToTime("2019-07-30 19:17:09"));
    }
}

