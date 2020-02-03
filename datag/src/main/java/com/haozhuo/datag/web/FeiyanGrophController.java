package com.haozhuo.datag.web;

import com.haozhuo.datag.model.ResponseEntity;
import com.haozhuo.datag.model.ResponseEnum;
import com.haozhuo.datag.service.Virus.GetVirusGraph;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RequestMapping(value = "/feiyan")
@RestController
public class FeiyanGrophController {

    @Autowired
    GetVirusGraph getVirusGraph;

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

    @GetMapping("/linechart")
    public ResponseEntity getGraph(@RequestParam(value = "id") int id){
        if (id ==1){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getFirstGraph());
        }else if(id ==2){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getSecondGraph());
        }else if(id ==3){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getThridGraph());
        }else if(id ==4){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getNotWhAll());
        }else if(id ==11){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getAllSiWang());
        }else {
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), "id错误");
        }

    }
}
