package com.haozhuo.datag.service;

import com.haozhuo.datag.model.DiseaseNorm;
import com.haozhuo.datag.model.bisys.ContentUrl;
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

    public List<ContentUrl> getContentUrl(String date) {
        List<ContentUrl> list = null;
        try {
            String sql;
            Object[] params;

            if("all".equals(date)){
                sql = "select date, location, url, url_pv, url_uv from content_url";
                params = new Object[]{};
            } else {
                sql = "select date, location, url, url_pv, url_uv from content_url where date = ?";
                params = new Object[]{date};
            }

            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            list = bisysDB.query(sql,params,
                    (resultSet, i) -> new ContentUrl(
                            resultSet.getString("date"),
                            resultSet.getString("location"),
                            resultSet.getString("url"),
                            resultSet.getInt("url_pv"),
                            resultSet.getInt("url_uv")
                    ));
        } catch (Exception ex) {
            logger.debug("getProdRiskEvaluation", ex);
        }
        return list;
    }

}
