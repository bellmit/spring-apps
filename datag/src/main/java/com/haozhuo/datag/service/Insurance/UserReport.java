package com.haozhuo.datag.service.Insurance;

import com.haozhuo.datag.model.report.InsuranceMap;
import com.haozhuo.datag.service.EsService;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserReport {
    @Autowired
    private HbaseTemplate hbaseTemplate;
    @Autowired
    private TransportClient client;
    @Autowired
    private EsService esService;

    private final static String HBASENAME = "DATAETL:RPT_IND";

    public InsuranceMap UserRep(String rptid) {
        InsuranceMap insuranceMap = new InsuranceMap();
        Map<String, String> valueMap = new HashMap<>();
        Map<String, String> textRefMap = new HashMap<>();
        Map<String, String> flagIdMap = new HashMap<>();
        String day = esService.getchkday(rptid);
        String rowkey = day + "_" + rptid + "_";
        String endrowkey = day + "_" + (Integer.parseInt(rptid) + 1) + "_";

        Scan scan = new Scan();
        scan.setStartRow(rowkey.getBytes());
        scan.setStopRow(endrowkey.getBytes());

        hbaseTemplate.find(HBASENAME, scan, (Result result, int i) -> {
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
            }
            return valueMap;
        });
        insuranceMap.setValueMap(valueMap);
        insuranceMap.setTextRefMap(textRefMap);
        insuranceMap.setFlagIdMap(flagIdMap);


        return insuranceMap;
    }

    public String Push(String rptid) {
        InsuranceMap insuranceMap = UserRep(rptid);
        PushGan pushGan = new PushGan();
        PushGaoxueya pushGaoxueya = new PushGaoxueya();
        PushTangniaobing pushTangniaobing = new PushTangniaobing();
        String s = pushGan.PushGan(insuranceMap);
        String s1 = pushGaoxueya.pushGaoxueya(insuranceMap);
        String s2 = pushTangniaobing.Pushtangniaobing(insuranceMap);
        return s+","+s1+","+s2;
    }

}
