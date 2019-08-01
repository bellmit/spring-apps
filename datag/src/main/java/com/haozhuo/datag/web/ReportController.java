package com.haozhuo.datag.web;

import com.haozhuo.datag.model.report.RepAbnormal;
import com.haozhuo.datag.service.DataEtlJdbcService;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.HbaseService;
import com.haozhuo.datag.util.SqlSplit;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RequestMapping(value = "/report")
@RestController
public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    @Autowired
    private HbaseService hbaseService;
    @GetMapping(value = "/body/reportId/{reportId}")
    @ApiOperation(value = "根据报告Id返回人体图数据",notes = "返回数据：item:18项之一，flag：1=异常，0=无异常")
    public Object getBodyByReportId(@PathVariable(value = "reportId") String reportId){
        return hbaseService.getBodyById(reportId);
    }

    @GetMapping(value = "/{reportId}")
    @ApiOperation(value = "根据报告Id返回人体图数据",notes = "返回数据：item:18项之一，flag：1=异常，0=无异常")
    public RepAbnormal getTest(@PathVariable(value = "reportId") String reportId){
        return hbaseService.insurance(reportId);
    }

/*    @GetMapping(value = "/test/{reportId}")
    @ApiOperation(value = "根据报告Id返回人体图数据",notes = "返回数据：item:18项之一，flag：1=异常，0=无异常")
    public Set getTest1(@PathVariable(value = "reportId") String reportId){
        return hbaseService.getItemByReportId1(reportId);
    }*/
}
