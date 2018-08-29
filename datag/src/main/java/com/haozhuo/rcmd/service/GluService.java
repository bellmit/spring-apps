package com.haozhuo.rcmd.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haozhuo.rcmd.common.Tuple;
import com.haozhuo.rcmd.model.glu.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Lucius on 8/27/18.
 */
@Component
public class GluService {
    private static final Logger logger = LoggerFactory.getLogger(GluService.class);

    private final Map<String, String> checkIndexRegexMap;
    private final Map<String, Tuple<Double, Double>> textRefMap;
    private final String excludeRegex = ".*(比|/|\\\\).*";
    @Autowired
    private RestTemplateBuilder restTemplate;

    private ObjectMapper mapper = new ObjectMapper();
    @Value("${app.rest.glu}")
    private String url;

    public GluService() {
        checkIndexRegexMap = new HashMap<>();
        checkIndexRegexMap.put("收缩压", "^收缩压$");
        checkIndexRegexMap.put("舒张压", "^舒张压$");
        checkIndexRegexMap.put("血糖（空腹）", "(?=.*空腹)(?=.*血糖)^.*");
        checkIndexRegexMap.put("体重", "^体重$");
        checkIndexRegexMap.put("身高", "^身高$");
        checkIndexRegexMap.put("高密度脂蛋白", ".*高密度脂.*");
        checkIndexRegexMap.put("低密度脂蛋白", ".*低密度脂.*");
        checkIndexRegexMap.put("甘油三酯", ".*甘油三.*");
        checkIndexRegexMap.put("总胆固醇", ".*总胆固醇.*");
        textRefMap = new HashMap<>();
        textRefMap.put("低密度脂蛋白", new Tuple<>(1.3D, 4.3D));
        textRefMap.put("高密度脂蛋白", new Tuple<>(0.8D, 1.96D));
        textRefMap.put("甘油三酯", new Tuple<>(0.45D, 1.8D));
        textRefMap.put("总胆固醇", new Tuple<>(2.33D, 5.7D));
        textRefMap.put("血糖（空腹）", new Tuple<>(3.9D, 6.1D));

    }


    public ReportObjData getAndParseReport(Long reportId) {
        try {
            ReportHttpMsg reportHttpMsg = restTemplate.build().getForObject(String.format(url, reportId), ReportHttpMsg.class);
            String userInfoJson = reportHttpMsg.getData().getUserInfo().replaceAll("\\\"", "\"");
            UserInfo userInfo = mapper.readValue(userInfoJson, UserInfo.class);
            String reportJson = reportHttpMsg.getData().getReportContent().replaceAll("\\\"", "\"");
            Report report = mapper.readValue(reportJson,Report.class);
            return new ReportObjData(userInfo, report);
        } catch (Exception e) {
            logger.info("解析报告{}出错,可能不存在这个报告,或者该报告缺少userInfo or reportContent", reportId);
            return null;
        }
    }

    private boolean userInfoIsLegal(UserInfo userInfo) {
        return (userInfo.getSex() == 0 || userInfo.getSex() == 1) && userInfo.getAge() >= 18 && userInfo.getAge() <= 65;
    }

    private String findIndex(String checkIndexName, String resultValue, String textRef) {
        String result = "";
        for (Map.Entry<String, String> entry : checkIndexRegexMap.entrySet()) {
            String checkIndexNameStd = entry.getKey();
            String checkIndexNameRegex = entry.getValue();
            if (isLegalCheckIndexName(checkIndexName, checkIndexNameRegex) && isLegalValue(checkIndexNameStd, resultValue, textRef)) {
                result = entry.getKey();
                break;
            }
        }
        return result;
    }

    private boolean isLegalValueTextRef(String checkIndexNameStd, String textRef, String resultValue) {
        List<Double> parsedTextRefList = findDoubles(textRef);
        if (parsedTextRefList.size() != 2) //参考范围解析后不符合要求
            return false;
        Tuple<Double, Double> minMaxRefStd = textRefMap.get(checkIndexNameStd);
        Double minCompare = Math.abs(parsedTextRefList.get(0) - minMaxRefStd.getT1()) / (minMaxRefStd.getT1()); //参考范围和标准参考范围的最小值的偏差
        Double maxCompare = Math.abs(parsedTextRefList.get(1) - minMaxRefStd.getT2()) / (minMaxRefStd.getT2()); //参考范围和标准参考范围的最大值的偏差
        if (minCompare > 0.1 || maxCompare > 0.1) //参考范围与标准参考范围偏差太大。不符合要求
            return false;
        List<Double> parsedResultValueList = findDoubles(resultValue);
        if (parsedResultValueList.size() != 1) //结果值解析后不符合要求
            return false;
        Double value = parsedResultValueList.get(0);
        if ("血糖（空腹）".equals(checkIndexNameStd) && (value < 3.9 || value > 7)) //验证空腹血糖的结果值是否在正常范围内
            return false;
        return true;
    }

    private boolean isLegalValue(String checkIndexNameStd, String resultValue, String textRef) {
        if (textRefMap.containsKey(checkIndexNameStd)) { // 参考范围也要验证，如2.33-5.7
            return isLegalValueTextRef(checkIndexNameStd, textRef, resultValue);
        } else { // 不验证参考范围
            return findDoubles(resultValue).size() == 1;
        }
    }

    private boolean isLegalCheckIndexName(String checkIndexName, String checkIndexNameRegex) {
        return checkIndexName.matches(checkIndexNameRegex) && !checkIndexName.matches(excludeRegex);
    }

    private List<Double> findDoubles(String text) {
        List<Double> result = new ArrayList<>(2);
        for (String x : text.split("[^\\d\\.]")) {
            if (!"".equals(x)) {
                result.add(Double.parseDouble(x));
            }
        }
        return result;
    }

    private boolean reportIsLegal(Report report) {
        List<String> legalCheckIndexList = new ArrayList<>();
        for (CheckItem checkItem : report.getCheckItems()) {
            for (CheckResult checkResult : checkItem.getCheckResults()) {
                String checkIndex = findIndex(checkResult.getCheckIndexName(), checkResult.getResultValue(), checkResult.getTextRef());
                if (!"".equals(checkIndex)) {
                    legalCheckIndexList.add(checkIndex);
                }
            }
        }
        Set<String> legalCheckIndexSet = new HashSet(legalCheckIndexList);
        logger.info(legalCheckIndexSet.toString());
        return (legalCheckIndexList.size() == legalCheckIndexSet.size()) && legalCheckIndexSet.size() == checkIndexRegexMap.size();
    }

    public boolean isLegal(ReportObjData reportObjData) {
        return (reportObjData != null)
                && userInfoIsLegal(reportObjData.getUserInfo())
                && reportIsLegal(reportObjData.getReport());
    }
}
