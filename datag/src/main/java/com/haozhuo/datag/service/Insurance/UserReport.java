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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
    //tes
    private final static String HBASENAME = "DATAETL:RPT_IND";
    private final static String HBASENAME1 = "DATAETL:RPT_B";
    private static final Logger logger = LoggerFactory.getLogger(UserReport.class);
    public InsuranceMap UserRep(String rptid) {
        String day = esService.getlastday(rptid);
        String substring = day.substring(0, 10);
        String rowkey = substring + "_" + rptid;
        String endrowkey = substring + "_" + (Integer.parseInt(rptid) + 1);
        //String rowkey ="2018-04-28_7292025";
       // String endrowkey ="2018-04-28_7292026";
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
            //String rowkey1 ="2018-04-28_7292025";
           // String endrowkey1 ="2018-04-28_7292026";
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

    public Msg Push(String rptid, String label,Integer age) {
        Msg msg = new Msg();
        FourIn fourIn = new FourIn();
        String day = esService.getlastday(rptid);
        String getlabel = esService.getLabelsByReportId(rptid);
        int [] arr = new int[4];
        if (JavaUtils.isEmpty(day)){
            msg.setCode("300");
            msg.setMsg("没有此报告id");
            return msg;
        }
        String singleNormTag = dataEtlJdbcService.getSingleNormTag(label);
        if (redisUtil.hasKey(rptid)) {
            logger.info("redis中存在此缓存id数据，开始查询");
            String fication = ClassiFication.fication(singleNormTag);
            System.out.println(fication);
            String s = redisUtil.get(rptid).toString();
            String[] split = s.split("_");
            if (label.equals("label")){
                Msg msg1 = getMsg(label,age,arr);
                return msg1;
            }
            if (fication.contains("4") && split[0].equals("1")&&age<=60) {//肝
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(1);
                msg.setFourIn(fourIn)  ;
                return msg;
            } else if (fication.contains("3")&&split[1].equals("1")&&age<=60) {//甲
                    msg.setCode("200");
                    msg.setMsg("查询成功");
                    fourIn.setAbnormal(1);
                    fourIn.setLabel(2);
                    msg.setFourIn(fourIn);
                    return msg;
            } else if (fication.contains("2") && split[2].equals("1")&&age<=55) {//高
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(4);
                msg.setFourIn(fourIn);
                return msg;
            } else if (fication.contains("1") && split[3].equals("1")&&age<=55) {//糖
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

            //System.out.println(s);
            //s1 = 1_1_1_1e
            //s2 = 1_0_1_1
            String s1 = ClassiFication.fourRs();
            String fication2 = ClassiFication.getFication2(getlabel);

            String[] split1 = s1.split("_");
            String[] split = fication2.split("_");
            for (int i = 0 ;i<split1.length;i++){
                if (i==1){
                    arr[i]=Integer.parseInt(split[i]);
                }else {
                    if (split[i].equals("1")&&split1[i].equals("1")){
                        arr[i]=1;
                    }else {
                        arr[i]=0;
                    }
                }
            }

            StringBuffer str5 = new StringBuffer();
            for (int i = 0;i<arr.length;i++) {
                if (i==3){
                    str5.append(arr[i]);
                }else {
                    str5.append(arr[i]+"_");
                }
            }

            String  s2= str5.toString();
            //System.out.println(s+","+s1+","+fication2+","+s2);

            redisUtil.set(rptid, s2,3600);

            logger.info("缓存添加完成");
            if (label.equals("label")){
                Msg msg1 = getMsg( label,age,arr);
                return msg1;
            }
            if (s.contains("0")) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(0);
                fourIn.setLabel(0);
                msg.setFourIn(fourIn);
                return msg;
            } else if (s.contains("g")&&age<=60) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(1);
                msg.setFourIn(fourIn);
                return msg;
            } else if (s.contains("j")&&arr[1]==1&&age<=60) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(2);
                msg.setFourIn(fourIn);
                return msg;
            } else if (s.contains("t")&&age<=55) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(3);
                msg.setFourIn(fourIn);
                return msg;
            } else if (s.contains("x")&&age<=55) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(4);
                msg.setFourIn(fourIn);
                return msg;
            }else {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(0);
                fourIn.setLabel(0);
                msg.setFourIn(fourIn);
                return msg;
            }
        }



    }

    public Msg getMsg( String label,Integer age,int [] arr) {
        Msg msg = new Msg();
        FourIn fourIn = new FourIn();


        if (label.equals("label")) {
            fourIn.setAbnormal(2);
            if (arr[0]==1&&age<=60) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setLabel(1);
                msg.setFourIn(fourIn);
            } else if (arr[1]==1&&age<=60) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setLabel(2);
                msg.setFourIn(fourIn);
            } else if (arr[2]==1&&age<=55) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setLabel(4);
                msg.setFourIn(fourIn);
            } else if (arr[3]==1&&age<=55) {
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

   /* public void test() throws IOException {
        String rptid = null;
        String pathname = "D:\\workspace\\new\\spring-apps\\datag\\src\\main\\excel\\1000.txt";
        FileReader reader = null;
        try {
            reader = new FileReader(pathname);
            BufferedReader br = new BufferedReader(reader);
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                rptid = line;
                System.out.println(rptid + "," + i);
                i++;
                Push(rptid,"label");
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }*/

    /*public static void main(String[] args) {
        String s1 = "1_0_1_1";
        String fication2 = "1_1_0_1";
        String[] split1 = s1.split("_");
        String[] split = fication2.split("_");
        int [] arr = new int[4];
        for (int i = 0 ;i<split1.length;i++){
            if (i==1){
                arr[i]=Integer.parseInt(split[i]);
            }else {
                if (split[i].equals("1")&&split1[i].equals("1")){
                    arr[i]=1;
                }else {
                    arr[i]=0;
                }
            }
        }

        StringBuffer str5 = new StringBuffer();
        for (int i = 0;i<arr.length;i++) {
            if (i==3){
                str5.append(arr[i]);
            }else {
                str5.append(arr[i]+"_");
            }
        }

        String  s2= str5.toString();

        System.out.println(s2);
    }*/
}


