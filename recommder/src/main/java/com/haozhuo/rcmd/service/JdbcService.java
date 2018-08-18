package com.haozhuo.rcmd.service;

import com.haozhuo.common.JavaUtils;
import com.haozhuo.rcmd.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import com.haozhuo.common.Tuple;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Lucius on 8/16/18.
 */
@Component
public class JdbcService {
    private static final Logger logger = LoggerFactory.getLogger(JdbcService.class);

    private final Map<Integer, Integer> categoryIdCountMap;

    private final Map<String, String> diseaseLabelIdNameMap;

    Random rand = new Random();


    @Qualifier("dataetlJdbc") //选择jdbc连接池
    private final JdbcTemplate dataetlDB;

    @Autowired
    public JdbcService(JdbcTemplate jdbcTemplate) {
        this.dataetlDB = jdbcTemplate;
        this.categoryIdCountMap = getCategoryIdCountMap();
        this.diseaseLabelIdNameMap = getDiseaseLabelIdNameMap();
    }

    public List<String> getRandomInfos(int rcmdType, int pageSize, int getNumber) {
        int limitBegin = rand.nextInt(categoryIdCountMap.get(rcmdType) - pageSize);
        return dataetlDB.query(String.format("select x.information_id from article x limit %d, %d", limitBegin, getNumber), new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("information_id");
            }
        });
    }

    public Map<String, Integer> getCategoryNameIdMap() {
        List<Category> list = dataetlDB.query("select name, id from category;", new RowMapper<Category>() {
            @Override
            public Category mapRow(ResultSet resultSet, int i) throws SQLException {
                return new Category(resultSet.getInt("id"), resultSet.getString("name"));
            }
        });

        Map<String, Integer> resultMap = new HashMap<String, Integer>();
        for (Category category : list) {
            resultMap.put(category.getName(), category.getId());
        }
        logger.info("getCategoryNameIdMap:{}", resultMap.toString());
        return resultMap;
    }

    public Map<Integer, Integer> getCategoryIdCountMap() {
        //查询每个频道有多少文章
        Map<Integer, Integer> resultMap = new HashMap<>();

        List<Tuple<Integer, Integer>> list = dataetlDB.query("select id, ifnull(num,0) as num from category y " +
                "left join (select news_category, count(1) as num from article where is_delete = 0 group by news_category) x " +
                "on y.name = x.news_category", new RowMapper<Tuple<Integer, Integer>>() {
            @Override
            public Tuple<Integer, Integer> mapRow(ResultSet resultSet, int i) throws SQLException {
                return new Tuple<Integer, Integer>(resultSet.getInt("id"), resultSet.getInt("num"));
            }
        });

        int totalSize = 0;
        for (Tuple<Integer, Integer> tuple : list) {
            resultMap.put(tuple.getT1(), tuple.getT2());
            totalSize += tuple.getT2();
        }
        resultMap.put(1, totalSize); //推荐的id为1
        logger.info("getCategoryIdCountMap:{}", resultMap.toString());
        return resultMap;
    }

    public Map<String, String> getDiseaseLabelIdNameMap() {
        //查询每个频道有多少文章
        Map<String, String> resultMap = new HashMap<>();

        List<Tuple<String, String>> list = dataetlDB.query("select id, label from disease_label", new RowMapper<Tuple<String, String>>() {
            @Override
            public Tuple<String, String> mapRow(ResultSet resultSet, int i) throws SQLException {
                return new Tuple<String, String>(resultSet.getString("id"), resultSet.getString("label"));
            }
        });

        for (Tuple<String, String> tuple : list) {
            resultMap.put(tuple.getT1(), tuple.getT2());

        }
        return resultMap;
    }

    private List<String> getDynamicLabelIdList(String userId) {
        String dynamicLabelSql = String.format("select distinct label_id from dynamic_userid_label x where x.user_id = '%s' and x.update_time > '%s'", userId, JavaUtils.getNdaysAgo(30));
        return dataetlDB.query(dynamicLabelSql, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("label_id");
            }
        });
    }

    private List<String> getReportLabelIdList(String userId) {
        String reportLabelSql = String.format("select label_ids from report_userid_label where user_id = '%s'", userId);
        String labelIds = dataetlDB.queryForObject(reportLabelSql, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("label_ids");
            }
        });
        return Arrays.asList(labelIds.split(","));
    }


    public Set<String> getLabelsByUserId(String userId) {
        Set<String> labelIdSet = new HashSet<>();
        labelIdSet.addAll(getDynamicLabelIdList(userId));
        labelIdSet.addAll(getReportLabelIdList(userId));
        Set<String> labelNameSet = new HashSet<>();
        for (String labelId : labelIdSet) {
            if (diseaseLabelIdNameMap.containsKey(labelId)) {
                labelNameSet.add(diseaseLabelIdNameMap.get(labelId));
            }
        }
        return labelNameSet;

    }
}
