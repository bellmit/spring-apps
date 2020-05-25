package com.es.dao;

import com.es.dto.Checksinkname;
import com.es.dto.RiskGradeName;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Data
@Document(indexName = "report", type = "examination")
public class ReportEntity {

    @Id
    private String id;
    private String report_id;   //检查id
    private String customerName;   //姓名
    private String companyadress;    //公司地址
    private String createtime;    //报告创建时间
    private String sex;                //性别
    private String age;       //年龄
    private String idcardnumber;    //身份证号
    private String checktime;        //体检时间
    private Checksinkname checksinkname;    //风险等级
    private RiskGradeName riskGradeName;    //异常标签
}
