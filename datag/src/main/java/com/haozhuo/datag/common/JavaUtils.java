package com.haozhuo.datag.common;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.stream.IntStream;

/**
 * Created by Lucius on 8/18/18.
 */
@SuppressWarnings("WeakerAccess")
public class JavaUtils {
    public static double retainDecimal(double number, int bits){
        return new BigDecimal(number).setScale(bits, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
    public static String getSeveralDaysAgo(int n, String format) {
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

    public static String getSeveralDaysAgo(int n) {
        return getSeveralDaysAgo(n, "yyyy-MM-dd");
    }

    public static String[] getLastDaysArray(int n) {
        return IntStream.range(0, n).boxed().map(JavaUtils::getSeveralDaysAgo).toArray(String[]::new);
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
