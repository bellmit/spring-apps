package com.haozhuo.rcmd.web;

import com.haozhuo.rcmd.model.glu.ReportObjData;
import com.haozhuo.rcmd.service.GluService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * Created by Lucius on 8/27/18.
 * https://javadeveloperzone.com/spring-boot/spring-boot-resttemplate-example/
 * 高血糖准入
 */

@RequestMapping(value = "/glu")
@RestController
public class GluController {
    private static final Logger logger = LoggerFactory.getLogger(GluController.class);
    @Autowired
    private GluService gluService;

    @GetMapping("/highGluPermit/{reportId}")
    @ApiOperation(value = "输入reportId，判断该报告是否符合高血糖准入,返回1表示准入,0表示不准入", notes = "")
    public Object getGoodsIdsByUserId(@PathVariable(value = "reportId") String reportId) {
        long beginTime = System.currentTimeMillis();
        logger.info(reportId);
        ReportObjData reportObjData = gluService.getAndParseReport(reportId);

        Boolean isLegal = gluService.isLegal(reportObjData);

        return isLegal?1:0;
    }

}
