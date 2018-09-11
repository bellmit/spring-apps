package com.haozhuo.datag.common;


import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.stream.IntStream;

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
        return IntStream.range(0, n).boxed().map(i -> getNdaysAgo(i)).toArray(String[]::new);
    }
    public static void main(String[] args) {
        System.out.println(StringUtils.arrayToCommaDelimitedString(getLastNdaysArray(10)));
        StringUtils.arrayToCommaDelimitedString(getLastNdaysArray(10));
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

    public static boolean isEmpty(Object obj) {
        return obj == null;
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isEmpty(Object[] array) {
        return (array == null) || array.length == 0;
    }

    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    public static boolean isEmpty(Collection collection) {
        return (collection == null) || collection.size() == 0;
    }

    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }
}
