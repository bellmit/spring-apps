package com.haozhuo.datag.service.Insurance;

import com.haozhuo.datag.com.service.Insurance.ClassiFication;
import com.haozhuo.datag.com.service.Insurance.MatchJzx;
import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.common.RedisUtil;
import com.haozhuo.datag.model.report.FourIn;
import com.haozhuo.datag.model.report.InsuranceMap;
import com.haozhuo.datag.model.report.Msg;
import com.haozhuo.datag.model.report.Msg1;
import com.haozhuo.datag.service.DataEtlJdbcService;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.RedisService;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.stereotype.Component;
import scala.collection.mutable.StringBuilder;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.haozhuo.datag.com.service.Insurance.ClassiFication.result;

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
    @Autowired
    private DataEtlJdbcService dataEtlJdbcService;

    private final static String HBASENAME = "DATAETL:RPT_IND";
    private final static String HBASENAME1 = "DATAETL:RPT_B";
    private static final Logger logger = LoggerFactory.getLogger(UserReport.class);
    public InsuranceMap UserRep(String rptid) {
        String day = esService.getlastday(rptid);
        String substring = day.substring(0, 10);
        String rowkey = substring + "_" + rptid;
        String endrowkey = substring + "_" + (Integer.parseInt(rptid) + 1);
        Scan scan = new Scan();
        scan.setStartRow(rowkey.getBytes());
        scan.setStopRow(endrowkey.getBytes());
        InsuranceMap rep = getRep(scan, HBASENAME1);
        if (rep.getList1().size() == 0) {
            LocalDate date = LocalDate.parse(substring);
            LocalDate newDate1 = date.plus(-1, ChronoUnit.DAYS);
            Scan scan1 = new Scan();
            String rowkey1 = newDate1 + "_" + rptid;
            String endrowkey1 = newDate1 + "_" + (Integer.parseInt(rptid) + 1);
            scan1.setStartRow(rowkey1.getBytes());
            scan1.setStopRow(endrowkey1.getBytes());
            InsuranceMap rep1 = getRep(scan1, HBASENAME);
            return rep1;
        } else {
            InsuranceMap rep1 = getRep(scan, HBASENAME);
            return rep1;
        }
    }

    public InsuranceMap getRep(Scan scan, String listname) {
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

    public Msg Push(String rptid, String label) {
        Msg msg = new Msg();
        FourIn fourIn = new FourIn();
        String singleNormTag = dataEtlJdbcService.getSingleNormTag(label);
        if (redisUtil.hasKey(rptid)) {
            logger.info("redis中存在此缓存id数据，开始查询");
            String fication = ClassiFication.fication(label);
            String s = redisUtil.get(rptid).toString();
            String[] split = s.split("_");
            if (label.equals("label")){
                Msg msg1 = getMsg(s, label);
                return msg1;
            }
            if (fication.contains("4") && split[0].equals("1")) {//肝
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(1);
                msg.setFourIn(fourIn);
                return msg;
            } else if (fication.contains("3") && split[1].equals("1")) {   //甲
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(2);
                msg.setFourIn(fourIn);
                return msg;
            } else if (fication.contains("2") && split[2].equals("1")) {//高
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(4);
                msg.setFourIn(fourIn);
                return msg;
            } else if (fication.contains("1") && split[3].equals("1")) {//糖
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(3);
                msg.setFourIn(fourIn);
                return msg;
            } else {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(0);
                fourIn.setLabel(0);
                msg.setFourIn(fourIn);
                return msg;
            }
        } else {
            logger.info("redis中不存在此缓存id数据，开始查询hbase");
            InsuranceMap insuranceMap = UserRep(rptid);
            String s = ClassiFication.result(singleNormTag, insuranceMap);
            String s1 = ClassiFication.fourRs();
            redisUtil.set(rptid, s1,3600);
            logger.info("缓存添加完成");
            if (label.equals("label")){
                Msg msg1 = getMsg(s1, label);
                return msg1;
            }
            if (s.contains("0")) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(0);
                fourIn.setLabel(0);
                msg.setFourIn(fourIn);
                return msg;
            } else if (s.contains("g")) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(1);
                msg.setFourIn(fourIn);
                return msg;
            } else if (s.contains("j")) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(2);
                msg.setFourIn(fourIn);
                return msg;
            } else if (s.contains("t")) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(3);
                msg.setFourIn(fourIn);
                return msg;
            } else if (s.contains("x")) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(4);
                msg.setFourIn(fourIn);
                return msg;
            }
        }


        return msg;
    }

    public Msg getMsg(String data, String label) {
        Msg msg = new Msg();
        FourIn fourIn = new FourIn();
        String[] split = data.split("_");

        if (label.equals("label")) {
            fourIn.setAbnormal(2);
            if (split[0].equals("1")) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setLabel(1);
                msg.setFourIn(fourIn);
            } else if (split[1].equals("1")) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setLabel(2);
                msg.setFourIn(fourIn);
            } else if (split[2].equals("1")) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setLabel(4);
                msg.setFourIn(fourIn);
            } else if (split[3].equals("1")) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setLabel(3);
                msg.setFourIn(fourIn);
            } else {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setLabel(0);
                msg.setFourIn(fourIn);
            }
        }
        return msg;
    }
}


