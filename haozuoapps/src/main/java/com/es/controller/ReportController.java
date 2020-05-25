package com.es.controller;

import com.es.dao.ReportEntity;
import com.es.dao.ReportEntity;
import com.es.service.ReportDao;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/report")
public class ReportController {
    @Autowired
    private ReportDao reportDao;


    // add
    @PostMapping("/add")
    public ReportEntity add(@RequestBody ReportEntity reportEntity) {
        ReportEntity save = reportDao.save(reportEntity);
        return save;
    }


    // springboot 整合 es 查询
    // 根据id查询文档信息
    @GetMapping("/findById/{id}")
    public Optional<ReportEntity> findById(@PathVariable String id) {
        return reportDao.findById(id);

    }

    // 实现分页查询
    @GetMapping("/search")
    public List<ReportEntity> search(String report_id,
                                     String customerName,
                                     String companyadress,
                                     String createtime,
                                     String sex,
                                     String age,
                                     String idcardnumber,
                                     String checktime,
                                     String checksinkname,
                                     String riskGradeName,
                                     @PageableDefault(page = 0, value = 100) Pageable pageable) throws ParseException {
        // 1.创建查询对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(report_id)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("report_id", report_id);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(customerName)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("customerName", customerName);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(companyadress)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("companyadress", companyadress);
            boolQuery.must(matchQuery);
        }
        if (createtime != null) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("createtime", createtime);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(sex)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("sex", sex);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(age)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("age", age);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(idcardnumber)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("idcardnumber", idcardnumber);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(idcardnumber)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("checktime", checktime);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(idcardnumber)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("checksinkname", checksinkname);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(idcardnumber)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("riskGradeName", riskGradeName);
            boolQuery.must(matchQuery);
        }
        // 2.调用查询接口
        Page<ReportEntity> search = reportDao.search(boolQuery, pageable);
        // 3.将迭代器转换为集合
        ArrayList<ReportEntity> reportEntities = Lists.newArrayList(search);
        return reportEntities;
    }
}

