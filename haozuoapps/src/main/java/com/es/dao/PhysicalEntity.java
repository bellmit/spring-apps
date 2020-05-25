package com.es.dao;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Document(indexName = "physical", type = "examination")
public class PhysicalEntity {

    @Id
    private String id;
    //身份证号
    private String id_card_number;
    // 检查指标
    private String index_name;
    // 指标结果
    private String result_value;
    // 指标范围
    private String text_ref;
    // 体检日期
    private String create_time;
    // 风险等级
    private String result_check;
    // 异常指标标记
    private String std_index_name;


}
