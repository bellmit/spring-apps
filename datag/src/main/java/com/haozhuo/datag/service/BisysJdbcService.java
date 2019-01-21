package com.haozhuo.datag.service;

import com.haozhuo.datag.common.JavaUtils;
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

    public List<OpsMallOrder> getOpsMallOrder(int id, String date, String endDate) {
        List<OpsMallOrder> list = null;
        try {
            String sql;
            Object[] params;

            sql = "select date, order_num, order_amount, pay_order_num, pay_order_amount, apply_order_num, " +
                    " apply_order_amount,refund_order_num,refund_order_amount from ops_mall_order where date >= ? and date <=? and genre=?";
            params = new Object[]{date, endDate, OpsMallOrder.getGenre(id)};

            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(sql, params,
                    (resultSet, i) ->
                            new OpsMallOrder(
                            id,
                            resultSet.getString("date"),
                            resultSet.getInt("order_num"),
                            resultSet.getDouble("order_amount"),
                            resultSet.getInt("pay_order_num"),
                            resultSet.getDouble("pay_order_amount"),
                            resultSet.getInt("apply_order_num"),
                            resultSet.getDouble("apply_order_amount"),
                            resultSet.getInt("refund_order_num"),
                            resultSet.getDouble("refund_order_amount")
                    ));
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
