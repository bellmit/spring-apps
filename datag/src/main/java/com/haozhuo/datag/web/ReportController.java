package com.haozhuo.datag.web;

import com.haozhuo.datag.common.RedisUtil;
import com.haozhuo.datag.common.ResultCodeBase;
import com.haozhuo.datag.common.StringUtil;
import com.haozhuo.datag.common.TipConstBase;
import com.haozhuo.datag.model.ResponseEntity;
import com.haozhuo.datag.model.report.*;
import com.haozhuo.datag.service.HbaseService;
import com.haozhuo.datag.service.Insurance.*;
import com.haozhuo.datag.service.RedisService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<WeiBaoM> Weibao(@PathVariable(value = "rptid") String rptid)  {
        Msg1 rep1 = weiBao.getRep1(rptid);


        return  new ResponseEntity<>(rep1.getCode()==0 ? ResultCodeBase.CODE_SUCCESS :rep1.getCode(),StringUtil.isEmpty(rep1.getMsg()) ? TipConstBase.OPERATION_SAVE_SUCCESS1 :rep1.getMsg() ,rep1.getWeiBaoM());
    }

    @GetMapping(value = "/weibao")
    @ApiOperation(value = "微保")
    public void Weibao() throws IOException {

        weiBao.test();
    }

    @GetMapping(value = "/push")
    @ApiOperation(value = "推送")
    public Msg PushInsurance(@RequestParam(value = "rptid") String rptid,
                                @RequestParam(value = "label") String label)  {

        return userReport.Push(rptid,label);
    }
/*    @GetMapping(value = "/haskey")
    public boolean haskey(@RequestParam(value = "key") String key){
        RedisUtil redisUtil = new RedisUtil();
       return redisUtil.hasKey(key);
    }*/

}
