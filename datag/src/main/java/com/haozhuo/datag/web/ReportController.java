package com.haozhuo.datag.web;

import com.haozhuo.datag.common.*;
import com.haozhuo.datag.model.ResponseEntity;
import com.haozhuo.datag.model.ResponseEnum;
import com.haozhuo.datag.model.ReturnValue;
import com.haozhuo.datag.model.report.*;
import com.haozhuo.datag.service.*;
import com.haozhuo.datag.service.Insurance.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

@RequestMapping(value = "/report")
@RestController
public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    @Autowired
    private HbaseService hbaseService;
    @Autowired
    private WeiBao weiBao;
    @Autowired
    private UserReport userReport;
    @Autowired
    private EsService esService;
    @Autowired
    private RptStdService rptStdService;

    @Autowired
    private YjkHReportService yjkHReportService;
    @Autowired
    private FileService fileService;

    @GetMapping(value = "/body/reportId/{reportId}")
    @ApiOperation(value = "根据报告Id返回人体图数据", notes = "返回数据：item:18项之一，flag：1=异常，0=无异常")
    public Object getBodyByReportId(@PathVariable(value = "reportId") String reportId) {
        return hbaseService.getBodyById(reportId);
    }

    @GetMapping(value = "/getResultAndDesc/{reportId}")
    public Object getDescByReportId(@PathVariable(value = "reportId") String reportId) {
        return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(),esService.getChkitems(reportId));

       // return esService.getChkitems(reportId);
    }

    @GetMapping(value = "/getStdRpt/{reportId}")
    @ApiOperation(value = "根据报告Id返回标准化报告")
    public Object getStdRptByReportId(@PathVariable(value = "reportId") String reportId) {
        return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(),esService.getStdReport(reportId));
        // return esService.getChkitems(reportId);
    }



    @GetMapping(value = "/weibao/{rptid}")
    @ApiOperation(value = "微保")
    public ResponseEntity<WeiBaoM> Weibao(@PathVariable(value = "rptid") String rptid) {
        Msg1 rep1 = weiBao.getRep1(rptid);


        return new ResponseEntity<>(rep1.getCode() == 0 ? ResultCodeBase.CODE_SUCCESS : rep1.getCode(), StringUtil.isEmpty(rep1.getMsg()) ? TipConstBase.OPERATION_SAVE_SUCCESS1 : rep1.getMsg(), rep1.getWeiBaoM());
    }


    @PostMapping(value = "/rptstd")
    @ApiOperation(value = "报告标准化")
    public ResponseEntity rptStd(@RequestParam String  rptjson) {
         //System.out.println(rptStdPo.getJsonstr());
        return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(),rptStdService.rptStd(rptjson));
    }

    @GetMapping(value = "/std/deep")
    @ApiOperation(value = "报告标准化")
    public ResponseEntity rptStdV2(@RequestParam(value = "rptjson",required = false, defaultValue = "") String  rptjson,@RequestParam(value = "rptid",required = false, defaultValue = "")String rptid) {
        //System.out.println(rptStdPo.getJsonstr());
        logger.info("============>"+Thread.currentThread().getName());
        if(rptid.isEmpty()&&rptjson.isEmpty()){
            return new ResponseEntity<>(ResponseEnum.PARAM_ERROR.getCode(), ResponseEnum.PARAM_ERROR.getMsg(),"");
        }else if(!rptid.isEmpty()){

            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(),yjkHReportService.rptStdSd(rptid));

        }else {
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(),rptStdService.rptStdSd(rptjson));
        }
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "uId",value = "上传用户ID",paramType = "query",required = true,dataType = "int")
    })
    @PostMapping(value = "/uploadFile", headers = "content-type=multipart/form-data")
    @ApiOperation(value = "通过文件上传报告id")
    public ReturnValue uploadFileTest(HttpServletRequest request, @RequestParam(value = "uId", required = true) String uId, @ApiParam(value = "file",  required = true) MultipartFile file) throws IOException {
        return fileService.uploadFileTest(file);
    }







    @GetMapping(value = "/weibao")
    @ApiOperation(value = "测试")
    public void test() throws IOException {

        userReport.test3();
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
    public ResponseEntity pushall(@RequestParam(value = "rptid") String rptid,
                                  @RequestParam(value = "age") int age) {


        return userReport.GetInsurance(rptid,age);
    }

/*
    @GetMapping(value = "/pushtest")
    @ApiOperation(value = "测试")
    public void test() throws IOException {


        userReport.test();
    }
*/

    @GetMapping(value = "/haskey")
    public String haskey(
                               @RequestParam(value = "rptid") String rptid) {

        return userReport.getrowkey(rptid);
    }

    @GetMapping(value = "/comport")
    public List<ReVO> getday( @RequestParam(value = "rptid") String rptid){

        return esService.query1(rptid);
    }

}
