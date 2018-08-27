package com.haozhuo.rcmd.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haozhuo.rcmd.model.glu.Report;
import com.haozhuo.rcmd.model.glu.ReportHttpMsg;
import com.haozhuo.rcmd.model.glu.ReportObjData;
import com.haozhuo.rcmd.model.glu.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

/**
 * Created by Lucius on 8/27/18.
 */
@Component
public class GluService {
    private static final Logger logger = LoggerFactory.getLogger(GluService.class);
    @Autowired
    private RestTemplateBuilder restTemplate;

    private ObjectMapper mapper = new ObjectMapper();
    @Value("${app.rest.glu}")
    private String url;

    public ReportObjData getAndParseReport(String reportId) {
        try {
            ReportHttpMsg reportHttpMsg = restTemplate.build().getForObject(String.format(url, reportId), ReportHttpMsg.class);
            String userInfoJson = reportHttpMsg.getData().getUserInfo().replaceAll("\\\"", "\"");
            String reportJson = reportHttpMsg.getData().getReportContent().replaceAll("\\\"", "\"");
            return new ReportObjData(mapper.readValue(userInfoJson, UserInfo.class), mapper.readValue(reportJson, Report.class));
        } catch (Exception e) {
            logger.info("解析报告{}出错,可能不存在这个报告,或者该报告缺少userInfo or reportContent", reportId);
            return null;
        }
    }

    private boolean userInfoIsLegal(UserInfo userInfo) {
        return (userInfo.getSex() == 0 || userInfo.getSex() == 1) && userInfo.getAge() >= 18 && userInfo.getAge() <= 65;
    }

    private boolean reportIsLegal(Report report) {
        return true;
    }

    public boolean isLegal(ReportObjData reportObjData) {
        return (reportObjData != null)
                && userInfoIsLegal(reportObjData.getUserInfo())
                && reportIsLegal(reportObjData.getReport());
    }
}
