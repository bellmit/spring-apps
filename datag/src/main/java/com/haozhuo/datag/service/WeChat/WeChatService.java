package com.haozhuo.datag.service.WeChat;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haozhuo.datag.common.HttpUtil;
import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.common.StringUtil;
import com.haozhuo.datag.model.wechat.*;
import com.haozhuo.datag.service.BisysJdbcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "ConstantConditions"})
@Component
public class WeChatService {
    private static final Logger logger = LoggerFactory.getLogger(BisysJdbcService.class);
    @Autowired
    @Qualifier("bisysJdbc") //选择jdbc连接池
    private JdbcTemplate bisysDB;

    @Value("${wechat.mp.appID:wx5356e6bdfb9676d0}")
    private String appID;

    @Value("${wechat.mp.secret:54ac5c85eac3ec65fda5120c952150f9}")
    private String secret;


    //@Autowired
    private HttpUtil httpUtil = new HttpUtil();

    private static String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?"
            + "grant_type=client_credential&appid=APPID&secret=APPSECRET";
    private static String GETUSERSUMMARY_URL = "https://api.weixin.qq.com/datacube/getusersummary?access_token=ACCESS_TOKEN";

    private static String GETUSERCUMULATE_URL = "https://api.weixin.qq.com/datacube/getusercumulate?access_token=ACCESS_TOKEN";

    private static String GETUESRSAOMA_URL = "http://bloodtrack.ihaozhuo.com/admin/yjkqrcount?user=yjk&pwd=xzb&startTime=DATE";

    private final static String getDownlodeNumSQL = "select * from content_weixin_app a  where a.`date`>= ? and a.`date` <= ? ";
    private final static String dailyMallOrderInputUpdateSQL =
            "INSERT INTO `wechat_data` (`date`, `cumulate_user`, `new_user`, `cancel_user`, `saoma`, `num`) VALUES (?,?,?,?,?,?)";

    private final static String getWechatData = "select * from wechat_data a  where a.`date`>= ? and a.`date` <= ? ";

    /**
     * 查询wechat_data数据
     */
    public List<WeChatDate> getWechatDate(int typeId, String date, String endDate) {
        List<WeChatDate> list = null;
        String sql = getWechatData;
        // String sql1 =  managerQuestion;
        Object[] params = new Object[]{date, endDate};

        //  Object[] params = new Object[]{date, endDate, OpsMallOrder.getGenre(typeId)};
        //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
        list = bisysDB.query(sql, params,
                (resultSet, i) -> {
                    WeChatDate weChatDate = new WeChatDate(
                            resultSet.getString("date"),
                            resultSet.getInt("cumulate_user"),
                            resultSet.getInt("new_user"),
                            resultSet.getInt("cancel_user"),
                            resultSet.getInt("saoma"),
                            resultSet.getInt("num")
                    );
                    DecimalFormat df = new DecimalFormat("0.00");

                    int a = weChatDate.getNewuser();
                    int b = weChatDate.getSaoma();
                    int c = weChatDate.getNum();
                    int d = weChatDate.getCumulateuser();
                    String s = df.format((float) a / b * 100);
                    String t = df.format((float) c / d * 100);
                    weChatDate.setGuanzhurate(s + "%");
                    weChatDate.setDownloadrate(t + "%");
                    return weChatDate;
                }
        );


        return list;
    }

    /**
     * wechat_data数据存储
     */
    public void saveData(String date, String enddate) {
        List<GetUserSummary> userSummaries = getuserSummary(date, enddate);
        List<GetUserCumulate> userCumulates = getUserCumulateList(date, enddate);
        List<DownloadNum> getDownlodeNum = getDownlodeNum(date, enddate);
        
        String updateTime = StringUtil.isEmpty(date) ? JavaUtils.getToday() : date;
        int saoma = getSaomaNum(date);
        int newuser = 0;
        int cancel = 0;
        int num = 0;
        
        userCumulates.get(0).getCumulate_user();
 
        for (int i = 0; i < userSummaries.size(); i++) {
            newuser += Integer.parseInt(userSummaries.get(i).getNew_user());
            cancel += Integer.parseInt(userSummaries.get(i).getCancel_user());
        }
        for (int i = 0; i < getDownlodeNum.size(); i++) {
            num += getDownlodeNum.get(i).getNum();
        }

        bisysDB.update(dailyMallOrderInputUpdateSQL, updateTime, userCumulates.get(0).getCumulate_user(),
                newuser, cancel, saoma,
                num);
        System.out.println(newuser + "," + cancel + "," + num);

    }

    /**
     * 获取扫码数量
     */
    public int getSaomaNum(String date) {
        List<SaoMa> saoMas = new ArrayList<>();

        String URL = GETUESRSAOMA_URL.replace("DATE", date);
        String s = httpUtil.sendGetRequest(URL, null);

        JSONObject jsonObject = JSONObject.parseObject(s);
        if (jsonObject.containsKey("a_totalOneday")){
            String s1 = jsonObject.get("a_totalOneday").toString();
            int s2 = Integer.parseInt(s1);

            return s2;
        }else{
            return 0;
        }


      //  JSONArray jsonObject1 = JSONObject.parseArray(s);
/*        for (int i = 0; i < jsonObject1.size(); i++) {
            Object o = jsonObject1.get(i);
            SaoMa saoMa = new SaoMa();
            JSONObject jsonObject = JSONObject.parseObject(o.toString());
            addSaomaNum(jsonObject, saoMa);
            saoMas.add(saoMa);
        }*/

    }

/*    private void addSaomaNum(JSONObject o, SaoMa saoMa) {
        saoMa.setIn_factory(o.containsKey("in_factory") ? o.get("in_factory").toString() : "");
        saoMa.setPoint_name(o.containsKey("point_name") ? o.get("point_name").toString() : "");
        saoMa.setOneday(o.containsKey("oneday") ? Integer.parseInt(o.get("oneday").toString()) : 0);
        saoMa.setTotal(o.containsKey("total") ? Integer.parseInt(o.get("total").toString()) : 0);
    }*/

