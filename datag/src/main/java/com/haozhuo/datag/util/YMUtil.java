package com.haozhuo.datag.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.ocean.rawsdk.ApiExecutor;
import com.alibaba.ocean.rawsdk.client.exception.OceanException;
import com.haozhuo.datag.model.bisys.RegisterUM;
import com.haozhuo.datag.model.bisys.YouApp;
import com.umeng.uapp.param.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class YMUtil {
    //Android
    private   static final  String AdAPPKEY = "5668dc8ae0f55a994e00035c";
    //ios
    private   static final  String IosAPPKEY = "5668dce167e58ebde100003e";
    private   static final  String totalAPPKEY = "total";

    public static List<YouApp> getAPPData(ApiExecutor apiExecutor,String strartDate,String enddate) throws OceanException {
        List<YouApp> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar dd =Calendar.getInstance();
        try {
            Date d1 = sdf.parse(strartDate);
            //结束日期
            Date d2 = sdf.parse(enddate);
            dd.setTime(sdf.parse(strartDate));
            Date tmp=d1;
            while(tmp.getTime()<=d2.getTime()) {
                tmp=dd.getTime();
                String date =sdf.format(tmp);
                //System.out.println(date);
                //数据统计
                UmengUappGetDailyDataParam dailyDataParam = new UmengUappGetDailyDataParam();
                // param.getOceanRequestPolicy().setUseHttps(false);
                dailyDataParam.setAppkey(AdAPPKEY);
                dailyDataParam.setDate(date);
                //ios数据统计
                UmengUappGetDailyDataParam iosdailyDataParam = new UmengUappGetDailyDataParam();
                iosdailyDataParam.setAppkey(IosAPPKEY);
                iosdailyDataParam.setDate(date);


                YouApp adyouApp = new YouApp();
                adyouApp.setDate(date);
                adyouApp.setOs(1);
                UmengUappGetDailyDataResult  dailyDataResult = apiExecutor.execute(dailyDataParam);
                //活跃用户数
                adyouApp.setActiveUsers(dailyDataResult.getDailyData().getActivityUsers());
                //新增用户数
                adyouApp.setDownloadUsers(dailyDataResult.getDailyData().getNewUsers());
                //启动次数
                adyouApp.setStartNum(dailyDataResult.getDailyData().getLaunches());
                //累计下载用户
                adyouApp.setTotalDownloadUsers(dailyDataResult.getDailyData().getTotalUsers());

                list.add(adyouApp);
                YouApp iosyouApp = new YouApp();
                iosyouApp.setDate(date);
                iosyouApp.setOs(2);
                UmengUappGetDailyDataResult  iosdailyDataResult = apiExecutor.execute(iosdailyDataParam);
                //活跃用户数
                iosyouApp.setActiveUsers(iosdailyDataResult.getDailyData().getActivityUsers());
                //新增用户数
                iosyouApp.setDownloadUsers(iosdailyDataResult.getDailyData().getNewUsers());
                //启动次数
                iosyouApp.setStartNum(iosdailyDataResult.getDailyData().getLaunches());
                //累计下载用户
                iosyouApp.setTotalDownloadUsers(iosdailyDataResult.getDailyData().getTotalUsers());
                list.add(iosyouApp);
                //总计
                YouApp totalyouApp = new YouApp();
                totalyouApp.setDate(date);
                totalyouApp.setOs(0);
                //活跃用户数
                totalyouApp.setActiveUsers(adyouApp.getActiveUsers()+iosyouApp.getActiveUsers());
                //新增用户数
                totalyouApp.setDownloadUsers(adyouApp.getDownloadUsers()+iosyouApp.getDownloadUsers());
                //启动次数
                totalyouApp.setStartNum(adyouApp.getStartNum()+iosyouApp.getStartNum());
                // 累计下载用户
                totalyouApp.setTotalDownloadUsers(adyouApp.getTotalDownloadUsers()+iosyouApp.getTotalDownloadUsers());
                list.add(totalyouApp);

                //天数加上1
                dd.add(Calendar.DAY_OF_MONTH, 1);
                tmp=dd.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return list;
    }


    public static List<RegisterUM> getRegisterData(ApiExecutor apiExecutor, String strartDate, String enddate) throws OceanException {
        List<RegisterUM> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar dd =Calendar.getInstance();
        try {
            Date d1 = sdf.parse(strartDate);
            //结束日期
            Date d2 = sdf.parse(enddate);
            dd.setTime(sdf.parse(strartDate));
            Date tmp=d1;
            while(tmp.getTime()<=d2.getTime()) {
                tmp=dd.getTime();
                String date =sdf.format(tmp);
                //System.out.println(date);
                //数据统计
                UmengUappGetDailyDataParam dailyDataParam = new UmengUappGetDailyDataParam();
                // param.getOceanRequestPolicy().setUseHttps(false);
                dailyDataParam.setAppkey(AdAPPKEY);
                dailyDataParam.setDate(date);
                //ios
                UmengUappGetDailyDataParam iosdailyDataParam = new UmengUappGetDailyDataParam();
                iosdailyDataParam.setAppkey(IosAPPKEY);
                iosdailyDataParam.setDate(date);

                UmengUappGetDailyDataResult  dailyDataResult = apiExecutor.execute(dailyDataParam);
                UmengUappGetDailyDataResult  iosdailyDataResult = apiExecutor.execute(iosdailyDataParam);

                RegisterUM totalregisterUM = new RegisterUM();
                totalregisterUM.setDate(date);
                //新增用户数
                totalregisterUM.setDownloadUsers(dailyDataResult.getDailyData().getNewUsers()+iosdailyDataResult.getDailyData().getNewUsers());
                //启动次数
                totalregisterUM.setStartNum(dailyDataResult.getDailyData().getLaunches()+iosdailyDataResult.getDailyData().getLaunches());
                // 累计下载用户
                totalregisterUM.setTotalDownloadUsers(dailyDataResult.getDailyData().getTotalUsers()+iosdailyDataResult.getDailyData().getTotalUsers());
                list.add(totalregisterUM);

                //天数加上1
                dd.add(Calendar.DAY_OF_MONTH, 1);
                tmp=dd.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return list;
    }



    public static void main(String[] args) {
        // 请替换apiKey和apiSecurity
        ApiExecutor apiExecutor = new ApiExecutor("2767273", "SdrFmOuLmqIY");
        apiExecutor.setServerHost("gateway.open.umeng.com");

         /*   for (YouApp youApp:YMUtil.getData(apiExecutor,"2019-07-18","2019-07-19")) {
                System.out.println(youApp);
            }
*/


    }

}
