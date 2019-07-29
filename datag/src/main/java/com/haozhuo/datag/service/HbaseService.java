package com.haozhuo.datag.service;


import com.haozhuo.datag.model.report.Body;
import com.haozhuo.datag.model.report.Msg;
import com.haozhuo.datag.util.SqlSplit;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class HbaseService {
    private static final Logger logger = LoggerFactory.getLogger(HbaseService.class);
    @Autowired
    private HbaseTemplate hbaseTemplate;
    @Autowired
    private EsService esService;
    private final static String HBASENAME = "DATAETL:RPT_IND";
    @Autowired
    private DataEtlJdbcService dataEtlJdbcService;

    public Set getItemByReportId(String reportId) {
        SubstringComparator substringComparator = new SubstringComparator(reportId);
        RowFilter rowFilter = new RowFilter(CompareFilter.CompareOp.EQUAL, substringComparator);
        Scan scan = new Scan();
        scan.setFilter(rowFilter);
        HashSet set = new HashSet();
        hbaseTemplate.find(HBASENAME, scan, (Result result, int i) -> {
            Cell[] cells = result.rawCells();
            for (Cell cell : cells) {
                String key = new String(CellUtil.cloneQualifier(cell));
                String value = new String(CellUtil.cloneValue(cell));
                String rowName = new String(CellUtil.cloneRow(cell));
                String[] rownmaes = rowName.split("_");
                System.out.println("rowkey:"+rowName);
                if((rownmaes[2].contains("血压")|| rownmaes[2].contains("一般检查"))&&rownmaes[3].equals("收缩压")){
                    if(key.equals("rs_val")){
                        set.add(value);
                    }
                }
                if(rownmaes[2].contains("血压")&&rownmaes[3].equals("")){
                    if(key.equals("rs_val")){
                        set.add(value);
                    }
                }
              // if (key.equals("chk_item") );

            }
            return set;
        });
        return set;
    }

    public List<Body> getBody(List list1, String sex) {
        List<Body> list = new ArrayList<>();
        if (sex.equals("男"))
            list1.remove("内科");
        if (list1.size() > 0)
            for (int i = 0; i < list1.size(); i++) {
                Body body = new Body();
                body.setFlag(1);
                body.setItem((String) list1.get(i));
                list.add(body);
            }
            //人体图18项数组
        String[] bodys = {"肺部","妇科","肝脏","化验","甲状腺","口腔","内科","尿液","泌尿生殖系统","肾脏","外科","胃肠","心脏","眼耳鼻喉","一般检查","影像","肿瘤标志物","其他"};
        List<String> list2 = Arrays.stream( bodys ).collect(Collectors.toList());
        list2.removeAll(list1);
        if (sex.equals("男"))
            list2.remove("妇科");
        // System.out.println("移除异常"+list2.toString());
        for (int i = 0; i < list2.size(); i++) {
            Body body = new Body();
            body.setFlag(0);
            body.setItem((String) list2.get(i));
            list.add(body);
        }

        return list;
    }
    public List getBodyById(String reportId) {
        List list = new ArrayList();
        String sex = esService.getSexByReportId(reportId);
        if (sex.trim().length() < 1) {
            return list;
        } else {
            return this.getBody(dataEtlJdbcService.getBylabel(esService.getLabelsByReportId(reportId)),  sex);
        }
    }
}