    /**
     * 获取下载次数
     * bisys
     */
    public List<DownloadNum> getDownlodeNum(String date, String endDate) {
        List<DownloadNum> list = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(getDownlodeNumSQL, new Object[]{date, endDate},
                    (resultSet, i) -> {
                        DownloadNum downloadNum = new DownloadNum();
                        downloadNum.setDate(resultSet.getString("date"));
                        downloadNum.setUrl(resultSet.getString("url"));
                        downloadNum.setUrl_pv(resultSet.getInt("url_pv"));
                        downloadNum.setSubmit_pv(resultSet.getInt("submit_pv"));
                        downloadNum.setNum(resultSet.getInt("num"));
                        downloadNum.setIos_num(resultSet.getInt("ios_num"));
                        downloadNum.setAndroid_num(resultSet.getInt("android_num"));
                        downloadNum.setUpdate_time(resultSet.getString("update_time"));
                        return downloadNum;
                    }
            );
        } catch (Exception ex) {
            logger.error("getdownloadNum", ex);
        }
        return list;
    }

    /**
     * 获取累计用户数据（getusercumulate）
     */
    public List<GetUserCumulate> getUserCumulateList(String begindate, String enddate) {
        List<GetUserCumulate> userCumulates = new ArrayList<>();
        String access_token = getAccess_token().getAccess_token();
        String URL = GETUSERCUMULATE_URL.replace("ACCESS_TOKEN", access_token);

        String body = "{\"begin_date\": \"" + begindate + "\",\"end_date\": \"" + enddate + "\"}";
        String s = null;
        try {
            s = httpUtil.sendPostByJson(URL, body);
        } catch (Exception e) {
            System.out.println("接口请求失败" + e.getStackTrace());
        }
        JSONObject json = JSONObject.parseObject(s);
        if (!json.containsKey("list")) {
            return userCumulates;
        } else {
            Object list = json.get("list");
            JSONArray objects = JSONObject.parseArray(list.toString());
            for (int i = 0; i < objects.size(); i++) {
                GetUserCumulate userCumulate = new GetUserCumulate();
                Object o = objects.get(i);
                JSONObject jsonObject = JSONObject.parseObject(o.toString());
                addSummary1(jsonObject, userCumulate);
                userCumulates.add(userCumulate);
            }
        }
        return userCumulates;
    }

    private void addSummary1(JSONObject o, GetUserCumulate userCumulate) {
        userCumulate.setRef_date(o.containsKey("ref_date") ? o.get("ref_date").toString() : "");
        userCumulate.setCumulate_user(o.containsKey("cumulate_user") ? o.get("cumulate_user").toString() : "");
    }

    /**
     * 用户数据分析
     * 获取用户增减数据（getusersummary）
     */
    public List<GetUserSummary> getuserSummary(String begindate, String enddate) {
        //AccessToken token = new AccessToken();
        List<GetUserSummary> userSummaries = new ArrayList<>();
        String access_token = getAccess_token().getAccess_token();
        String URL = GETUSERSUMMARY_URL.replace("ACCESS_TOKEN", access_token);
        String body = "{\"begin_date\": \"" + begindate + "\",\"end_date\": \"" + enddate + "\"}";
        String s = null;
        try {
            s = httpUtil.sendPostByJson(URL, body);
            System.out.println(s);
        } catch (Exception e) {
            System.out.println("接口请求失败" + e.getStackTrace());
        }
        JSONObject json = JSONObject.parseObject(s);
        if (!json.containsKey("list")) {
            return userSummaries;
        } else {
            Object list = json.get("list");
            JSONArray objects = JSONObject.parseArray(list.toString());
            for (int i = 0; i < objects.size(); i++) {
                GetUserSummary summary = new GetUserSummary();
                Object o = objects.get(i);
                JSONObject jsonObject = JSONObject.parseObject(o.toString());
                addSummary(jsonObject, summary);
                userSummaries.add(summary);
            }
        }
        return userSummaries;
    }

    private void addSummary(JSONObject o, GetUserSummary summary) {
        summary.setNew_user(o.containsKey("new_user") ? o.get("new_user").toString() : "");
        summary.setCancel_user(o.containsKey("cancel_user") ? o.get("cancel_user").toString() : "");
        summary.setUser_source(o.containsKey("user_source") ? o.get("user_source").toString() : "");
        summary.setRef_date(o.containsKey("ref_date") ? o.get("ref_date").toString() : "");
    }


    /**
     * 获取access_token
     *
     * @return
     */
    public AccessToken getAccess_token() {
        AccessToken token = new AccessToken();
        String URL = ACCESS_TOKEN_URL.replace("APPID", appID).replace("APPSECRET", secret);
        //JSONObject json = httpRequestUtil.httpsRequest(URL, "Get", null);
        String s = httpUtil.sendGetRequest(URL, null);
        System.out.print("json" + s);// {""}
        JSONObject jsonObject = JSONObject.parseObject(s);
        String s1 = jsonObject.get("access_token").toString();
        token.setAccess_token(s1);

        return token;
    }

}

