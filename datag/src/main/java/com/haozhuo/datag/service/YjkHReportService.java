package com.haozhuo.datag.service;

import com.haozhuo.datag.com.service.bean.ReportContent;
import com.haozhuo.datag.com.service.stdrpt.UnifMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;



@Component
public class YjkHReportService {

    private static final Logger logger = LoggerFactory.getLogger(com.haozhuo.datag.service.YjkHReportService.class);


    @Autowired
    @Qualifier("yjkrepot")
    private JdbcTemplate rptHDDB;

    @Autowired
    @Qualifier("newyjkrepot")
    private JdbcTemplate rptHDNew;


    @Autowired
    private AsyncService asyncService;




    private String getReportContentByid(String rptid){
        //int flag = 0 ;

        String reportSql ="select report_content from report_content where health_report_id = ? ";
        String result = "";
        try{
            if(Long.parseLong(rptid) >= 111743700) {
                result =rptHDNew.queryForObject(reportSql,new Object[]{rptid},String.class);
            }else {
                result =rptHDDB.queryForObject(reportSql,new Object[]{rptid},String.class);
            }
        } catch(Exception ex) {
            logger.debug("查询报告 error", ex);
        }
        return result;
    }




    public ReportContent rptStdSd(String rptId){

        Long beginTime1 = System.currentTimeMillis();
        String rptjson = getReportContentByid(rptId);
        logger.info("获取报告cost：{}ms", System.currentTimeMillis() - beginTime1);
        if(rptjson.isEmpty()){
            return new ReportContent();
        }
        Long beginTime = System.currentTimeMillis();
        ReportContent reportContent =  UnifMethod.stdReport(rptjson);
        logger.info("标准化cost：{}ms", System.currentTimeMillis() - beginTime);
        //asyncService.saveToes(rptId,reportContent);
        return reportContent;
    }


}
