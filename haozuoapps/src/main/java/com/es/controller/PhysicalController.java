package com.es.controller;

import com.es.dao.PhysicalEntity;
import com.es.service.PhysicalDao;
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
@RequestMapping("/physical")
public class PhysicalController {
    @Autowired
    private PhysicalDao physicalDao;


    // add
    @PostMapping("/add")
    //@ApiOperation("add")
    public PhysicalEntity add(@RequestBody PhysicalEntity physicalEntity) {
        PhysicalEntity save = physicalDao.save(physicalEntity);
        return save;
    }


    // springboot 整合 es 查询
    // 根据id查询文档信息
    @GetMapping("/findById/{id}")
    public Optional<PhysicalEntity> findById(@PathVariable String id) {
        return physicalDao.findById(id);

    }

    // 实现分页查询
    @GetMapping("/search")
    public List<PhysicalEntity> search(String id_card_number,
                                       String index_name,
                                       String result_value,
                                       String create_time,
                                       String text_ref,
                                       String result_check,
                                       String std_index_name,
                                       @PageableDefault(page = 0, value = 100) Pageable pageable) throws ParseException {
        // 1.创建查询对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(id_card_number)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("id_card_number", id_card_number);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(index_name)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("index_name", index_name);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(result_value)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("result_value", result_value);
            boolQuery.must(matchQuery);
        }
        if (create_time!=null) {
//			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//			Date parse = simpleDateFormat.parse(create_time);
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("create_time", create_time);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(text_ref)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("text_ref", text_ref);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(result_check)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("result_check", result_check);
            boolQuery.must(matchQuery);
        }
        if (!StringUtils.isEmpty(std_index_name)) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("std_index_name", std_index_name);
            boolQuery.must(matchQuery);
        }
        // 2.调用查询接口
        Page<PhysicalEntity> search = physicalDao.search(boolQuery, pageable);
        // 3.将迭代器转换为集合
        ArrayList<PhysicalEntity> physicalEntities = Lists.newArrayList(search);
        return physicalEntities;
    }
}

