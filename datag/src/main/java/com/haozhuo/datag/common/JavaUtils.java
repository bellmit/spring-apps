package com.haozhuo.datag.common;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * Created by Lucius on 8/18/18.
 */
public class JavaUtils {
    public static String getNdaysAgo(int n, String format) {
        Date dNow = new Date();//当前时间
        Calendar calendar = Calendar.getInstance(); //得到日历
        calendar.setTime(dNow);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, -n);
        Date dBefore = calendar.getTime();
        return new SimpleDateFormat(format).format(dBefore);
    }

    public static String getNdaysAgo(int n) {
        return getNdaysAgo(n, "yyyy-MM-dd");
    }

    public static String[] getLastNdaysArray(int n) {
        String[] days = new String[n];
        for (int i = 0; i < n; i++) {
            days[i] = getNdaysAgo(i);
        }
        return days;
    }

    public static String getCurrentFormat(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    public static String getCurrent() {
        return getCurrentFormat("yyyy-MM-dd HH:mm:ss");
    }

    public static String getToday() {
        return getCurrentFormat("yyyy-MM-dd");
    }


    public static boolean isEmpty(String str) {
        return (str == null) || "".equals(str.trim());
    }
    public static boolean isEmpty(Object[] array) {
        return (array == null) || array.length==0;
    }
    public static boolean isEmpty(Collection collection) {
        return (collection == null) || collection.size()==0;
    }
}
