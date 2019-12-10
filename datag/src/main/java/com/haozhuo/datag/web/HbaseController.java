package com.haozhuo.datag.web;

import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.HbaseService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Null;
import java.util.List;

@RequestMapping(value = "/hbase")
@RestController
public class HbaseController {
    @Autowired
   private HbaseService hbaseService;
    @Autowired
    private EsService esService;

    @GetMapping(value = "/getrep")
    @ApiOperation(value = "getrep")
    public List getrep(@RequestParam(value = "rowkey") String rowkey){

        return hbaseService.getrep(rowkey);
    }

    @GetMapping(value = "/delete")
    public void deletees(
            @RequestParam(value = "index")String index,@RequestParam(value = "id")String id){

        esService.deleteIdByQuery1(index, id);
    }
}
