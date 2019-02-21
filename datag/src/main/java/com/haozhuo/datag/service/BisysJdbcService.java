package com.haozhuo.datag.service;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.model.bisys.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Created by Lucius on 9/3/18.
 */
@SuppressWarnings({"unused", "ConstantConditions"})
@Component
public class BisysJdbcService {
    private static final Logger logger = LoggerFactory.getLogger(BisysJdbcService.class);

    @Autowired
    @Qualifier("bisysJdbc") //选择jdbc连接池
    private JdbcTemplate bisysDB;

    private final static String dailyYouAppQuerySQL = "select `date`, os, download_users, total_download_users, active_users," +
            "start_num from daily_app union select `date`, 0 as os, sum(download_users) as download_users, " +
            "sum(total_download_users) as total_download_users, sum(active_users) as active_users, sum(start_num) as start_num " +
            "from daily_app where date >=? and date <= ? group by `date` ";

    private final static String dailyRegisterQuerySQL = "select a.`date`, download_users, total_download_users, active_users, " +
            "start_num, a.register_users, a.total_register_users, a.download_unregister from daily_register a inner join (" +
            "select `date`, sum(download_users) as download_users, sum(total_download_users) as total_download_users, " +
            "sum(active_users) as active_users, sum(start_num) as start_num from daily_app group by `date` ) b on " +
            "a.`date` = b.`date` where a.`date`>= ? and a.`date` <= ?";

    private final static String smsQuerySQL = "select `date`, factory_sms_num, one_sms_num, one_sms_register_num, old_user_num, " +
            "ifnull(one_sms_register_num/(one_sms_num - old_user_num), 0) as one_rate, ifnull(one_sms_cost/one_sms_register_num, 0) as one_sms_cost," +
            "two_sms_num, two_sms_register_num, ifnull(two_sms_register_num/two_sms_num, 0) as two_rate, " +
            "ifnull(two_sms_cost/two_sms_register_num,0) as two_sms_cost, one_sms_register_num + two_sms_register_num as sms_register_num " +
            "from yyht_sms_day where `date`>=? and `date`<=?";

    private final static String opsMallOrderQuerySQL = "select date, order_num, order_amount, pay_order_num, pay_order_amount, apply_order_num as apply_refund_order_num, " +
            " apply_order_amount as apply_refund_order_amount, refund_order_num,refund_order_amount from ops_mall_order where date >= ? and date <=? and genre=?";

    private final static String dailyMallOrderInputQuerySQL = "select date, order_num, order_amount, pay_order_num, pay_order_amount, " +
            " ifnull(apply_refund_order_num, -1) as apply_refund_order_num, ifnull(apply_refund_order_amount, -1) as apply_refund_order_amount," +
            " ifnull(refund_order_num, -1) as refund_order_num, ifnull(refund_order_amount, -1) as refund_order_amount , " +
            " ifnull(refund_gross_profit, -1) as  refund_gross_profit, ifnull(gross_profit, -1) as gross_profit, " +
            " ifnull(gross_profit_rate, -1) as gross_profit_rate, ifnull(refund_rate, -1) as refund_rate, " +
            " ifnull(pay_conversion_rate, -1) as  pay_conversion_rate from daily_mall_order_input where date >= ? and date <=? and genre=?";

    private final static String dailyMallOrderInputUpdateSQL =
            "INSERT INTO `daily_mall_order_input` (`date`, `genre`, `order_num`, `order_amount`, `pay_order_num`, `pay_order_amount`, " +
                    "`apply_refund_order_num`, `apply_refund_order_amount`, `refund_order_num`, `refund_order_amount`, `refund_gross_profit`, " +
                    "`gross_profit`, `gross_profit_rate`, `refund_rate`, `pay_conversion_rate`, `update_time`) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY " +
                    "UPDATE `order_num` = ?, `order_amount` = ?, `pay_order_num` = ?, `pay_order_amount` = ?, `apply_refund_order_num` = ?, " +
                    "`apply_refund_order_amount` = ?, `refund_order_num` = ?, `refund_order_amount` = ?, `refund_gross_profit` = ?, " +
                    "`gross_profit` = ?, `gross_profit_rate` = ?, `refund_rate` = ?, `pay_conversion_rate` = ?, `update_time` = ?";

