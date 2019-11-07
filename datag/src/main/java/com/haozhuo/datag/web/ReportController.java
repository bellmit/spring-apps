package com.haozhuo.datag.web;

import com.haozhuo.datag.common.*;
import com.haozhuo.datag.model.ResponseEntity;
import com.haozhuo.datag.model.report.*;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.HbaseService;
import com.haozhuo.datag.service.Insurance.*;
import io.swagger.annotations.ApiOperation;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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
    @Autowired
    private UserReport userReport;
    @Autowired
    private PushGan pushGan;
    @Autowired
    private EsService esService;

    @GetMapping(value = "/body/reportId/{reportId}")
    @ApiOperation(value = "根据报告Id返回人体图数据", notes = "返回数据：item:18项之一，flag：1=异常，0=无异常")
    public Object getBodyByReportId(@PathVariable(value = "reportId") String reportId) {
        return hbaseService.getBodyById(reportId);
    }

/*    @GetMapping(value = "/ant/{idcard}")
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
    }*/

/*    @GetMapping(value = "/hongkangvaluetest")
    @ApiOperation(value = "弘康保险返回值")
    public void test11() throws IOException {

        hongkang.test();
    }*/

    @GetMapping(value = "/weibao/{rptid}")
    @ApiOperation(value = "微保")
    public ResponseEntity<WeiBaoM> Weibao(@PathVariable(value = "rptid") String rptid) {
        Msg1 rep1 = weiBao.getRep1(rptid);


        return new ResponseEntity<>(rep1.getCode() == 0 ? ResultCodeBase.CODE_SUCCESS : rep1.getCode(), StringUtil.isEmpty(rep1.getMsg()) ? TipConstBase.OPERATION_SAVE_SUCCESS1 : rep1.getMsg(), rep1.getWeiBaoM());
    }

    @GetMapping(value = "/weibao")
    @ApiOperation(value = "微保")
    public void Weibao() throws IOException {

        weiBao.test();
    }

    @PostMapping(value = "/push")
    @ApiOperation(value = "推送")
    public Msg PushInsuranceForpost(@RequestBody PushBody pushBody) throws UnsupportedEncodingException {


        //Utf8 utf8 = new Utf8();
        //String s = utf8.convertPercent(label);
        return userReport.Push(pushBody.getRptid(), pushBody.getLabel(),pushBody.getAge());
    }

    @GetMapping(value = "/push")
    @ApiOperation(value = "推送")
    public Msg PushInsuranceForget(@RequestParam(value = "rptid") String rptid,
                                   @RequestParam(value = "label") String label,
                                   @RequestParam(value = "age") Integer age) throws UnsupportedEncodingException {
        //System.out.println();

        Utf8 utf8 = new Utf8();
        String s = utf8.convertPercent(label);
        return userReport.Push(rptid, URLDecoder.decode(s, "utf8"),age);
    }

    @GetMapping(value = "/push1")
    @ApiOperation(value = "报告查询")
    public InsuranceMap PushInsurance(@RequestParam(value = "rptid") String rptid) {


        return userReport.UserRep(rptid);
    }

    @GetMapping(value = "/pushall")
    @ApiOperation(value = "推送甲肝保险")
    public ResponseEntity pushall(@RequestParam(value = "rptid") String rptid) {


        return userReport.GetInsurance(rptid);
    }

/*
    @GetMapping(value = "/pushtest")
    @ApiOperation(value = "测试")
    public void test() throws IOException {


        userReport.test();
    }
*/

    @GetMapping(value = "/haskey")
    public InsuranceMap haskey(@RequestParam(value = "listname") String listname,
                               @RequestParam(value = "rptid") String rptid) {
        String day = esService.getlastday(rptid);
        String substring = day.substring(0, 10);
        String rowkey = substring + "_" + rptid;
        String endrowkey = substring + "_" + (Integer.parseInt(rptid) + 1);
        Scan scan = new Scan();
        scan.setStartRow(rowkey.getBytes());
        scan.setStopRow(endrowkey.getBytes());
        return userReport.getRep(scan, listname);
    }

}
