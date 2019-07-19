package com.haozhuo.datag.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.ocean.rawsdk.ApiExecutor;
import com.alibaba.ocean.rawsdk.client.exception.OceanException;
import com.umeng.uapp.param.*;

public class YMUtil {
    private   static final  String AdAPPKEY = "5668dc8ae0f55a994e00035c";
    private   static final  String IosAPPKEY = "5668dce167e58ebde100003e";

    public static void getData(ApiExecutor apiExecutor,String appkey,String date,String enddate) {
        //启动次数
        UmengUappGetLaunchesParam param = new UmengUappGetLaunchesParam();
        //新增用户数
        UmengUappGetNewUsersParam newUsersParam = new UmengUappGetNewUsersParam();
        //数据统计
        UmengUappGetDailyDataParam dailyDataParam = new UmengUappGetDailyDataParam();
        //新增账号
        UmengUappGetNewAccountsParam newAccountPparam = new UmengUappGetNewAccountsParam();
        // 测试环境只支持http
        // param.getOceanRequestPolicy().setUseHttps(false);

        param.setAppkey(appkey);
        param.setStartDate(date);
        param.setEndDate(enddate);
        newUsersParam.setAppkey(appkey);
        newUsersParam.setStartDate(date);
        newUsersParam.setEndDate(enddate);
        dailyDataParam.setAppkey(appkey);
        dailyDataParam.setDate(enddate);
        newAccountPparam.setAppkey(appkey);
        newAccountPparam.setStartDate(date);
        newAccountPparam.setEndDate(enddate);
        //param.setPeriodType("daily");

        try {
            UmengUappGetLaunchesResult result = apiExecutor.execute(param);
            UmengUappGetNewUsersResult newUsersResult = apiExecutor.execute(newUsersParam);
            UmengUappGetDailyDataResult  dailyDataResult = apiExecutor.execute(dailyDataParam);
            UmengUappGetNewAccountsResult newAccountsResult = apiExecutor.execute(newAccountPparam);
            System.out.println(JSONObject.toJSONString(newUsersResult));
            System.out.println(JSONObject.toJSONString(result));
            System.out.println("新增账号数据："+JSONObject.toJSONString(newAccountsResult));
            System.out.println("统计数据："+JSONObject.toJSONString(dailyDataResult));
        } catch (OceanException e) {
            System.out.println("errorCode=" + e.getErrorCode() + ", errorMessage=" + e.getErrorMessage());
        }
    }

    public static void main(String[] args) {
        // 请替换apiKey和apiSecurity
        ApiExecutor apiExecutor = new ApiExecutor("2767273", "SdrFmOuLmqIY");
        apiExecutor.setServerHost("gateway.open.umeng.com");
        YMUtil.getData(apiExecutor,YMUtil.AdAPPKEY,"2019-07-15","2019-07-18");
        YMUtil.getData(apiExecutor,YMUtil.IosAPPKEY,"2019-07-15","2019-07-18");

    }

}
