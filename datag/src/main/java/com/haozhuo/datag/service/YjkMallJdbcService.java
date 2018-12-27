package com.haozhuo.datag.service;

import com.haozhuo.datag.common.JavaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.joining;


/**
 * Created by Lucius on 9/3/18.
 */
@Component
public class YjkMallJdbcService {
    private static final Logger logger = LoggerFactory.getLogger(YjkMallJdbcService.class);

    @Autowired
    @Qualifier("yjkMallJdbc") //选择jdbc连接池
    private JdbcTemplate yjkMallDB;
    @Value("${app.biz.goods-sales-days:90}")
    private int goodsSalesDays;

    public int getGoodsSaleNum(List<String> goodsIdList) {
        int saleNum = 0;
        String sql = String.format("select ifnull (sum(count),0) as sale_num from mall_order x where x.goods_id in " +
                " (%s) and x.`status` >= 2 and Createtime >='%s'", goodsIdList.stream().collect(joining("','", "'", "'")), JavaUtils.getSeveralDaysAgo(goodsSalesDays));
        logger.info(sql);
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            saleNum = yjkMallDB.queryForObject(sql,
                    (resultSet, i) -> resultSet.getInt("sale_num"));
        } catch (Exception ex) {
            logger.debug("getProdRiskEvaluation", ex);
        }
        return saleNum;
    }

}
