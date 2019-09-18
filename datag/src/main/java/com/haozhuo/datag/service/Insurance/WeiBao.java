package com.haozhuo.datag.service.Insurance;

import com.alibaba.fastjson.JSONObject;
import com.haozhuo.datag.com.service.Insurance.getBeiShu;
import com.haozhuo.datag.com.service.Insurance.matchMain;
import com.haozhuo.datag.common.StringUtil;
import com.haozhuo.datag.model.report.Msg1;
import com.haozhuo.datag.model.report.WeiBaoM;
import com.haozhuo.datag.service.EsService;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Component
public class WeiBao {
    @Autowired
    private HbaseTemplate hbaseTemplate;
    @Autowired
    private TransportClient client;
    @Autowired
    private EsService esService;

    private final static String HBASENAME = "DATAETL:RPT_IND";

    public String getchkday(String rptid) {

        SearchRequestBuilder srb = client.prepareSearch("reportlabel").setSize(1)
                .setQuery(matchQuery("healthReportId", rptid.trim()));
        SearchHit[] searchHits = srb.execute().actionGet().getHits().getHits();
        return stream(searchHits).map(x -> x.getSourceAsMap().get("labelCreateTime")).findFirst().orElse("").toString();
    }

    public Msg1 getRep1(String rptid) {
        Msg1 msg = new Msg1();
        WeiBaoM weiBaoM = new WeiBaoM();
        String day = getchkday(rptid);
        if (StringUtil.isEmpty(day)){
            msg.setCode(300);
            msg.setMsg("没有此报告id");
            return msg;
        }
        String rs = "2";
        String rsa = null;
        String pgi = "";
        String pgiz = "";
        String REGEX = "[^0-9.]";
        List list1 = new ArrayList();
        /*StringBuffer sb1 = new StringBuffer(day);
        String substring = day.substring(0, 10);*/
        String substring = day.substring(0, 10);
        String rowkey = substring+("_" + rptid + "_");
        String endrowkey = substring + "_" + (Integer.parseInt(rptid) + 1) + "_";
        Map<String, String> map = new HashMap<>();//value
        Map<String, String> map1 = new HashMap<>();//text_ref
        Map<String, String> map2 = new HashMap<>();//rs_flag_id
        String rsval = "";
        StringBuffer sb2 = new StringBuffer(rsval);
        Scan scan = new Scan();
        // scan.setFilter(filter);
        int i2 = Integer.parseInt(rptid)+1;
        String s5 = String.valueOf(i2);
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
                    if (rownmaes.length>3){
                        map.put(rownmaes[2] + "," + rownmaes[3], value);
                    }
                    sb2.append(value+"\n");
                }
                if (key.equals("text_ref")) {
                    if (rownmaes.length>3){
                        map1.put(rownmaes[2] + "," + rownmaes[3], value);
                    }

                }
                if (key.equals("rs_flag_id")) {
                    //System.out.println(rowName);
                    if (rownmaes.length>3){
                        map2.put(rownmaes[2] + "," + rownmaes[3], value);
                    }

                }
                //System.out.println(key+","+value);
            }
            return map;
        });
        String str = sb2.toString();
      System.out.println(str);

        String s3 = matchMain.noNumMatch(sb2.toString());
        String[] split = s3.split(",");
        if (Integer.parseInt(split[0])==1){
            String s4 = matchMain.numMatch(sb2.toString());
            String[] split1 = s4.split(",");
            System.out.println(s4);
            if (Integer.parseInt(split1[0])==1){

            }else {
                rs="1";
                String s = split[1];
                weiBaoM.setLabel(rs);
                weiBaoM.setAbnormal(s);
                msg.setWeiBaoM(weiBaoM);

                return msg;
            }
        }else {
            rs="1";
            String s = split[1];
            weiBaoM.setLabel(rs);
            weiBaoM.setAbnormal(s);
            msg.setWeiBaoM(weiBaoM);

            return msg;
        }

        String niaotang = null;
        String tongti = null;
        for (String a : map.keySet()) {
           System.out.println(a+","+map.get(a));
            String s1 = map.get(a);
            String[] key = a.split(",");
            System.out.println(key[0] + "," + key[1]);
            if ((a.contains("一般") || a.contains("血压") || a.contains("内科")) && a.contains("收缩压")) {
                //收缩压≥180mmHg，伴急性症状或安静休息后复测仍达此标准     收缩压＜80mmHg，伴周围循环衰竭表现
                String s = map.get(a);
                int i1 = Integer.parseInt(s);

                if (i1 >= 180 || i1 < 80) {
                    rs =  "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if ((a.contains("一般") || a.contains("血压") || a.contains("内科")) && a.contains("舒张")) {
                //2、舒张压≥110mmHg，伴急性症状或安静休息后复测仍达此标准  4、舒张压＜50mmHg，伴周围循环衰竭表现
                String s = map.get(a);
                int i1 = Integer.parseInt(s);
                if (i1 >= 110 || i1 < 50) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("内科") && a.contains("心率")) {
                //1、心率持续≥150次/分，伴心律不齐或心界扩大
                //2、心率≤45次/分，伴心律不齐或心界扩大
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());

                if ((v <= 45 || v >= 150) && (str.contains("心律不齐") || str.contains("心界扩大"))) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }

            if (a.contains("血常规") && (a.contains("血红蛋白测定") || a.contains("Hb"))) {
                //血红蛋白（Hb）≤60g/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v <= 60) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("血常规") && (a.contains("白细胞计数") || a.contains("WBC"))) {
                //白细胞计数（WBC）≤1.0×10^9/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v <= 1.0) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("血常规") && (a.contains("血小板计数") || a.contains("PLT"))) {
                //3、血小板计数(PLT)≤30.0×10^9/L或有明显出血倾向   4、血小板计数(PLT)≥1000.0×10^9/L

                //5、血小板计数(PLT)在30—50×10^9/L之间
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v <= 30.0 || v >= 1000 || (v >= 30 && v <= 50)) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("血常规") && (a.contains("中性粒细胞绝对值") || a.contains("NEU"))) {
                //5、中性粒细胞绝对值（NEU）≤0.5×10^9/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v <= 0.5) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("肝功能") && (a.contains("ALT") || a.contains("丙") || a.contains("丙氨酸"))) {
                //1、血清丙氨酸氨基转移酶（ALT）≥600 U/L
                // 1、血清丙氨酸氨基转移酶(ALT)＞200U/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (a.contains("/")) {

                } else {
                    if (v > 200) {
                        rs = "1";
                        rsa = a + "该重大阳性";
                        break;
                    }
                }

            }
            if (a.contains("肝功能") && (a.contains("AST") || a.contains("草") || a.contains("冬氨酸"))) {
                //2、血清天门冬氨酸氨基转移酶(AST)≥600 U/L
                // 2、血清天门冬氨酸氨基转移酶(AST)＞200U/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (a.contains("/")) {

                } else {
                    if (v > 200) {
                        rs = "1";
                        rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (a.contains("空腹") && a.contains("血糖")) {
                //3、空腹血糖（FPG）≥16.7 mmol/L（糖尿病史）
                // 4、空腹血糖（FPG）≥13.9mmol/L，合并尿酮体
                // 6、空腹血糖（FPG）≤2.8mmol/L（无糖尿病史）
                // 7、空腹血糖（FPG）≤ 3.9mmol/L（糖尿病史）
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")){

                }else{
                    double v = Double.parseDouble(trim);
                    if (v >= 16.7 && str.contains("糖尿病")) {
                        rs = "1";
                        rsa = a + "该重大阳性";
                        break;
                    }
                    if (v >= 13.9) {

                    }
                    if (v <= 2.8) {
                        if (str.contains("糖尿病")) {

                        } else {
                            rs = "1";
                            rsa = a + "该重大阳性";
                            break;
                        }
                    }
                    if (v <= 3.9) {
                        if (str.contains("糖尿病")) {
                            rs = "1";
                            rsa = a + "该重大阳性";
                            break;
                        }
                    }
                }

            }
            if (a.contains("肾功") && a.contains("血") && a.contains("肌酐")) {
                //8、血肌酐(Cr) ≥707μmol/L
                //10、血肌酐(Cr) ≥445μmol/L（首次）
                //11、血肌酐(Cr) ≥707μmol/L（历次）.
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                }else{
                    double v = Double.parseDouble(trim);
                    if (v >= 445) {
                        rs = "1";
                        rsa = a + "该重大阳性";
                        break;
                    }
                }

            }
            //(key[0].contains("眼压") || key[0].contains("眼科") &&
            if ( key[1].contains("眼压")) {
                //11、眼压＞25mmHg
                String s = map.get(a);
              //  [0-9]+
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    if (s.contains("视力")){

                    }else{
                        double v = Double.parseDouble(trim);
                        if (v > 25) {
                            rs = "1";
                            rsa = a + "该重大阳性";
                            break;
                        }
                    }
                }
            }
            if (a.contains("血常规") && (a.contains("红细胞计数") || a.contains("RBC"))) {
                //1、红细胞计数(RBC)＜2.5×10^12/L   2、红细胞计数(RBC)＞6.8×10^12/L
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    if (v < 2.5 || v > 6.8) {
                        rs = "1";
                        rsa = a + "该重大阳性";
                        break;
                    }

                }
            }
            if (a.contains("血常规") && (a.contains("白细胞计数") || a.contains("WBC"))) {
                //3、白细胞计数(WBC)≤2.0×10^9/L  4、白细胞计数(WBC)≥30.0×10^9/L
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    if (v > 25) {
                        rs = "1";
                        rsa = a + "该重大阳性";
                        break;
                    }

                }
            }
            if (a.contains("尿常规") && (a.contains("隐血") || a.contains("潜血"))) {
                //、尿潜血﹥+++
                String s = map.get(a);
                if (s.contains("3+") || s.contains("+++") || s.contains("4+") || s.contains("5+") || s.contains("++++") || s.contains("+++++")) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("尿常规") && (a.contains("尿蛋白") || a.contains("PRO"))) {
                //2、尿蛋白﹥+++
                String s = map.get(a);
                if (s.contains("3+") || s.contains("+++") || s.contains("4+") || s.contains("5+") || s.contains("++++") || s.contains("+++++")) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }

            if (a.contains("尿常规") && (a.contains("糖") || a.contains("GLU"))) {
                //、尿糖+ + +～ + + + +
                niaotang = map.get(a);
                if (niaotang.contains("3+") || niaotang.contains("+++") || niaotang.contains("4+") || niaotang.contains("++++")) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("尿常规") && a.contains("镜检") && a.contains("白")) {

                //5、镜检白细胞（WBC）＞20
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();


                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    if (v > 20) {
                        rs = "1";
                        rsa = a + "该重大阳性";
                        break;
                    }

                }
            }
            if (a.contains("尿常规") && a.contains("镜检") && a.contains("红")) {
                //4、镜检红细胞（RBC）＞20
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();

                boolean n = trim.equals("");
                System.out.println(trim + n);
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    if (v > 20) {
                        rs = "1";
                        rsa = a + "该重大阳性";
                        break;
                    }

                }
            }

            if (a.contains("尿常规") && a.contains("酮体")) {
                //6、酮体≥++（糖尿病史）
                //7、酮体≥+++（无糖尿病史）
                tongti = map.get(a);
                if (tongti.contains("2+") || tongti.contains("++") && str.contains("糖尿病")) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                } else if (tongti.contains("3+") || tongti.contains("+++")) {
                    if (str.contains("糖尿病")) {

                    } else {
                        rs = "1";
                        rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (a.contains("便常规") && (a.contains("便隐血") || a.contains("OB"))) {
                //2、大便隐血(OB)≥+++
                String s = map.get(a);
                if (s.contains("3+") || s.contains("+++")) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("肝功") && (a.contains("谷氨酰基") || a.contains("GGT"))) {
                //3、γ-谷氨酰转移酶（GGT）＞200U/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v > 200) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("肝功") && (a.contains("总蛋白") || a.contains("TP"))) {
                // 4、总蛋白＜40g/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v < 40) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("肝功") && (a.contains("白蛋白") || a.contains("Alb"))) {
                //5、白蛋白＜20g/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (a.contains("/")) {

                } else {
                    if (v < 20) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }

            }
            if (a.contains("肝功") && (a.contains("总胆红素") || a.contains("T-Bil"))) {
                //6、总胆红素＞50μmol/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v > 50) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("餐后") && (a.contains("血糖"))) {
                //8、餐后血糖≥17mmol/L，伴尿糖 +++，尿酮+
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());

                if (v >= 17 && niaotang.contains("+++") && niaotang.contains("+")) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;

                }
            }
            if (a.contains("肾功") && (a.contains("尿素") || a.contains("Urea"))) {
                //9、尿素(Urea)＞21mmol/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v > 21) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("肾功") && (a.contains("尿酸") || a.contains("UA"))) {

                //  12、尿酸(UA)＞650μmol/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v > 650) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }

            if (a.contains("肾功") && (a.contains("总胆固醇") || a.contains("TC"))) {
                //13、总胆固醇(TC)＞8mmol/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v > 8) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("肾功") && (a.contains("甘油三酯") || a.contains("TG"))) {
                //14、甘油三酯(TG)＞10mmol/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v > 10) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
            if (a.contains("血清淀粉酶")) {
                //16、血清淀粉酶:＞200U/L(PNP法)
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v > 200) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }

            if (a.contains("AFP") || a.contains("甲胎蛋白")) {
                //1、甲胎蛋白（AFP）≥400 μg/L 甲胎蛋白（AFP）≥200 μg/L
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {
                } else {
                    double v = Double.parseDouble(trim);
                    if (v > 200) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }

            }
            if (a.contains("PSA") || a.contains("前列腺特异性抗原")) {
                //  1、前列腺特异性抗原（PSA）≥10 μg/L
                if (a.contains("/")) {

                } else {
                    String s = map.get(a);
                    String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                    if (trim.equals("")) {
                    } else {
                        double v = Double.parseDouble(trim);
                        if (v >= 10) {
                            rs = "1";
                    rsa = a + "该重大阳性";
                            break;
                        }
                    }
                }
            }
            if ((a.contains("fPSA") || a.contains("游离前列腺特异性抗原")) && a.contains("/") && (a.contains("PSA") || a.contains("前列腺特异性抗原"))) {
                //2、游离前列腺特异性抗原（fPSA）/前列腺特异性抗原（PSA）比值<0.15

                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {
                } else {
                    double v = Double.parseDouble(trim);
                    if (v < 0.15) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (a.contains("CA125")) {
                //1、绝经后女性 CA125 增高到≥95 U/mL
                //2、未绝经女性CA125 增高2倍并结合其他检查结果
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {
                } else {
                    double v = Double.parseDouble(trim);
                    if (v >= 70) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (a.contains("CA153")) {
                String s = map2.get(a);
                int i1 = Integer.parseInt(s);
                if (i1 == 3) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }

            if (a.contains("CA50")) {
                String s = map2.get(a);
                int i1 = Integer.parseInt(s);
                if (i1 == 3) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }

            if (a.contains("CA724")) {
                String s = map2.get(a);
                int i1 = Integer.parseInt(s);
                if (i1 == 3) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }

            if (a.contains("CA242")) {
                //1、绝经后女性 CA125 增高到≥95 U/mL
                //2、未绝经女性CA125 增高2倍并结合其他检查结果
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {
                } else {
                    double v = Double.parseDouble(trim);
                    if (v >= 40) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (a.contains("CA19-9")) {
                //4、糖类抗原19-9（CA19-9）≥ 2倍并结合其他检查结果
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {
                } else {
                    double v = Double.parseDouble(trim);
                    if (v >= 72) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (a.contains("CEA")) {
                //5、癌胚抗原（CEA）≥ 2倍并结合其他检查结果
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {
                } else {
                    double v = Double.parseDouble(trim);
                    if (v >= 10) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }

            if (a.contains("CYFRA21-1") || a.contains("细胞角蛋白19片段")) {
                //细胞角蛋白19片段（CYFRA21-1）≥2倍并结合其他检查结果
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {
                } else {
                    double v = Double.parseDouble(trim);
                    if (v >= 6.6) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }

            if (a.contains("SCC") || a.contains("鳞状细胞癌抗原")) {
                //鳞状细胞癌抗原（SCC）≥2倍并结合其他检查结果
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    if (v >= 3.0) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (a.contains("NSE") || a.contains("神经特异性烯醇化酶")) {
                //神经特异性烯醇化酶（NSE）≥2倍并结合其他检查结果
                String s = map.get(a);
                String s2 = map1.get(a);

                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    getBeiShu.getBeiShu(v, s2);
                    if (v >= 2.0) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }

            if (a.contains("ESR") || a.contains("红细胞沉降率")) {
                //神经特异性烯醇化酶（NSE）≥2倍并结合其他检查结果
                String s = map.get(a);
                String s2 = map1.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {

                    double v = Double.parseDouble(trim);
                    getBeiShu.getBeiShu(v, s2);
                    if (v > 100) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }

            if (a.contains("抗“O”") || a.contains("“O”")) {
                //神经特异性烯醇化酶（NSE）≥2倍并结合其他检查结果
                String s = map.get(a);
                String s2 = map1.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    getBeiShu.getBeiShu(v, s2);
                    if (v > 1000) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            //微量元素
            if (key[0].contains("微量元素") && (key[1].contains("钙") || key[1].contains("Ca"))) {
                //84.0 CA
                String s = map.get(a);
                String s2 = map1.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    double beiShu = getBeiShu.getBeiShu(v, s2);
                    if (beiShu > 2) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (key[0].contains("微量元素") && (key[1].contains("铁") || key[1].contains("Fe"))) {
                //84.0 CA
                String s = map.get(a);
                String s2 = map1.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    double beiShu = getBeiShu.getBeiShu(v, s2);
                    if (beiShu > 2) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (key[0].contains("微量元素") && (key[1].contains("铅") || key[1].contains("Pb"))) {
                //84.0 CA
                String s = map.get(a);
                String s2 = map1.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    double beiShu = getBeiShu.getBeiShu(v, s2);
                    if (beiShu > 2) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (key[0].contains("微量元素") && (key[1].contains("铜") || key[1].contains("Cu"))) {
                //84.0 CA
                String s = map.get(a);
                String s2 = map1.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    double beiShu = getBeiShu.getBeiShu(v, s2);
                    if (v > 1000) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (key[0].contains("微量元素") && (key[1].contains("锌") || key[1].contains("Zn"))) {
                //84.0 CA
                String s = map.get(a);
                String s2 = map1.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    double beiShu = getBeiShu.getBeiShu(v, s2);
                    if (beiShu > 2) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (key[0].contains("微量元素") && (key[1].contains("锰") || key[1].contains("Mn"))) {
                //84.0 CA
                String s = map.get(a);
                String s2 = map1.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    double beiShu = getBeiShu.getBeiShu(v, s2);
                    if (beiShu > 2) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }
            if (key[0].contains("微量元素") && (key[1].contains("镁") || key[1].contains("Mg"))) {
                //84.0 CA
                String s = map.get(a);
                String s2 = map1.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    double beiShu = getBeiShu.getBeiShu(v, s2);
                    if (beiShu > 2) {
                        rs = "1";
                    rsa = a + "该重大阳性";
                        break;
                    }
                }
            }


            if (a.contains("PGI") || a.contains("胃蛋白酶原")) {
                String s = map.get(a);
                String trim = Pattern.compile(REGEX).matcher(s).replaceAll("").trim();
                if (a.contains("/") || a.contains("比值")) {
                    if (trim.equals("")) {

                    } else {
                        double v = Double.parseDouble(trim);
                            pgiz = s;
                    }
                } else {
                    if (trim.equals("")) {

                    } else {
                        double v = Double.parseDouble(trim);
                            pgi = s;

                    }
                }

            }

            if (a.contains("CK-MB") || a.contains("血清肌酸激酶同工酶") || a.contains("肌酸酶同工酶")) {
                String s = map2.get(a);
                int i1 = Integer.parseInt(s);
                if (i1 == 3) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }

            if (a.contains("肌钙蛋白")) {
                String s = map2.get(a);
                int i1 = Integer.parseInt(s);
                if (i1 == 3) {
                    rs = "1,"+"1";
                    break;
                }
            }

            if (a.contains("肌红蛋白")) {
                String s = map2.get(a);
                int i1 = Integer.parseInt(s);
                if (i1 == 3) {
                    rs = "1";
                    rsa = a + "该重大阳性";
                    break;
                }
            }
        }
        if(pgi.equals("")||pgiz.equals("")){

        }else {
            if (Double.parseDouble(pgi) <= 70 && Double.parseDouble(pgiz) <= 7.0) {
                rs =   "1";
                rsa = "pgi呈重大阳性";
            }
        }
        weiBaoM.setLabel(rs);
        weiBaoM.setAbnormal(rsa);
        msg.setWeiBaoM(weiBaoM);

        /*json.put("code",200);
        json.put("msg","success");
        json.put("data",rs);*/


        return msg;
    }

    public  void test() throws IOException {
        String rptid = null;
        String pathname = "D:\\workspace\\new\\spring-apps\\datag\\src\\main\\excel\\wbcs.txt";
        FileReader reader = null;
        try {
            reader = new FileReader(pathname);
            BufferedReader br = new BufferedReader(reader);
            String line;
            int i =1;
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                rptid = line;
                System.out.println(rptid+","+i);
                i++;
               getRep1(rptid);

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String str = "2018-02-01 19:10:05";
        String substring = str.substring(0, 10);
        String[] split = str.split("_");
        System.out.println(substring);
    }
}
