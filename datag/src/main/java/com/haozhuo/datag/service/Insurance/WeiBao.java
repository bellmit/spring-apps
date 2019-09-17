package com.haozhuo.datag.service.Insurance;

import com.haozhuo.datag.com.service.Insurance.matchMain;
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

import javax.xml.bind.SchemaOutputResolver;
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
        return stream(searchHits).map(x -> x.getSourceAsMap().get("lastUpdateTime")).findFirst().orElse("").toString();
    }

    public String getRep1(String rptid) {
        String rs = "正常";
        String REGEX = "[^0-9.]";
        List list1 = new ArrayList();
        // SubstringComparator substringComparator = new SubstringComparator(rptid);
        String day = getchkday(rptid);
        StringBuffer sb = new StringBuffer(day);
        String day1 = day.substring(0, 10);
        StringBuffer sb1 = new StringBuffer(day1);
        StringBuffer rowkey = sb1.append("_" + rptid + "_");
        String endrowkey = day1 + "_" + (Integer.parseInt(rptid) + 1) + "_";
        Map<String, String> map = new HashMap<>();
        String rsval = "";
        StringBuffer sb2 = new StringBuffer(rsval);
        Scan scan = new Scan();
        // scan.setFilter(filter);
        scan.setStartRow(rowkey.toString().getBytes());
        scan.setStopRow(endrowkey.getBytes());
        hbaseTemplate.find(HBASENAME, scan, (Result result, int i) -> {
            Cell[] cells = result.rawCells();
            for (Cell cell : cells) {
                String key = new String(CellUtil.cloneQualifier(cell));
                String value = new String(CellUtil.cloneValue(cell));
                String rowName = new String(CellUtil.cloneRow(cell));
                String[] rownmaes = rowName.split("_");
                if (key.equals("rs_val")) {
                    map.put(rownmaes[2] + "," + rownmaes[3], value);
                    sb2.append(value);
                }
                //System.out.println(key+","+value);
            }
            return map;
        });
        String str = sb2.toString();
        System.out.println(str);

        int i = matchMain.noNumMatch(sb2.toString());
        System.out.println(i);
        List<String> list = new ArrayList<>();
        String niaotang = null;
        String tongti = null;
        for (String a : map.keySet()) {
            if ((a.contains("一般") || a.contains("血压") || a.contains("内科")) && a.contains("收缩压")) {
                //收缩压≥180mmHg，伴急性症状或安静休息后复测仍达此标准     收缩压＜80mmHg，伴周围循环衰竭表现
                String s = map.get(a);
                int i1 = Integer.parseInt(s);

                if (i1>=180||i1<80){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if ((a.contains("一般") || a.contains("血压") || a.contains("内科")) && a.contains("舒张")) {
                //2、舒张压≥110mmHg，伴急性症状或安静休息后复测仍达此标准  4、舒张压＜50mmHg，伴周围循环衰竭表现
                String s = map.get(a);
                int i1 = Integer.parseInt(s);
                if (i1>=110||i1<50){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("内科") && a.contains("心率")) {
                //1、心率持续≥150次/分，伴心律不齐或心界扩大
                //2、心率≤45次/分，伴心律不齐或心界扩大
                String s = map.get(a);
                int i1 = Integer.parseInt(s);
                if ((i1<=45||i1>=150)&&(str.contains("心律不齐")||str.contains("心界扩大"))){
                    rs = a+"呈重大阳性";
                    break;
                }
            }

            if (a.contains("血常规") && (a.contains("血红蛋白测定") || a.contains("Hb"))) {
                //血红蛋白（Hb）≤60g/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v<=60){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("血常规") && (a.contains("白细胞计数") || a.contains("WBC"))) {
                //白细胞计数（WBC）≤1.0×10^9/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v<=1.0){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("血常规") && (a.contains("血小板计数") || a.contains("PLT"))) {
                //3、血小板计数(PLT)≤30.0×10^9/L或有明显出血倾向   4、血小板计数(PLT)≥1000.0×10^9/L

                //5、血小板计数(PLT)在30—50×10^9/L之间
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v<=30.0||v>=1000||(v>=30&&v<=50)){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("血常规") && (a.contains("中性粒细胞绝对值") || a.contains("NEU"))) {
                //5、中性粒细胞绝对值（NEU）≤0.5×10^9/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v<=0.5){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("肝功能") && (a.contains("ALT") || a.contains("丙") || a.contains("丙氨酸"))) {
                //1、血清丙氨酸氨基转移酶（ALT）≥600 U/L
                // 1、血清丙氨酸氨基转移酶(ALT)＞200U/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>200){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("肝功能") && (a.contains("AST") || a.contains("草") || a.contains("冬氨酸"))) {
                //2、血清天门冬氨酸氨基转移酶(AST)≥600 U/L
                // 2、血清天门冬氨酸氨基转移酶(AST)＞200U/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>200){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("空腹") && a.contains("血糖")) {
                //3、空腹血糖（FPG）≥16.7 mmol/L（糖尿病史）
                // 4、空腹血糖（FPG）≥13.9mmol/L，合并尿酮体
                // 6、空腹血糖（FPG）≤2.8mmol/L（无糖尿病史）
                // 7、空腹血糖（FPG）≤ 3.9mmol/L（糖尿病史）
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>=16.7&&str.contains("糖尿病")){
                    rs = a+"呈重大阳性";
                    break;
                }
                if(v>=13.9){

                }
                if (v<=2.8){
                    if (str.contains("糖尿病")){

                    }else {
                        rs = a+"呈重大阳性";
                        break;
                    }
                }
                if (v<=3.9){
                    if (str.contains("糖尿病")){
                        rs = a+"呈重大阳性";
                        break;
                    }
                }
            }
            if (a.contains("肾功") && a.contains("血") && a.contains("肌酐")) {
                //8、血肌酐(Cr) ≥707μmol/L
                //10、血肌酐(Cr) ≥445μmol/L（首次）
                //11、血肌酐(Cr) ≥707μmol/L（历次）.
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>=445){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("眼压") || (a.contains("眼科") && a.contains("眼压"))) {
                //11、眼压＞25mmHg
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>=25){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("血常规") && (a.contains("红细胞计数") || a.contains("RBC"))) {
                //1、红细胞计数(RBC)＜2.5×10^12/L   2、红细胞计数(RBC)＞6.8×10^12/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v<2.5||v>6.8){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("血常规") && (a.contains("白细胞计数") || a.contains("WBC"))) {
                //3、白细胞计数(WBC)≤2.0×10^9/L  4、白细胞计数(WBC)≥30.0×10^9/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v<2.5||v>6.8){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("尿常规") && (a.contains("隐血") || a.contains("潜血"))) {
                //、尿潜血﹥+++
                String s = map.get(a);
                if (s.contains("3+")||s.contains("+++")||s.contains("4+")||s.contains("5+")||s.contains("++++")||s.contains("+++++")){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("尿常规") && (a.contains("尿蛋白") || a.contains("PRO"))) {
                //2、尿蛋白﹥+++
                String s = map.get(a);
                if (s.contains("3+")||s.contains("+++")||s.contains("4+")||s.contains("5+")||s.contains("++++")||s.contains("+++++")){
                    rs = a+"呈重大阳性";
                    break;
                }
            }

            if (a.contains("尿常规") && (a.contains("糖") || a.contains("GLU"))) {
                //、尿糖+ + +～ + + + +
                 niaotang = map.get(a);
                if (niaotang.contains("3+")||niaotang.contains("+++")||niaotang.contains("4+")||niaotang.contains("++++")){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("尿常规") && a.contains("镜检") && a.contains("白")) {

                //5、镜检白细胞（WBC）＞20
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>20){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("尿常规") && a.contains("镜检") && a.contains("红")) {
                //4、镜检红细胞（RBC）＞20
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>20){
                    rs = a+"呈重大阳性";
                    break;
                }
            }

            if (a.contains("尿常规") && a.contains("酮体")) {
                //6、酮体≥++（糖尿病史）
                //7、酮体≥+++（无糖尿病史）
                 tongti = map.get(a);
                if (tongti.contains("2+")||tongti.contains("++")&&str.contains("糖尿病")){
                    rs = a+"呈重大阳性";
                    break;
                }else if (tongti.contains("3+")||tongti.contains("+++")){
                    if (str.contains("糖尿病")){

                    }else {
                        rs = a+"呈重大阳性";
                        break;
                    }
                }
            }
            if (a.contains("便常规") && (a.contains("便隐血") || a.contains("OB"))) {
                //2、大便隐血(OB)≥+++
                String s = map.get(a);
                if (s.contains("3+")||s.contains("+++")){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("肝功") && (a.contains("谷氨酰基") || a.contains("GGT"))) {
                //3、γ-谷氨酰转移酶（GGT）＞200U/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>200){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("肝功") && (a.contains("总蛋白") || a.contains("TP"))) {
                // 4、总蛋白＜40g/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v<40){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("肝功") && (a.contains("白蛋白") || a.contains("Alb"))) {
                //5、白蛋白＜20g/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v<20){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("肝功") && (a.contains("总胆红素") || a.contains("T-Bil"))) {
                //6、总胆红素＞50μmol/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>50){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("餐后") && (a.contains("血糖"))) {
                //8、餐后血糖≥17mmol/L，伴尿糖 +++，尿酮+
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());

                if (v>=17&&niaotang.contains("+++")&&niaotang.contains("+")){
                    rs = a+"呈重大阳性";
                    break;

                }
            }
            if (a.contains("肾功") && (a.contains("尿素") || a.contains("Urea"))) {
                //9、尿素(Urea)＞21mmol/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>21){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("肾功") && (a.contains("尿酸") || a.contains("UA"))) {

                //  12、尿酸(UA)＞650μmol/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>650){
                    rs = a+"呈重大阳性";
                    break;
                }
            }

            if (a.contains("肾功") && (a.contains("总胆固醇") || a.contains("TC"))) {
                //13、总胆固醇(TC)＞8mmol/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>8){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("肾功") && (a.contains("甘油三酯") || a.contains("TG"))) {
                //14、甘油三酯(TG)＞10mmol/L
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>10){
                    rs = a+"呈重大阳性";
                    break;
                }
            }
            if (a.contains("血清淀粉酶")) {
                //16、血清淀粉酶:＞200U/L(PNP法)
                String s = map.get(a);
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s).replaceAll("").trim());
                if (v>200){
                    rs = a+"呈重大阳性";
                    break;
                }
            }


        }

        return rs;
    }
}
