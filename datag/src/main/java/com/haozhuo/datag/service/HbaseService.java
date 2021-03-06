package com.haozhuo.datag.service;


import com.haozhuo.datag.common.StringUtil;
import com.haozhuo.datag.model.report.Body;
import com.haozhuo.datag.model.report.HongKang;
import com.haozhuo.datag.model.report.InsuranceMap;
import com.haozhuo.datag.model.report.RepAbnormal;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;


@Component
public class HbaseService {
    private static final Logger logger = LoggerFactory.getLogger(HbaseService.class);
    @Autowired
    private HbaseTemplate hbaseTemplate;
    @Autowired
    private EsService esService;
    private final static String HBASENAME = "DATAETL:RPT_IND";
    private final static String HBASENAME1 = "DATAETL:RPT";

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
                System.out.println("rowkey:" + rowName);
                if ((rownmaes[2].contains("??????") || rownmaes[2].contains("????????????")) && rownmaes[3].equals("?????????")) {
                    if (key.equals("rs_val")) {
                        set.add(value);
                    }
                }
                if (rownmaes[2].contains("??????") && rownmaes[3].equals("")) {
                    if (key.equals("rs_val")) {
                        set.add(value);
                    }
                }
                // if (key.equals("chk_item") );

            }
            return set;
        });
        return set;
    }

    public static List<Body> getBody(List list1, String sex) {
        List<Body> list = new ArrayList<>();
        if (sex.equals("???"))
            list1.remove("??????");
        if (list1.size() > 0)
            for (int i = 0; i < list1.size(); i++) {
                Body body = new Body();
                body.setFlag(1);
                body.setItem((String) list1.get(i));
                list.add(body);
            }
        //?????????18?????????
        String[] bodys = {"??????", "??????", "??????", "??????", "?????????", "??????", "??????", "??????", "??????????????????", "??????", "??????", "??????", "??????", "????????????", "????????????", "??????", "???????????????", "??????"};
        List<String> list2 = Arrays.stream(bodys).collect(Collectors.toList());
        list2.removeAll(list1);
        if (sex.equals("???"))
            list2.remove("??????");
        // System.out.println("????????????"+list2.toString());
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
            return this.getBody(dataEtlJdbcService.getBylabel(esService.getLabelsByReportId(reportId)), sex);
        }
    }

    public List getrep(String rowkey){
        List list = new ArrayList();
        String day = esService.getlastday(rowkey);
        String substring = day.substring(0, 10);
        String srowkey = substring + "_" + rowkey;
        String endrowkey = substring + "_" + (Integer.parseInt(rowkey) + 1);
        Scan scan = new Scan();

        scan.setStartRow(srowkey.getBytes());
        scan.setStopRow(endrowkey.getBytes());
          hbaseTemplate.find(HBASENAME1,scan,(Result result, int i)->{
            Cell[] cells = result.rawCells();
            for (Cell cell:cells){
                String key = new String(CellUtil.cloneQualifier(cell));
                String value = new String(CellUtil.cloneValue(cell));
                String rowName = new String(CellUtil.cloneRow(cell));

                list.add(rowName+"=="+key+"=="+value);
            }

             return list;
        });
          return list;
    }

    public static void main(String[] args) {
        List list = new ArrayList();
        list.add("???????????????");
        list.add("???????????????");
        List<Body> body = getBody(list,"???");
        for (int i = 0;i<body.size();i++){
            System.out.println(body.get(i).getItem()+","+body.get(i).getFlag());
        }
    }
}


