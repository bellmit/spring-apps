package com.haozhuo.bisys.service;


import com.haozhuo.bisys.model.OpsChannelNameInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


/**
 * Created by Lucius on 8/16/18.
 */
@Component
public class JdbcService {
    private static final Logger logger = LoggerFactory.getLogger(JdbcService.class);

    @Qualifier("bisysJdbc") //选择jdbc连接池
    @Autowired
    private JdbcTemplate bisysDB;

    public List<OpsChannelNameInfo> getChannelInfo(int channelId) {
        String sql = String.format("select channel_id,channel_name from  ops_channel_name_info where channel_id > %s", channelId);

        List<OpsChannelNameInfo> list =  bisysDB.query(sql, new RowMapper<OpsChannelNameInfo>() {
            @Override
            public  OpsChannelNameInfo mapRow(ResultSet resultSet, int i) throws SQLException {
                OpsChannelNameInfo ocni = new OpsChannelNameInfo();
                ocni.setChannelId(resultSet.getLong("channel_id"));
                ocni.setChannelName(resultSet.getString("channel_name"));
                return ocni;
            }
        });
        return list;
    }

}
