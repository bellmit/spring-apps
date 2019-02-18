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

    private final static String dailyYouAppQuerySQL =  "select `date`, os, download_users, total_download_users, active_users," +
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
            bisysDB.update(serviceTransactionWeChatUpdateSQL,healthCheck.getDate(), healthCheck.getOrderNum(),
                    healthCheck.getPayOrderNum(),healthCheck.getPayOrderAmount(),healthCheck.getRefundWinNum(),
                    healthCheck.getRefundWinAmount(), healthCheck.getPayUseNum(),healthCheck.getPayProfitAmount(),
                    healthCheck.getRefundSuccessAmount(), updateTime, healthCheck.getOrderNum(),
                    healthCheck.getPayOrderNum(),healthCheck.getPayOrderAmount(),healthCheck.getRefundWinNum(),
                    healthCheck.getRefundWinAmount(), healthCheck.getPayUseNum(),healthCheck.getPayProfitAmount(),
                    healthCheck.getRefundSuccessAmount(), updateTime);
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


    public List<Sms> getSms(String date, String endDate){
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

    public List<YouApp> getYouApp(String date, String endDate){
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


    public List<Register> getRegister(String date, String endDate){
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

    public List<UplusGoods> getUplusGoods() {
        List<UplusGoods> list = null;
        try {
            String sql;
            Object[] params;

            sql = "select goods_name, order_num, order_amount from manage_uplus_goods where date = ? ";
            params = new Object[]{JavaUtils.getSeveralDaysAgo(1)};

            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(sql, params,
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

}
