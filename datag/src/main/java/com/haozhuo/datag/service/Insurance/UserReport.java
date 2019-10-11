package com.haozhuo.datag.service.Insurance;

import com.haozhuo.datag.com.service.Insurance.MatchJzx;
import com.haozhuo.datag.common.RedisUtil;
import com.haozhuo.datag.model.report.FourIn;
import com.haozhuo.datag.model.report.InsuranceMap;
import com.haozhuo.datag.model.report.Msg1;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.RedisService;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserReport {
    @Autowired
    private HbaseTemplate hbaseTemplate;
    @Autowired
    private TransportClient client;
    @Autowired
    private EsService esService;
    @Autowired
    private RedisUtil redisUtil;

    private final static String HBASENAME = "DATAETL:RPT_IND";
    private final static String HBASENAME1 = "DATAETL:RPT_B";

    public InsuranceMap UserRep(String rptid) {
        String day = esService.getlastday(rptid);
        String substring = day.substring(0, 10);
        String rowkey = substring + "_" + rptid + "_";
        String endrowkey = substring + "_" + (Integer.parseInt(rptid) + 1) + "_";
        Scan scan = new Scan();
        scan.setStartRow(rowkey.getBytes());
        scan.setStopRow(endrowkey.getBytes());
        InsuranceMap rep = getRep(scan, HBASENAME1);
        if (rep.getList1().size()==0){

        }
        InsuranceMap rep1 = getRep(scan, HBASENAME);

        return rep1;
    }

    public InsuranceMap getRep(Scan scan,String listname){
        InsuranceMap insuranceMap = new InsuranceMap();
        Map<String, String> valueMap = new HashMap<>();
        Map<String, String> textRefMap = new HashMap<>();
        Map<String, String> flagIdMap = new HashMap<>();
        String rs = "";
        List list = new ArrayList();
        StringBuffer sb = new StringBuffer(rs);
        hbaseTemplate.find(listname, scan, (Result result, int i) -> {
            Cell[] cells = result.rawCells();
            for (Cell cell : cells) {
                String key = new String(CellUtil.cloneQualifier(cell));
                String value = new String(CellUtil.cloneValue(cell));
                String rowName = new String(CellUtil.cloneRow(cell));
                String[] rownmaes = rowName.split("_");
                if (key.equals("rs_val")) {
                    if (rownmaes.length > 3) {
                        valueMap.put(rownmaes[2] + "," + rownmaes[3], value);
                    }
                    sb.append(value + "\n");
                }
                if (key.equals("text_ref")) {
                    if (rownmaes.length > 3) {
                        textRefMap.put(rownmaes[2] + "," + rownmaes[3], value);
                    }
                }
                if (key.equals("rs_flag_id")) {
                    if (rownmaes.length > 3) {
                        flagIdMap.put(rownmaes[2] + "," + rownmaes[3], value);
                    }
                }
                if (key.equals("rpt_id")) {
                    list.add(value);
                }
            }
            return valueMap;
        });
        insuranceMap.setValueMap(valueMap);
        insuranceMap.setFlagIdMap(flagIdMap);
        insuranceMap.setTextRefMap(textRefMap);
        insuranceMap.setRsval(sb.toString());
        insuranceMap.setList1(list);
        return insuranceMap;
    }
    public String Push(String rptid,String label) {
        Msg1 msg = new Msg1();
        FourIn fourIn = new FourIn();
        int rs = 0;
        InsuranceMap insuranceMap = UserRep(rptid);
        PushGan pushGan = new PushGan();
        PushGaoxueya pushGaoxueya = new PushGaoxueya();
        PushTangniaobing pushTangniaobing = new PushTangniaobing();
        String s = pushGan.PushGan(insuranceMap);
        String s1 = pushGaoxueya.pushGaoxueya(insuranceMap);
        String s2 = pushTangniaobing.Pushtangniaobing(insuranceMap);
        String panduan = MatchJzx.panduan(label);
        String[] split = panduan.split("_");
        String s3 = split[0];
        redisUtil.set(rptid,s+","+s1+","+s2+","+s3);
        int i = Integer.parseInt(s);
        int i1 = Integer.parseInt(s1);
        int i2 = Integer.parseInt(s2);
        int i3 = Integer.parseInt(s3);


        return s+","+s1+","+s2+","+s3;

    }

}
