package com.haozhuo.datag.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Lucius on 9/3/18.
 */
public class BisysJdbcService {
    @Qualifier("bisysJdbc") //选择jdbc连接池
    private  JdbcTemplate bisysDB;

}