    private static String serviceTransactionWeChatUpdateSQL = "INSERT INTO `daily_service_transaction_wechat` (`date`, `order_num`, `pay_order_num`, " +
            "`pay_order_amount`, `refund_win_num`, `refund_win_amount`, `pay_use_num`, `pay_profit_amount`, `refund_success_amount`, `update_time`)" +
            " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `order_num` = ?, `pay_order_num` = ?, `pay_order_amount` = ?, " +
            "`refund_win_num` = ?, `refund_win_amount` = ?, `pay_use_num` = ?, `pay_profit_amount` = ?, `refund_success_amount` = ?, `update_time` = ?";


    private final static String healthCheckQuerySql = "select 'App' as src, `date`, sum(order_num) as order_num, sum(pay_order_num) as pay_order_num, " +
            "sum(pay_order_amount) as pay_order_amount, sum(refund_win_num) as refund_win_num, sum(refund_win_amount) as refund_win_amount , " +
            "sum(pay_use_num) as pay_use_num, sum(pay_profit_amount) as pay_profit_amount, sum(refund_success_amount) as refund_success_amount  " +
            "from ops_service_transaction x where x.facilitator like '%美年%'  and `date` >= ? and `date` <= ? group by `date` " +
            "union " +
            "select '微信' as src, `date`,order_num,pay_order_num, pay_order_amount, refund_win_num, refund_win_amount, pay_use_num,pay_profit_amount, " +
            "refund_success_amount from daily_service_transaction_wechat where `date` >= ? and `date` <= ?";

    private final static String healthCheckQueryTotalSql = "select '总计' as src ,`date`, sum(order_num) as order_num, sum(pay_order_num) as pay_order_num, " +
            "sum(pay_order_amount) as pay_order_amount, sum(refund_win_num) as refund_win_num, sum(refund_win_amount) as refund_win_amount , " +
            "sum(pay_use_num) as pay_use_num, sum(pay_profit_amount) as pay_profit_amount, sum(refund_success_amount) as refund_success_amount " +
            "from (  select `date`,order_num, pay_order_num , pay_order_amount, refund_win_num,refund_win_amount,pay_use_num,pay_profit_amount," +
            "refund_success_amount from ops_service_transaction x where x.facilitator like '%美年%'  and `date` >=  ? and `date` <= ? " +
            "union select `date`,order_num, pay_order_num , pay_order_amount, refund_win_num,refund_win_amount,pay_use_num,pay_profit_amount," +
            "refund_success_amount from daily_service_transaction_wechat where `date` >= ? and `date` <= ?) total group by `date`";


