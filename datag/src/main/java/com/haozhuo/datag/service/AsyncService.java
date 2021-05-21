package com.haozhuo.datag.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.haozhuo.datag.com.service.bean.Report;
import com.haozhuo.datag.com.service.bean.ReportContent;
import com.haozhuo.datag.com.service.bean.ReportObj;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class AsyncService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);


    @Autowired
    private TransportClient client;

    @Autowired
    @Qualifier("yjkrepot")
    private JdbcTemplate rptHDDB;

    @Async("rcmdExecutor")
    void saveToes(String rptid,ReportContent reportContent) {
        try {
            ReportObj reportObj = getReportContentByid(rptid);
            //ReportContent reportContent =  JSON.parseObject(rptjson, ReportContent.class);
            reportObj.setReportContent(reportContent);
            if (!reportObj.getBirthday().isEmpty()) {
                try {
                    int age = Integer.valueOf(reportObj.getCheckDate().substring(0, 4)) - Integer.valueOf(reportObj.getBirthday().substring(0, 4));
                    reportObj.setAge(age);
                } catch (Exception e){
                    logger.info("年龄计算错错误：" + e);
                }
            }
            if(!reportObj.getHealthReportId().isEmpty()){
                updateReport(rptid,reportObj);
                logger.info("报告标准化存储ES完成");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateReport(String rptid, ReportObj report) {
        // 此处ID是es里面自己生成的ID，与数据ID无关
        UpdateRequest updateRequest = new UpdateRequest("report_std", "report", rptid);
        Map<String, Object> newjsonMap = new HashMap<>();
        newjsonMap.put("health_report_id",report.getHealthReportId());
        newjsonMap.put("create_date",report.getReportCreateTime().substring(0,10));
        newjsonMap.put("idcard", report.getIdCardNoMd5());
        newjsonMap.put("userId",report.getUserId());
        newjsonMap.put("sex",report.getSex());
        newjsonMap.put("name",report.getName());
        newjsonMap.put("dwdm",report.getDwdm());
        newjsonMap.put("dwmc",report.getDwmc());
        newjsonMap.put("checkDate",report.getCheckDate());
        newjsonMap.put("age",report.getAge());
        newjsonMap.put("birtday",report.getBirthday());
        newjsonMap.put("userLoadTiem",report.getUserLoadTime());
        newjsonMap.put("report_create_time",report.getReportCreateTime());
        newjsonMap.put("report_content", JSON.toJSON(report.getReportContent()));
        newjsonMap.put("etl_time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        updateRequest.doc(newjsonMap).upsert();
        client.update(updateRequest);
    }

    public void insertReport(String rptid, ReportObj report) {
        IndexRequest indexRequest = new IndexRequest("report_std", "report");

        // 此处ID是es里面自己生成的ID，与数据ID无关
        String id = rptid+"7777";
        UpdateRequest updateRequest = new UpdateRequest("report_std", "report", id);
        Map<String, Object> newjsonMap = new HashMap<>();
        newjsonMap.put("health_report_id",report.getHealthReportId());
        newjsonMap.put("create_date",report.getReportCreateTime().substring(0,10));
        newjsonMap.put("idcard", report.getIdCardNoMd5());
        newjsonMap.put("userId",report.getUserId());
        newjsonMap.put("sex",report.getSex());
        newjsonMap.put("name",report.getName());
        newjsonMap.put("dwdm",report.getDwdm());
        newjsonMap.put("dwmc",report.getDwmc());
        newjsonMap.put("checkDate",report.getCheckDate());
        newjsonMap.put("age",report.getAge());
        newjsonMap.put("birtday",report.getBirthday());
        newjsonMap.put("userLoadTiem",report.getUserLoadTime());
        newjsonMap.put("report_create_time",report.getReportCreateTime());
        newjsonMap.put("report_content", JSON.toJSON(report.getReportContent()));
        newjsonMap.put("etl_time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        updateRequest.doc(newjsonMap).upsert();
        client.update(updateRequest);
    }



    private static final String persontSql ="select cast(a.health_report_id as char) as health_report_id,a.user_id,b.check_date as checkDate,b.check_unit_code,b.check_unit_name,b.dwdm,b.dwmc,b.sex,a.create_time as  reportCreateTime,a.last_update_time as lastUpdateTime,b.id_card_number as idCardNoMd5,b.customer_name as name,b.born_date as birthday from yjk.health_archive a left JOIN yjk.health_report b  on a.health_report_id = b.health_report_id  where a.health_report_id = ? ";

    private ReportObj getReportContentByid(String rptid){
        //int flag = 0 ;
        ReportObj result = new ReportObj();
        try{
            result =rptHDDB.queryForObject(persontSql, new BeanPropertyRowMapper<>(ReportObj.class), rptid);
        }
        catch(Exception ex) {
            logger.debug("persontSql error", ex);
        }
        System.out.println(result);
        return result;
    }
}
