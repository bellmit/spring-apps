package com.haozhuo.datag.web;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/feiyan")
@RestController
public class FeiyanGrophController {



    @Autowired
    @Qualifier("whhaozhuoJdbc")
    private JdbcTemplate whDB;


    @GetMapping("/getGrouph")
    @ApiOperation(value = "直接调用接口", notes = "获取web图json")
    public String getGrouph() {

        String sql = "select increase_case from fight2feiyan where now_time=(select max(now_time) from fight2feiyan)";

        SqlRowSet sqlRowSet = whDB.queryForRowSet(sql);
        String aCase ="";
        while (sqlRowSet.next()){
            aCase = sqlRowSet.getString("increase_case");
        }


        return aCase;
    }
}
