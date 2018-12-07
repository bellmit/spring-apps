package com.haozhuo.datag.web;

import com.haozhuo.datag.model.glu.ReportObjData;
import com.haozhuo.datag.service.DataEtlJdbcService;
import com.haozhuo.datag.service.biz.GluService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * Created by Lucius on 8/27/18.
 * https://javadeveloperzone.com/spring-boot/spring-boot-resttemplate-example/
 * 高血糖准入,业务使按照刘苏的python的permit项目来的。
 */

@RequestMapping(value = "/glu")
@RestController
class GluController {
    private static final Logger logger = LoggerFactory.getLogger(GluController.class);
    @Autowired
    private GluService gluService;
    @Autowired
    private DataEtlJdbcService dataetlJdbcService;

    @GetMapping("/highGluPermit/{reportId}")
    @ApiOperation(value = "输入reportId，判断该报告是否符合高血糖准入,返回1表示准入,0表示不准入")
    public Object getHighGluPermit(@PathVariable(value = "reportId") Long reportId) {
        long beginTime = System.currentTimeMillis();
        ReportObjData reportObjData = gluService.getAndParseReport(reportId);
        int isLegal = gluService.isLegal(reportObjData) ? 1 : 0;
        dataetlJdbcService.updatePermitUsers(reportId, isLegal);
        logger.info("reportId:{},isLegal:{},cost:{}ms", reportId, isLegal, System.currentTimeMillis() - beginTime);
        return isLegal;
    }

}
