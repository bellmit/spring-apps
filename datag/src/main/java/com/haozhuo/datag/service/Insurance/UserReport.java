package com.haozhuo.datag.service.Insurance;

import com.haozhuo.datag.com.service.Insurance.ClassiFication;
import com.haozhuo.datag.com.service.Insurance.MatchJzx;
import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.common.RedisUtil;
import com.haozhuo.datag.model.ResponseEntity;
import com.haozhuo.datag.model.ResponseEnum;
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

    public Msg Push(String rptid, String label, Integer age) {
        Msg msg = new Msg();
        FourIn fourIn = new FourIn();
        String day = esService.getlastday(rptid);
        String getlabel = esService.getLabelsByReportId(rptid);
        int[] arr = new int[4];
        if (JavaUtils.isEmpty(day)) {
            msg.setCode("300");
            msg.setMsg("没有此报告id");
            return msg;
        }
        String singleNormTag = dataEtlJdbcService.getSingleNormTag(label);
        if (redisUtil.hasKey(rptid)) {
            logger.info("redis中存在此缓存id数据，开始查询");
            String fication = ClassiFication.fication(singleNormTag);
            //System.out.println(fication);
            String s = redisUtil.get(rptid).toString();
            String[] split = s.split("_");
            for (int i = 0; i < arr.length; i++) {
                arr[i] = Integer.parseInt(split[i]);
            }
            if (label.equals("label")) {
                Msg msg1 = getMsg(label, age, arr);
               // logger.info("rptid:" + rptid + ",Code:" + msg1.getCode() + ",Msg:" + msg1.getMsg() + ",Abnormal:" + msg1.getFourIn().getAbnormal() + ",Label:" + msg1.getFourIn().getLabel());
                return msg1;
            }
            if (fication.contains("4") && split[0].equals("1") && age <= 60) {//肝
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(1);
                msg.setFourIn(fourIn);

                return msg;
            } else if (fication.contains("3") && split[1].equals("1") && age <= 60) {//甲
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(2);
                msg.setFourIn(fourIn);

                return msg;
            } else if (fication.contains("2") && split[2].equals("1") && age <= 55) {//高
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(4);
                msg.setFourIn(fourIn);

                return msg;
            } else if (fication.contains("1") && split[3].equals("1") && age <= 55) {//糖
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
                fourIn.setLabel(1);
                msg.setFourIn(fourIn);

                return msg;
            }
        } else {
            logger.info("redis中不存在此缓存id数据，开始查询hbase");
            InsuranceMap insuranceMap = UserRep(rptid);
            String gan = new PushGan().PushGan(insuranceMap);
            String gao = new PushGaoxueya().pushGaoxueya(insuranceMap);
            String tang = new PushTangniaobing().Pushtangniaobing(insuranceMap);

            String[] gans = gan.split("_");
            String[] gaos = gao.split("_");
            String[] tangs = tang.split("_");
            String s = ClassiFication.result(singleNormTag, insuranceMap,gans[0],gaos[0],tangs[0]);

            //System.out.println(s);
            //s1 = 1_1_1_1e
            //s2 = 1_0_1_1
            String s1 = gans[0]+"_"+"1"+"_"+gaos[0]+"_"+tangs[0];
            String fication2 = ClassiFication.getFication2(getlabel);

            String[] split1 = s1.split("_");
            String[] split = fication2.split("_");
            for (int i = 0; i < split1.length; i++) {
                if (i == 1) {
                    arr[i] = Integer.parseInt(split[i]);
                } else {
                    if (split[i].equals("1") && split1[i].equals("1")) {
                        arr[i] = 1;
                    } else {
                        arr[i] = 0;
                    }
                }
            }

            StringBuffer str5 = new StringBuffer();
            for (int i = 0; i < arr.length; i++) {
                if (i == 3) {
                    str5.append(arr[i]);
                } else {
                    str5.append(arr[i] + "_");
                }
            }

            String s2 = str5.toString();
            //System.out.println(s+","+s1+","+fication2+","+s2);

            redisUtil.set(rptid, s2, 3600);

            logger.info("缓存添加完成");
            if (label.equals("label")) {
                Msg msg1 = getMsg(label, age, arr);
                logger.info("rptid:" + rptid +"\n"+
                        "该用户所有的label:"+getlabel +"\n"
                +"该用户所有label所对应能推的分类结论："+fication2+"\n"
                +"该用户体检报告结论:"+s1+"\n"
                +"体检报告对应的原因："+"肝："+gans[1]+"，高："+gaos[1]+"，糖："+tangs[1]);
                
                return msg1;
            }
            if (s.contains("0")) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(0);
                fourIn.setLabel(1);
                msg.setFourIn(fourIn);
                logger.info("rptid:" + rptid +
                        "该用户所有的label:"+getlabel
                +"，该用户所有label所对应能推的分类结论："+fication2
                +"，该用户体检报告结论:"+s1
                +"，体检报告对应的原因："+"肝："+gans[1]+"，高："+gaos[1]+"，糖："+tangs[1]);
                return msg;
            } else if (s.contains("g") && age <= 60) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(1);
                msg.setFourIn(fourIn);
                logger.info("rptid:" + rptid +
                        "该用户所有的label:"+getlabel
                +"，该用户所有label所对应能推的分类结论："+fication2
                +"，该用户体检报告结论:"+s1
                +"，体检报告对应的原因："+"肝："+gans[1]+"，高："+gaos[1]+"，糖："+tangs[1]);
                return msg;
            } else if (s.contains("j") && arr[1] == 1 && age <= 60) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(2);
                msg.setFourIn(fourIn);
                logger.info("rptid:" + rptid +
                        "该用户所有的label:"+getlabel
                +"，该用户所有label所对应能推的分类结论："+fication2
                +"，该用户体检报告结论:"+s1
                +"，体检报告对应的原因："+"肝："+gans[1]+"，高："+gaos[1]+"，糖："+tangs[1]);
                return msg;
            } else if (s.contains("t") && age <= 55) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(3);
                msg.setFourIn(fourIn);
                logger.info("rptid:" + rptid +
                        "该用户所有的label:"+getlabel
                +"，该用户所有label所对应能推的分类结论："+fication2
                +"，该用户体检报告结论:"+s1
                +"，体检报告对应的原因："+"肝："+gans[1]+"，高："+gaos[1]+"，糖："+tangs[1]);
                return msg;
            } else if (s.contains("x") && age <= 55) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(1);
                fourIn.setLabel(4);
                msg.setFourIn(fourIn);
                logger.info("rptid:" + rptid +
                        "该用户所有的label:"+getlabel
                +"，该用户所有label所对应能推的分类结论："+fication2
                +"，该用户体检报告结论:"+s1
                +"，体检报告对应的原因："+"肝："+gans[1]+"，高："+gaos[1]+"，糖："+tangs[1]);
                return msg;
            } else {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setAbnormal(0);
                fourIn.setLabel(0);
                msg.setFourIn(fourIn);
                logger.info("rptid:" + rptid +
                        "该用户所有的label:"+getlabel
                +"，该用户所有label所对应能推的分类结论："+fication2
                +"，该用户体检报告结论:"+s1
                +"，体检报告对应的原因："+"肝："+gans[1]+"，高："+gaos[1]+"，糖："+tangs[1]);
                return msg;
            }
        }


    }

    public Msg getMsg(String label, Integer age, int[] arr) {
        Msg msg = new Msg();
        FourIn fourIn = new FourIn();


        if (label.equals("label")) {
            fourIn.setAbnormal(2);
            if (arr[0] == 1 && age <= 60) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setLabel(1);
                msg.setFourIn(fourIn);
            } else if (arr[1] == 1 && age <= 60) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setLabel(2);
                msg.setFourIn(fourIn);
            } else if (arr[2] == 1 && age <= 55) {
                msg.setCode("200");
                msg.setMsg("查询成功");
                fourIn.setLabel(4);
                msg.setFourIn(fourIn);
            } else if (arr[3] == 1 && age <= 55) {
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

    public ResponseEntity GetInsurance(String rptid) {
        Msg msg = new Msg();
        FourIn fourIn = new FourIn();
        String rs = "";
        StringBuffer sb = new StringBuffer(rs);
        int[] arr = new int[4];
        if (redisUtil.hasKey(rptid)) {
            String s = redisUtil.get(rptid).toString();
            String[] split = s.split("_");
            if (Integer.parseInt(split[0]) == 1) {
                sb.append("1");
            }
            if (Integer.parseInt(split[1]) == 1) {
                sb.append("2");
            }


       /*     msg.setCode("200");
            msg.setMsg("查询成功");
            fourIn.setAbnormal(3);
            if (Integer.parseInt(split[0]) == 0 && Integer.parseInt(split[1]) == 0) {
                fourIn.setLabel("0");
            } else {
                fourIn.setLabel(sb.toString());
            }

            msg.setFourIn(fourIn);
            return msg;*/
            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(),split[0].equals("0")  && split[1].equals("0")? "0": sb.toString());
        } else {

            String label = esService.getLabelsByReportId(rptid);
            InsuranceMap insuranceMap = UserRep(rptid);
            String s = new PushGan().PushGan(insuranceMap);
            String s1 = new PushGaoxueya().pushGaoxueya(insuranceMap);
            String s2 = new PushTangniaobing().Pushtangniaobing(insuranceMap);
            String fourrs = s + "_" + "1_" + s1 + "_" + s2;
            String[] split = fourrs.split("_");
            //4ge fenlei
            String fication2 = ClassiFication.getFication2(label);
            String[] split1 = fication2.split("_");

            for (int i = 0; i < split1.length; i++) {
                if (i == 1) {
                    arr[i] = Integer.parseInt(split1[i]);
                } else {
                    if (split[i].equals("1") && split1[i].equals("1")) {
                        arr[i] = 1;
                    } else {
                        arr[i] = 0;
                    }
                }
            }

            StringBuffer str6 = new StringBuffer();
            for (int i = 0; i < arr.length; i++) {
                if (i == 3) {
                    str6.append(arr[i]);
                } else {
                    str6.append(arr[i] + "_");
                }
            }

            String s3 = str6.toString();
            redisUtil.set(rptid, s3, 3600);
            String s4 = "";
            if (arr[0] == 1&&arr[1]==0) {
                s4="1";
            }
            if (arr[1] == 1&&arr[0]==0) {
                s4="2";
            }
            if (arr[1]==1&&arr[0]==1){
                s4="3";
            }

            return new ResponseEntity<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(),arr[0] == 0 && arr[1] == 0? "0": s4);
        }


    }


}