    public long getProdRiskEvaluation() {
        long pv = 0;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            pv = bisysDB.queryForObject(
                    "select sum(pv) as pv from prod_risk_evaluation where trace_id = '首页_风险评估'",
                    (resultSet, i) -> resultSet.getLong("pv"));
        } catch (Exception ex) {
            logger.debug("getProdRiskEvaluation", ex);
        }
        return pv;
    }

    public void updateMallOrderInput(OpsMallOrder mallOrder) {
        if (mallOrder.isInputMallOrder()) {
            String updateTime = JavaUtils.getCurrent();
            bisysDB.update(dailyMallOrderInputUpdateSQL, mallOrder.getDate(), mallOrder.getGenre(), mallOrder.getOrderNum(), mallOrder.getOrderAmount(),
                    mallOrder.getPayOrderNum(), mallOrder.getPayOrderAmount(), mallOrder.getApplyRefundOrderNum(), mallOrder.getApplyRefundOrderAmount(),
                    mallOrder.getRefundOrderNum(), mallOrder.getRefundOrderAmount(), mallOrder.getRefundGrossProfit(), mallOrder.getGrossProfit(),
                    mallOrder.getGrossProfitRate(), mallOrder.getRefundRate(), mallOrder.getPayConversionRate(), updateTime, mallOrder.getOrderNum(), mallOrder.getOrderAmount(),
                    mallOrder.getPayOrderNum(), mallOrder.getPayOrderAmount(), mallOrder.getApplyRefundOrderNum(), mallOrder.getApplyRefundOrderAmount(),
                    mallOrder.getRefundOrderNum(), mallOrder.getRefundOrderAmount(), mallOrder.getRefundGrossProfit(), mallOrder.getGrossProfit(),
                    mallOrder.getGrossProfitRate(), mallOrder.getRefundRate(), mallOrder.getPayConversionRate(), updateTime);
        }
    }

    public void updateServiceTransactionWeChat(HealthCheck healthCheck) {
        String updateTime = JavaUtils.getCurrent();
        bisysDB.update(serviceTransactionWeChatUpdateSQL, healthCheck.getDate(), healthCheck.getOrderNum(),
                healthCheck.getPayOrderNum(), healthCheck.getPayOrderAmount(), healthCheck.getRefundWinNum(),
                healthCheck.getRefundWinAmount(), healthCheck.getPayUseNum(), healthCheck.getPayProfitAmount(),
                healthCheck.getRefundSuccessAmount(), updateTime, healthCheck.getOrderNum(),
                healthCheck.getPayOrderNum(), healthCheck.getPayOrderAmount(), healthCheck.getRefundWinNum(),
                healthCheck.getRefundWinAmount(), healthCheck.getPayUseNum(), healthCheck.getPayProfitAmount(),
                healthCheck.getRefundSuccessAmount(), updateTime);
    }

    private static final String youAppUpdateSQL = "INSERT INTO `daily_app` (`date`, `os`, `download_users`, `total_download_users`, `active_users`," +
            " `start_num`, `update_time`) VALUES(?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `download_users` = ?, " +
            "`total_download_users` = ?, `active_users` = ?, `start_num` = ?, `update_time` = ?";

    public String updateYouApp(YouApp youApp) {
        String updateTime = JavaUtils.getCurrent();
        if (youApp.getOs() == 1 || youApp.getOs() == 2) {
            bisysDB.update(youAppUpdateSQL, youApp.getDate(), youApp.getOs(), youApp.getDownloadUsers(), youApp.getTotalDownloadUsers(),
                    youApp.getActiveUsers(), youApp.getStartNum(), updateTime, youApp.getDownloadUsers(), youApp.getTotalDownloadUsers(),
                    youApp.getActiveUsers(), youApp.getStartNum(), updateTime);
            return "success!";
        } else {
            return "os字段只能是 1：Android，2:iOS";
        }
    }

    private static final String registerUpdateSQL = "INSERT INTO `daily_register` (`date`, `register_users`, `total_register_users`," +
            " `download_unregister`, `update_time`) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `register_users` = ?, `total_register_users` = ?, `download_unregister` = ?, `update_time` = ?";

    public void updateRegister(Register register) {
        String updateTime = JavaUtils.getCurrent();
        bisysDB.update(registerUpdateSQL, register.getDate(), register.getRegisterUsers(), register.getTotalRegisterUsers(),
                register.getDownloadUnregister(), updateTime, register.getRegisterUsers(), register.getTotalRegisterUsers(),
                register.getDownloadUnregister(), updateTime);
    }

    public List<OpsMallOrder> getOpsMallOrder(int typeId, String date, String endDate) {
        List<OpsMallOrder> list = null;
        boolean isInputMallOrder = OpsMallOrder.isInputMallOrder(typeId);
        try {
            String sql = isInputMallOrder ? dailyMallOrderInputQuerySQL : opsMallOrderQuerySQL;
            Object[] params = new Object[]{date, endDate, OpsMallOrder.getGenre(typeId)};

            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(sql, params,
                    (resultSet, i) -> {
                        OpsMallOrder mallOrder = new OpsMallOrder(
                                typeId,
                                resultSet.getString("date"),
                                resultSet.getInt("order_num"),
                                resultSet.getDouble("order_amount"),
                                resultSet.getInt("pay_order_num"),
                                resultSet.getDouble("pay_order_amount"),
                                resultSet.getInt("apply_refund_order_num"),
                                resultSet.getDouble("apply_refund_order_amount"),
                                resultSet.getInt("refund_order_num"),
                                resultSet.getDouble("refund_order_amount")
                        );

                        if (isInputMallOrder) {
                            mallOrder.setRefundGrossProfit(resultSet.getDouble("refund_gross_profit"));
                            mallOrder.setGrossProfit(resultSet.getDouble("gross_profit"));
                            mallOrder.setGrossProfitRate(resultSet.getDouble("gross_profit_rate"));
                            mallOrder.setRefundRate(resultSet.getDouble("refund_rate"));
                            mallOrder.setPayConversionRate(resultSet.getDouble("pay_conversion_rate"));
                        }
                        return mallOrder;
                    }
            );
        } catch (Exception ex) {
            logger.error("getOpsMallOrder error", ex);
        }
        return list;
    }


    public List<Sms> getSms(String date, String endDate) {
        List<Sms> list = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(smsQuerySQL, new Object[]{date, endDate},
                    (resultSet, i) -> {
                        Sms sms = new Sms();
                        sms.setDate(resultSet.getString("date"));
                        sms.setFactorySmsNum(resultSet.getInt("factory_sms_num"));
                        sms.setOldUserNum(resultSet.getInt("old_user_num"));
                        sms.setOneRate(resultSet.getDouble("one_rate"));
                        sms.setOneSmsCost(resultSet.getDouble("one_sms_cost"));
                        sms.setOneSmsNum(resultSet.getInt("one_sms_num"));
                        sms.setOneSmsRegisterNum(resultSet.getInt("one_sms_register_num"));
                        sms.setSmsRegisterNum(resultSet.getInt("sms_register_num"));
                        sms.setTwoRate(resultSet.getDouble("two_rate"));
                        sms.setTwoSmsCost(resultSet.getDouble("two_sms_cost"));
                        sms.setTwoSmsNum(resultSet.getInt("two_sms_num"));
                        sms.setTwoSmsRegisterNum(resultSet.getInt("two_sms_register_num"));
                        return sms;
                    });
        } catch (Exception ex) {
            logger.error("getBuBuBao error", ex);
        }
        return list;
    }

    public List<YouApp> getYouApp(String date, String endDate) {
        List<YouApp> list = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(dailyYouAppQuerySQL, new Object[]{date, endDate},
                    (resultSet, i) -> {
                        YouApp youApp = new YouApp();
                        youApp.setActiveUsers(resultSet.getInt("active_users"));
                        youApp.setDate(resultSet.getString("date"));
                        youApp.setDownloadUsers(resultSet.getInt("download_users"));
                        youApp.setOs(resultSet.getInt("os"));
                        youApp.setStartNum(resultSet.getInt("start_num"));
                        youApp.setTotalDownloadUsers(resultSet.getInt("total_download_users"));
                        return youApp;
                    });
        } catch (Exception ex) {
            logger.error("getYouApp error", ex);
        }
        return list;
    }


    public List<Register> getRegister(String date, String endDate) {
        List<Register> list = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(dailyRegisterQuerySQL, new Object[]{date, endDate},
                    (resultSet, i) -> {
                        Register register = new Register();
                        register.setDate(resultSet.getString("date"));
                        register.setDownloadUsers(resultSet.getInt("download_users"));
                        register.setTotalDownloadUsers(resultSet.getInt("total_download_users"));
                        register.setActiveUsers(resultSet.getInt("active_users"));
                        register.setStartNum(resultSet.getInt("start_num"));
                        register.setRegisterUsers(resultSet.getInt("register_users"));
                        register.setTotalRegisterUsers(resultSet.getInt("total_register_users"));
                        register.setDownloadUnregister(resultSet.getInt("download_unregister"));
                        return register;
                    });
        } catch (Exception ex) {
            logger.error("getRegister error", ex);
        }
        return list;
    }

    private static final String buBuBaoQuerySQL = "select date, banner_num, click_num, policy_num, policy_rate " +
            "from manage_policy_stat where date >= ? and date <=? ";

    public List<ManagePolicyStat> getBuBuBao(String date, String endDate) {
        List<ManagePolicyStat> list = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(buBuBaoQuerySQL, new Object[]{date, endDate},
                    (resultSet, i) ->
                            new ManagePolicyStat(
                                    resultSet.getString("date"),
                                    resultSet.getInt("banner_num"),
                                    resultSet.getInt("click_num"),
                                    resultSet.getInt("policy_num"),
                                    resultSet.getDouble("policy_rate")
                            ));
        } catch (Exception ex) {
            logger.error("getBuBuBao error", ex);
        }
        return list;
    }

    public List<HealthCheck> getHealthCheck(boolean isTotal, String date, String endDate) {
        List<HealthCheck> list = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(isTotal ? healthCheckQueryTotalSql : healthCheckQuerySql, new Object[]{date, endDate, date, endDate},
                    (resultSet, i) -> {
                        HealthCheck healthCheck = new HealthCheck();
                        healthCheck.setSrc(resultSet.getString("src"));
                        healthCheck.setDate(resultSet.getString("date"));
                        healthCheck.setOrderNum(resultSet.getInt("order_num"));
                        healthCheck.setPayOrderNum(resultSet.getInt("pay_order_num"));
                        healthCheck.setPayOrderAmount(resultSet.getDouble("pay_order_amount"));
                        healthCheck.setRefundWinNum(resultSet.getInt("refund_win_num"));
                        healthCheck.setRefundWinAmount(resultSet.getDouble("refund_win_amount"));
                        healthCheck.setPayUseNum(resultSet.getInt("pay_use_num"));
                        healthCheck.setPayProfitAmount(resultSet.getDouble("pay_profit_amount"));
                        healthCheck.setRefundSuccessAmount(resultSet.getDouble("refund_success_amount"));
                        return healthCheck;
                    }
            );
        } catch (Exception ex) {
            logger.error("getHealthCheck error", ex);
        }
        return list;
    }

    private static final String uplusGoodsQuerySQL = "select goods_name, order_num, order_amount from manage_uplus_goods where date = ? ";

    public List<UplusGoods> getUplusGoods() {
        List<UplusGoods> list = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(uplusGoodsQuerySQL , new Object[]{JavaUtils.getSeveralDaysAgo(1)},
                    (resultSet, i) ->
                            new UplusGoods(
                                    resultSet.getString("goods_name"),
                                    resultSet.getInt("order_num"),
                                    resultSet.getDouble("order_amount")));
        } catch (Exception ex) {
            logger.error("getUplusGoods error", ex);
        }
        return list;
    }

    private static final String userRetentionQuerySQL = "SELECT `date`, after_day_1, after_day_3, after_day_7 from boss_user_day_retention where  `date` >= ? and `date` <= ?";

    public List<UserRetention> getUserRetention(String date, String endDate) {
        List<UserRetention> list = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(userRetentionQuerySQL, new Object[]{date, endDate},
                    (resultSet, i) ->
                  new UserRetention(
                          resultSet.getString("date"),
                          resultSet.getDouble("after_day_1"),
                          resultSet.getDouble("after_day_3"),
                          resultSet.getDouble("after_day_7"))
            );
        } catch (Exception ex) {
            logger.error("getUserRetention error", ex);
        }
        return list;
    }

    private static final String accessDataQuerySQL = "select `date`,'详情页相关推荐' as source, sum(pv) as pv, sum(uv) as uv from content_info_data x " +
            " where x.source like '%详情页%' and `date` >= ? and `date` <= ? group by `date` " +
            " union " +
            " select `date`, " +
            " case source when '搜索结果页' then '搜索导入流量' else source END as source, " +
            " pv, uv from content_info_data x where x.source  in ('优知','搜索结果页','全部') and `date` >= ? and `date` <= ?";


    public List<AccessData> getAccessData(String date, String endDate) {
        List<AccessData> list = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query( accessDataQuerySQL, new Object[]{date, endDate, date, endDate},
                    (resultSet, i) ->
                            new AccessData(
                                    resultSet.getString("date"),
                                    resultSet.getString("source"),
                                    resultSet.getInt("pv"),
                                    resultSet.getInt("uv"))
            );
        } catch (Exception ex) {
            logger.error("getAccessData error", ex);
        }
        return list;
    }

    private static final String smsCityQuerySQL = "select city_name,sms_one_num,sms_one_register_num,old_num,sms_one_rate,sms_one_cost,sms_two_num, " +
            "sms_two_register_num,sms_two_rate,sms_two_cost,sms_register_num,sms_cost,sms_rate from ops_factory_city " +
            " where `date` = (select max(`date`) from ops_factory_city) and city_name != ''";

    public List<SmsCity> getSmsCity() {
        List<SmsCity> list = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(smsCityQuerySQL, new Object[]{},
                    (resultSet, i) ->{
                        SmsCity smsCity = new SmsCity();
                        smsCity.setCityName(resultSet.getString("city_name"));
                        smsCity.setOneSmsNum(resultSet.getInt("sms_one_num"));
                        smsCity.setOneSmsRegisterNum(resultSet.getInt("sms_one_register_num"));
                        smsCity.setOldUserNum(resultSet.getInt("old_num"));
                        smsCity.setOneRate(resultSet.getDouble("sms_one_rate"));
                        smsCity.setOneSmsCost(resultSet.getDouble("sms_one_cost"));
                        smsCity.setTwoSmsNum(resultSet.getInt("sms_two_num"));
                        smsCity.setTwoSmsRegisterNum(resultSet.getInt("sms_two_register_num"));
                        smsCity.setTwoRate(resultSet.getDouble("sms_two_rate"));
                        smsCity.setTwoSmsCost(resultSet.getDouble("sms_two_cost"));
                        smsCity.setSmsRegisterNum(resultSet.getInt("sms_register_num"));
                        smsCity.setTotalCost(resultSet.getDouble("sms_cost"));
                        smsCity.setTotalRate(resultSet.getDouble("sms_rate"));
                        return smsCity;
                    }

            );
        } catch (Exception ex) {
            logger.error("getSmsCity error", ex);
        }
        return list;
    }

}
