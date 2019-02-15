package com.haozhuo.datag.service;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.model.bisys.HealthCheck;
import com.haozhuo.datag.model.bisys.ManagePolicyStat;
import com.haozhuo.datag.model.bisys.OpsMallOrder;
import com.haozhuo.datag.model.bisys.UplusGoods;
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


    private final static String opsMallOrderQuerySql = "select date, order_num, order_amount, pay_order_num, pay_order_amount, apply_order_num as apply_refund_order_num, " +
            " apply_order_amount as apply_refund_order_amount ,refund_order_num,refund_order_amount from ops_mall_order where date >= ? and date <=? and genre=?";

    private final static String opsMallOrderInputQuerySql = "select date, order_num, order_amount, pay_order_num, pay_order_amount, " +
            " ifnull(apply_refund_order_num, -1) as apply_refund_order_num, ifnull(apply_refund_order_amount, -1) as apply_refund_order_amount," +
            " ifnull(refund_order_num, -1) as refund_order_num, ifnull(refund_order_amount, -1) as refund_order_amount , " +
            " ifnull(refund_gross_profit, -1) as  refund_gross_profit, ifnull(gross_profit, -1) as gross_profit, " +
            " ifnull(gross_profit_rate, -1) as gross_profit_rate, ifnull(refund_rate, -1) as refund_rate, " +
            " ifnull(pay_conversion_rate, -1) as  pay_conversion_rate from ops_mall_order_input where date >= ? and date <=? and genre=?";

    private final static String mallOrderInputUpdateSQL =
            "INSERT INTO `ops_mall_order_input` (`date`, `genre`, `order_num`, `order_amount`, `pay_order_num`, `pay_order_amount`, " +
                    "`apply_refund_order_num`, `apply_refund_order_amount`, `refund_order_num`, `refund_order_amount`, `refund_gross_profit`, " +
                    "`gross_profit`, `gross_profit_rate`, `refund_rate`, `pay_conversion_rate`, `update_time`) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY " +
                    "UPDATE `order_num` = ?, `order_amount` = ?, `pay_order_num` = ?, `pay_order_amount` = ?, `apply_refund_order_num` = ?, " +
                    "`apply_refund_order_amount` = ?, `refund_order_num` = ?, `refund_order_amount` = ?, `refund_gross_profit` = ?, " +
                    "`gross_profit` = ?, `gross_profit_rate` = ?, `refund_rate` = ?, `pay_conversion_rate` = ?, `update_time` = ?";

    public void updateMallOrderInput(OpsMallOrder mallOrder) {
        if (mallOrder.isInputMallOrder()) {
            String updateTime = JavaUtils.getCurrent();
            bisysDB.update(mallOrderInputUpdateSQL, mallOrder.getDate(), mallOrder.getGenre(), mallOrder.getOrderNum(), mallOrder.getOrderAmount(),
                    mallOrder.getPayOrderNum(), mallOrder.getPayOrderAmount(), mallOrder.getApplyRefundOrderNum(), mallOrder.getApplyRefundOrderAmount(),
                    mallOrder.getRefundOrderNum(), mallOrder.getRefundOrderAmount(), mallOrder.getRefundGrossProfit(), mallOrder.getGrossProfit(),
                    mallOrder.getGrossProfitRate(), mallOrder.getRefundRate(), mallOrder.getPayConversionRate(), updateTime, mallOrder.getOrderNum(), mallOrder.getOrderAmount(),
                    mallOrder.getPayOrderNum(), mallOrder.getPayOrderAmount(), mallOrder.getApplyRefundOrderNum(), mallOrder.getApplyRefundOrderAmount(),
                    mallOrder.getRefundOrderNum(), mallOrder.getRefundOrderAmount(), mallOrder.getRefundGrossProfit(), mallOrder.getGrossProfit(),
                    mallOrder.getGrossProfitRate(), mallOrder.getRefundRate(), mallOrder.getPayConversionRate(), updateTime);
        }
    }

    public List<OpsMallOrder> getOpsMallOrder(int typeId, String date, String endDate) {
        List<OpsMallOrder> list = null;
        boolean isInputMallOrder = OpsMallOrder.isInputMallOrder(typeId);
        try {
            String sql = isInputMallOrder ? opsMallOrderInputQuerySql : opsMallOrderQuerySql;
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
            logger.debug("getOpsMallOrder", ex);
        }
        return list;
    }

    public List<ManagePolicyStat> getBuBuBao(String date, String endDate) {
        List<ManagePolicyStat> list = null;
        try {
            String sql;
            Object[] params;

            sql = "select date, banner_num, click_num, policy_num, policy_rate from manage_policy_stat where date >= ? and date <=? ";
            params = new Object[]{date, endDate};

            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(sql, params,
                    (resultSet, i) ->
                            new ManagePolicyStat(
                                    resultSet.getString("date"),
                                    resultSet.getInt("banner_num"),
                                    resultSet.getInt("click_num"),
                                    resultSet.getInt("policy_num"),
                                    resultSet.getDouble("policy_rate")
                            ));
        } catch (Exception ex) {
            logger.debug("getBuBuBao", ex);
        }
        return list;
    }

    private final static String healthCheckQuerySql = "select 'App' as src, `date`, sum(order_num) as order_num, sum(pay_order_num) as pay_order_num, " +
            "sum(pay_order_amount) as pay_order_amount, sum(refund_win_num) as refund_win_num, sum(refund_win_amount) as refund_win_amount , " +
            "sum(pay_use_num) as pay_use_num, sum(pay_profit_amount) as pay_profit_amount, sum(refund_success_amount) as refund_success_amount  " +
            "from ops_service_transaction x where x.facilitator like '%美年%'  and `date` >= ? and `date` <= ? group by `date` " +
            "union " +
            "select '微信' as src, `date`,order_num,pay_order_num, pay_order_amount, refund_win_num, refund_win_amount, pay_use_num,pay_profit_amount, " +
            "refund_success_amount from ops_service_transaction_wechat where `date` >= ? and `date` <= ?";

    private final static String healthCheckQueryTotalSql = "select '总计' as src ,`date`, sum(order_num) as order_num, sum(pay_order_num) as pay_order_num, " +
            "sum(pay_order_amount) as pay_order_amount, sum(refund_win_num) as refund_win_num, sum(refund_win_amount) as refund_win_amount , " +
            "sum(pay_use_num) as pay_use_num, sum(pay_profit_amount) as pay_profit_amount, sum(refund_success_amount) as refund_success_amount " +
            "from (  select `date`,order_num, pay_order_num , pay_order_amount, refund_win_num,refund_win_amount,pay_use_num,pay_profit_amount," +
            "refund_success_amount from ops_service_transaction x where x.facilitator like '%美年%'  and `date` >=  ? and `date` <= ? " +
            "union select `date`,order_num, pay_order_num , pay_order_amount, refund_win_num,refund_win_amount,pay_use_num,pay_profit_amount," +
            "refund_success_amount from ops_service_transaction_wechat where `date` >= ? and `date` <= ?) total group by `date`";

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
            logger.debug("getUplusGoods", ex);
        }
        return list;
    }

}
