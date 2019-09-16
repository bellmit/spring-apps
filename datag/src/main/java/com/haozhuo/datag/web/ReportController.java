package com.haozhuo.datag.web;

import com.haozhuo.datag.model.report.HongKang;
import com.haozhuo.datag.model.report.RepAbnormal;
import com.haozhuo.datag.service.HbaseService;
import com.haozhuo.datag.service.Insurance.Hongkang;
import com.haozhuo.datag.service.Insurance.Mayi;
import com.haozhuo.datag.service.Insurance.WeiBao;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequestMapping(value = "/report")
@RestController
public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    @Autowired
    private HbaseService hbaseService;
    @Autowired
    private Hongkang hongkang;
    @Autowired
    private Mayi mayi;
    @Autowired
    private WeiBao weiBao;

    @GetMapping(value = "/body/reportId/{reportId}")
    @ApiOperation(value = "根据报告Id返回人体图数据", notes = "返回数据：item:18项之一，flag：1=异常，0=无异常")
    public Object getBodyByReportId(@PathVariable(value = "reportId") String reportId) {
        return hbaseService.getBodyById(reportId);
    }

    @GetMapping(value = "/ant/{idcard}")
    @ApiOperation(value = "蚂蚁保险返回值")
    public RepAbnormal getAnt(@PathVariable(value = "idcard") String idcard) {
        RepAbnormal insurance = hbaseService.insurance(idcard);
        return mayi.getAbnormalValue(idcard);
    }
    @GetMapping(value = "/antvalue/{idcard}")
    @ApiOperation(value = "蚂蚁保险返回值")
    public RepAbnormal getAnt1(@PathVariable(value = "idcard") String idcard) {
        RepAbnormal insurance = hbaseService.insurance(idcard);
        return mayi.insurance(idcard);
    }

    @GetMapping(value = "/hongkang/{idcard}")
    @ApiOperation(value = "弘康保险返回值")
    public HongKang getTest(@PathVariable(value = "idcard") String idcard) {

        return hongkang.getAbnormalValueForHongkang(idcard);
    }

    @GetMapping(value = "/hongkangvalue/{idcard}")
    @ApiOperation(value = "弘康保险返回值")
    public HongKang getTest1(@PathVariable(value = "idcard") String idcard) {

        return hongkang.getHongkangValue(idcard);
    }

    @GetMapping(value = "/hongkangvaluetest")
    @ApiOperation(value = "弘康保险返回值")
    public void test11() throws IOException {

        hongkang.test();
    }

    @GetMapping(value = "/weibao")
    @ApiOperation(value = "弘康保险返回值")
    public List Weibao(String rptid)  {

      return  weiBao.getRep1(rptid);
    }

}
