package com.haozhuo.datag.web;

import com.haozhuo.datag.service.BisysJdbcService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Lucius on 9/3/18.
 */
@RequestMapping(value = "/bisys")
@RestController
class BisysController {
    private static final Logger logger = LoggerFactory.getLogger(BisysController.class);

    @Autowired
    private BisysJdbcService bisysJdbcService;

    @GetMapping("/ProdRiskEvaluation")
    @ApiOperation(value = "直接调用接口", notes = "业务：查询prod_risk_evaluation的点击首页_风险评估的次数(PV，累加)")
    public Object getProdRiskEvaluation() {
        return bisysJdbcService.getProdRiskEvaluation();
    }

}


