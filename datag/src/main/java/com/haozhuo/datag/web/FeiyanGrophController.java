package com.haozhuo.datag.web;

import com.haozhuo.datag.model.ResponseEntity;
import com.haozhuo.datag.model.ResponseEnum;
import com.haozhuo.datag.model.bisys.virus.UpdateData;
import com.haozhuo.datag.service.Virus.GetVirusGraph;
import com.haozhuo.datag.service.Virus.UpdateVirusData;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping(value = "/feiyan")
@RestController
public class FeiyanGrophController {

    @Autowired
    GetVirusGraph getVirusGraph;

    @Autowired
    UpdateVirusData updateVirusData;

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
        }else if(id ==5){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getFifthGraph());
        }else if(id ==6){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getSixthGraph());
        }else if(id ==7){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getSeventhGraph());
        }else if(id ==8){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getEighthGraph());
        }else if(id ==9){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getBl());
        }else if(id ==11){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getAllSiWang());
        }else if(id ==12){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getNum());
        }else if(id ==13){
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), getVirusGraph.getNums());
        }else {
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), "id错误");
        }

    }

    @PostMapping
    public ResponseEntity Update(@RequestBody UpdateData updateData){
        updateVirusData.UpdateData(updateData);
        return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), "修改成功");
    }

    @GetMapping("/getaux")
    public ResponseEntity getAux(){
        return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), updateVirusData.getAus());

    }
}
