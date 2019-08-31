package com.haozhuo.datag.service.Insurance;

import ch.qos.logback.core.joran.conditional.ElseAction;
import com.haozhuo.datag.com.service.Insurance.*;
import com.haozhuo.datag.model.report.HongKang;

import com.haozhuo.datag.model.report.RepAbnormal;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.HbaseService;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
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
import scala.collection.mutable.ArrayBuffer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class Hongkang {
    private static final Logger logger = LoggerFactory.getLogger(HbaseService.class);
    @Autowired
    private HbaseTemplate hbaseTemplate;
    @Autowired
    private EsService esService;

    private final static String HBASENAME = "DATAETL:RPT_IND";

    public HongKang getAbnormalValueForHongkang(String idcard) {
        String reportId = esService.getrptid(idcard);
        String day = esService.getchkday(idcard);
        String a = esService.getLabelsByReportId(reportId);
        System.out.println(day);
        System.out.println(reportId);
        StringBuffer sb = new StringBuffer(day);

        StringBuffer rowkey = sb.append("_" + reportId + "_");
        System.out.println(rowkey);
        String endrowkey = day + "_" + (Integer.parseInt(reportId) + 1) + "_";
        HongKang hongKang = new HongKang();
        Scan scan = new Scan();
        scan.setStartRow(rowkey.toString().getBytes());
        scan.setStopRow(endrowkey.getBytes());

        hbaseTemplate.find(HBASENAME, scan, (Result result, int i) -> {
            Cell[] cells = result.rawCells();
            for (Cell cell : cells) {
                String key = new String(CellUtil.cloneQualifier(cell));
                String value = new String(CellUtil.cloneValue(cell));
                String rowName = new String(CellUtil.cloneRow(cell));
                String[] rownmaes = rowName.split("_");
                //  System.out.println(rowName + "," + key + "," + value);
                if ((rownmaes[2].contains("外科") && rownmaes[3].equals("小结"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setWaike(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("血压") || rownmaes[2].contains("一般") || rownmaes[2].contains("基础")) && rownmaes[3].equals("收缩压")) {
                    if (key.equals("rs_val")) {
                        hongKang.setShousuoya(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }

                if ((rownmaes[2].contains("血压") || rownmaes[2].contains("一般") || rownmaes[2].contains("基础")) && rownmaes[3].equals("舒张压")) {
                    if (key.equals("rs_val")) {
                        hongKang.setShuzhangya(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }


                if ((rownmaes[2].contains("体重指数") || rownmaes[2].contains("一般") || rownmaes[2].contains("人体成分分析") || rownmaes[2].contains("体重") || rownmaes[2].contains("基础")) && rownmaes[3].contains("体重指数")) {
                    if (key.equals("rs_val")) {
                        hongKang.setBmi(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("耳鼻喉")) && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        hongKang.setErbihou(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }

                //待定
                if ((rownmaes[2].contains("眼科")) && rownmaes[3].contains("小结") || (rownmaes[2].contains("视力")) && rownmaes[3].contains("小结") || (rownmaes[2].contains("眼底")) && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        hongKang.setYanke(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }

                if ((rownmaes[2].contains("口腔")) && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        hongKang.setKouqiang(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("颈动脉")) && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        hongKang.setJingdongmai(value);
                        System.out.println(rowName + "," + key + "," + value);

                    }
                }

                if ((rownmaes[2].contains("心脏")) && rownmaes[3].contains("描述")) {
                    if (key.equals("rs_val")) {
                        hongKang.setXinzangcaichao(value);
                        System.out.println(rowName + "," + key + "," + value);

                    }
                }
                //待定
                if ((rownmaes[2].contains("腹部") && rownmaes[3].contains("肝")) || rownmaes[2].contains("肝胆脾胰") && rownmaes[3].contains("肝")) {
                    if (key.equals("rs_val")) {
                        hongKang.setGan(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("腹部") && rownmaes[3].contains("胆")) || rownmaes[2].contains("肝胆脾胰") && rownmaes[3].contains("胆")) {
                    if (key.equals("rs_val")) {
                        hongKang.setDan(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("腹部") && rownmaes[3].contains("脾")) || rownmaes[2].contains("肝胆脾胰") && rownmaes[3].contains("脾")) {
                    if (key.equals("rs_val")) {
                        hongKang.setPi(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("腹部") && rownmaes[3].contains("胰")) || rownmaes[2].contains("肝胆脾胰") && rownmaes[3].contains("胰")) {
                    if (key.equals("rs_val")) {
                        hongKang.setYi(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("腹部") && rownmaes[3].contains("左肾")) || (rownmaes[2].contains("肝胆脾胰") && rownmaes[3].contains("左肾"))
                    ||(rownmaes[2].contains("双肾")&&rownmaes[3].contains("左肾"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setZuoshen(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("腹部") && rownmaes[3].contains("右肾")) || (rownmaes[2].contains("肝胆脾胰") && rownmaes[3].contains("右肾"))
                        ||(rownmaes[2].contains("双肾")&&rownmaes[3].contains("右肾"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setYoushen(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }


                if ((rownmaes[2].contains("输尿管")) && rownmaes[3].contains("输尿管")) {
                    if (key.equals("rs_val")) {
                        hongKang.setShuniaoguan(value);
                        System.out.println(rowName + "," + key + "," + value);

                    }
                }

                if ((rownmaes[2].contains("膀胱") || rownmaes[2].contains("泌尿")) && rownmaes[3].contains("膀胱")) {
                    if (key.equals("rs_val")) {
                        hongKang.setPangguang(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("前列腺") || rownmaes[2].contains("泌尿") || rownmaes[2].contains("盆腔")) && rownmaes[3].contains("前列腺")) {
                    if (key.equals("rs_val")) {
                        hongKang.setQianliexian(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }


                if ((rownmaes[2].contains("子宫") || rownmaes[2].contains("妇科") || rownmaes[2].contains("阴式")) && rownmaes[3].contains("子宫")) {
                    if (key.equals("rs_val")) {
                        hongKang.setZigong(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }

                if ((rownmaes[2].contains("甲状腺") && rownmaes[3].contains("小结"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setJiazuangxian(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if (((rownmaes[2].contains("乳腺") || rownmaes[2].contains("双乳") || rownmaes[2].contains("乳房") )&& rownmaes[3].contains("描述"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setRuxian(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                /**
                 * 胸片
                 */
                if ((((rownmaes[2].contains("胸") && rownmaes[2].contains("CT"))) && rownmaes[3].contains("小结")) || ((rownmaes[2].contains("肺") && rownmaes[2].contains("CT")) && rownmaes[3].contains("小结"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setXiongct(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                /**
                 * 心电图
                 */
                if (rownmaes[2].contains("心电图") && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        hongKang.setXindiantu(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                /**
                 * 尿常规
                 */
                if ((rownmaes[2].contains("尿常规") || rownmaes[2].contains("尿检")) && rownmaes[3].contains("糖")) {
                    if (key.equals("rs_val")) {
                        hongKang.setNiaotang(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("尿常规") || rownmaes[2].contains("尿检")) && rownmaes[3].contains("蛋白")) {
                    if (key.equals("rs_val")) {
                        hongKang.setNiaodanbai(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("尿常规") || rownmaes[2].contains("尿检")) && rownmaes[3].contains("潜血")) {
                    if (key.equals("rs_val")) {
                        hongKang.setNiaoqianxue(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                /**
                 * 血常规
                 */
                if (rownmaes[2].contains("血常规") && rownmaes[3].contains("白细胞计数")) {
                    if (key.equals("rs_val")) {
                        hongKang.setBaixibao(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setBaixibao_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if (rownmaes[2].contains("血常规") && rownmaes[3].contains("中性粒细胞绝对值")) {
                    if (key.equals("rs_val")) {
                        hongKang.setLixibao(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setLixibao_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                if (rownmaes[2].contains("血常规") && rownmaes[3].contains("红细胞计数")) {
                    if (key.equals("rs_val")) {
                        hongKang.setHongxibao(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setHongxibao_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if (rownmaes[2].contains("血常规") && rownmaes[3].contains("血小板")) {
                    if (key.equals("rs_val")) {
                        hongKang.setXuexiaoban(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setXuexiaoban_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }

                /**
                 * 免疫
                 */
                if (rownmaes[3].contains("EB病毒")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setEbbingdu(value);
                        System.out.println(rowName + "," + key + "," + value);

                    }
                }

                if (rownmaes[2].contains("风湿") && rownmaes[3].contains("风湿")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setLeifengshiyinzi(value);
                        System.out.println(rowName + "," + key + "," + value);

                    }
                }

                if (rownmaes[2].contains("丙肝") && rownmaes[3].contains("丙")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setBingganbingdukangti(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                if ((rownmaes[2].contains("丙肝") && rownmaes[2].contains("RNA")) && (rownmaes[3].contains("丙") || rownmaes[3].contains("RNA"))) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setBingganbingdurna(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                if (rownmaes[2].contains("梅毒") && rownmaes[3].contains("梅毒")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setMeidu(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                if (rownmaes[2].contains("HIV") && rownmaes[3].contains("HIV")) {
                    if (key.equals("rs_hflag_id")) {
                        hongKang.setHiv(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }

                if (rownmaes[2].contains("乙肝") && rownmaes[3].contains("乙肝表面抗原")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setYiganbiaomiankangyuan(value);
                        System.out.println(rowName + "," + key + "," + value);

                    }

                }

                if (rownmaes[2].contains("血沉") && rownmaes[3].contains("血沉")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setXuechen(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                /**
                 * 肝
                 */
                if (rownmaes[2].contains("肝功") && rownmaes[3].contains("/")) {
                    if (key.equals("rs_val")) {
                        hongKang.setXuebiqiu(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setXuebiqiu_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }


                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("谷氨酰基") || rownmaes[3].contains("GT"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setGt(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setGt_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }

                if ((rownmaes[2].contains("肝功") || rownmaes[2].contains("ALT")) && (rownmaes[3].contains("丙") || rownmaes[3].contains("ALT"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setAlt(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setAlt_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                if ((rownmaes[2].contains("肝功") || rownmaes[2].contains("AST")) && (rownmaes[3].contains("草") || rownmaes[3].contains("冬氨酸") || rownmaes[3].contains("AST"))) {

                    if (key.equals("rs_val")) {
                        hongKang.setAst(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setAst_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("胆汁酸") || rownmaes[3].contains("TBA"))) {

                    if (key.equals("rs_val")) {
                        hongKang.setTba(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setAst_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("胆红素") || rownmaes[3].contains("T-Bil"))) {

                    if (key.equals("rs_val")) {
                        hongKang.setTbil(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setTbil_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("总蛋白") || rownmaes[3].contains("TP"))) {

                    if (key.equals("rs_val")) {
                        hongKang.setTp(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setTp_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("球蛋白"))) {

                    if (key.equals("rs_val")) {
                        hongKang.setQiudanbai(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setQiudanbai_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("血清白蛋白") || rownmaes[3].contains("Alb"))) {

                    if (key.equals("rs_val")) {
                        hongKang.setBaidanbai(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setBaidanbai_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("碱性磷酸酶") || rownmaes[3].contains("ALP"))) {


                    if (key.equals("rs_val")) {
                        hongKang.setAlp(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setAlp_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }


                }
                /**
                 * 肾
                 */
                if (rownmaes[2].contains("肾功") && (rownmaes[3].contains("肌酐"))) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setJigan(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                /**
                 * 血脂
                 */
                if ((rownmaes[2].contains("血脂") || rownmaes[2].contains("血清")) && (rownmaes[3].contains("TC"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setTc(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setTc_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("血脂") || rownmaes[2].contains("血清")) && (rownmaes[3].contains("TG"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setTg(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setTg_ref(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }

                }
                /**
                 * 血糖
                 */
                if ((rownmaes[2].contains("血糖") || rownmaes[2].contains("生化")) && rownmaes[3].contains("血糖")) {
                    if (key.equals("rs_val")) {
                        hongKang.setKongfuxuetang(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                    if (key.equals("rs_flag_id")) {
                        hongKang.setKongfuxuetangid(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if (rownmaes[2].contains("糖化血红蛋白") && rownmaes[3].contains("糖化血红蛋白")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setTanghuaxuehongdanbai(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("肿瘤"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setZhongliu(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }
                if ((rownmaes[2].contains("TCT") && rownmaes[3].contains("小结")) || (rownmaes[2].contains("阴道镜") && rownmaes[3].contains("结果"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setGongjingtct(value);
                        System.out.println(rowName + "," + key + "," + value);
                    }
                }

            }


            return hongKang;
        });

        return hongKang;
    }

    public HongKang getHongkangValue(String idcard) {
        String rptid = esService.getrptid(idcard);
        HongKang hongKang = getAbnormalValueForHongkang(idcard);
        String a = esService.getLabelsByReportId(rptid);
        Double beiShu;
        String REGEX = "[^0-9.]";
        if (hongKang.getWaike() == null) {
            hongKang.setWaike("0");
        } else if (hongKang.getWaike().contains("肢体残疾") || hongKang.getWaike().contains("疤痕") || hongKang.getWaike().contains("压痛") || hongKang.getWaike().contains("疼痛")
                || hongKang.getWaike().contains("包块") || hongKang.getWaike().contains("淋巴结")) {
            hongKang.setWaike("2");
        } else {
            hongKang.setWaike("1");
        }
        if (hongKang.getShousuoya() == null) {
            hongKang.setShousuoya("0");
        } else if (Integer.parseInt(hongKang.getShousuoya()) >= 170) {
            hongKang.setShousuoya("3");
        } else if (Integer.parseInt(hongKang.getShousuoya()) >= 140 && Integer.parseInt(hongKang.getShousuoya()) < 170) {
            hongKang.setShousuoya("2");
        } else {
            hongKang.setShousuoya("1");
        }

        if (hongKang.getShuzhangya() == null) {
            hongKang.setShuzhangya("0");
        } else if (Integer.parseInt(hongKang.getShuzhangya()) >= 105) {
            hongKang.setShuzhangya("3");
        } else if (Integer.parseInt(hongKang.getShuzhangya()) < 105 && Integer.parseInt(hongKang.getShuzhangya()) >= 90) {
            hongKang.setShuzhangya("2");
        } else {
            hongKang.setShuzhangya("1");
        }

        if (equals(hongKang.getShousuoya(), "3") || equals(hongKang.getShuzhangya(), "3")) {
            hongKang.setXueya("3");
        } else if (equals(hongKang.getShousuoya(), "2") || equals(hongKang.getShuzhangya(), "2")) {
            hongKang.setXueya("2");
        } else if (equals(hongKang.getShousuoya(), "1") || equals(hongKang.getShuzhangya(), "1")) {
            hongKang.setXueya("1");
        } else {
            hongKang.setXueya("0");
        }

        if (hongKang.getBmi() == null) {
            hongKang.setBmi("0");
        } else if (Double.parseDouble(hongKang.getBmi()) >= 35 || Double.parseDouble(hongKang.getBmi()) <= 15) {
            hongKang.setBmi("3");
        } else if ((Double.parseDouble(hongKang.getBmi()) > 29 && Double.parseDouble(hongKang.getBmi()) < 35) || (Double.parseDouble(hongKang.getBmi()) > 15 && Double.parseDouble(hongKang.getBmi()) < 18)) {
            hongKang.setBmi("2");
        } else {
            hongKang.setBmi("1");
        }


        if (hongKang.getErbihou() == null) {
            hongKang.setErbihou("0");
        } else if (hongKang.getErbihou().contains("听力异常") || hongKang.getErbihou().contains("鼓膜穿孔")) {
            hongKang.setErbihou("2");
        } else {
            hongKang.setErbihou("1");
        }


        //眼科 问题------------
        if (hongKang.getYanke() == null) {
            hongKang.setYanke("0");
        } else {
            hongKang.setYanke("1");
        }
        //口腔
        if (hongKang.getKouqiang() == null) {
            hongKang.setKouqiang("0");
        } else if (hongKang.getKouqiang().contains("黏膜白斑") || hongKang.getKouqiang().contains("肿物") || hongKang.getKouqiang().contains("包块") || hongKang.getKouqiang().contains("赘生物")) {
            hongKang.setKouqiang("2");
        } else {
            hongKang.setKouqiang("1");
        }

        //颈椎
        if (hongKang.getJingdongmai() == null) {
            hongKang.setJingdongmai("0");
        } else if (hongKang.getJingdongmai().contains("斑块")) {
            hongKang.setJingdongmai("3");
        } else if (hongKang.getJingdongmai().contains("增厚")) {
            hongKang.setJingdongmai("3");
        } else {
            hongKang.setJingdongmai("1");
        }

        //心脏
        if (hongKang.getXinzangcaichao() == null) {
            hongKang.setXinzangcaichao("0");
        } else if (hongKang.getXinzangcaichao().contains("扩大") || hongKang.getXinzangcaichao().contains("增厚") || hongKang.getXinzangcaichao().contains("同向运动") || hongKang.getXinzangcaichao().contains("赘生物") || hongKang.getXinzangcaichao().contains("肺动脉高压")) {
            hongKang.setXinzangcaichao("3");
        } else if (hongKang.getXinzangcaichao().contains("返流") || hongKang.getXinzangcaichao().contains("关闭不全") || hongKang.getXinzangcaichao().contains("脱垂") || hongKang.getXinzangcaichao().contains("假键索")) {
            hongKang.setXinzangcaichao("2");
        } else {
            hongKang.setXinzangcaichao("1");
        }

        //腹部
        if (hongKang.getGan() == null) {
            hongKang.setGan("0");
        } else {
            String feibu = GetDataNK_test.fubu(hongKang.getGan());
            if (Integer.parseInt(feibu) == 0) {
                hongKang.setGan("3");
            } else if (hongKang.getGan().contains("中度脂肪肝")) {
                hongKang.setGan("2");
            } else {
                hongKang.setGan("1");
            }
        }

        if (hongKang.getDan() == null) {
            hongKang.setDan("0");
        } else {
            String feibu = GetDataNK_test.fubu(hongKang.getDan());
            if (Integer.parseInt(feibu) == 0) {
                hongKang.setDan("3");
            } else if (hongKang.getDan().contains("泥沙样结石")) {
                hongKang.setDan("2");
            } else {
                hongKang.setDan("1");
            }
        }

        if (hongKang.getYi() == null) {
            hongKang.setYi("0");
        } else {
            String feibu = GetDataNK_test.fubu(hongKang.getYi());
            if (Integer.parseInt(feibu) == 0) {
                hongKang.setYi("3");
            } else if (hongKang.getYi().contains("囊肿")) {
                hongKang.setYi("2");
            } else {
                hongKang.setYi("1");
            }
        }

        if (hongKang.getPi() == null) {
            hongKang.setPi("0");
        } else {
            String feibu = GetDataNK_test.fubu(hongKang.getPi());
            if (Integer.parseInt(feibu) == 0) {
                hongKang.setPi("3");
            } else if (hongKang.getPi().contains("切除")) {
                hongKang.setPi("2");
            } else {
                hongKang.setPi("1");
            }
        }

 /*       if (hongKang.getShen() == null) {
            hongKang.setShen("0");
        } else {
            String feibu = GetDataS_test.getStatus(hongKang.getShen());
            if (hongKang.getShen().contains("积水") || hongKang.getShen().contains("肾盂扩张") || hongKang.getShen().contains("马蹄肾") || hongKang.getShen().contains("多囊肾")) {
                hongKang.setShen("3");
            } else if (Integer.parseInt(feibu) == 0) {
                hongKang.setShen("2");
            } else {
                hongKang.setShen("1");
            }
        }*/

        if (equals(hongKang.getGan(), "3") || equals(hongKang.getDan(), "3") || equals(hongKang.getPi(), "3") || equals(hongKang.getYi(), "3") ||
                a.contains("胆管结石") || a.contains("胆管扩张") || a.contains("胆囊萎缩") || a.contains("多囊肝") || a.contains("多囊脾") || a.contains("多囊肾") || a.contains("肝内占位病变")
                || a.contains("肝异常回声") || a.contains("肝脏增大") || a.contains("卵巢畸胎瘤") || a.contains("卵巢占位性病变")) {
            hongKang.setFubucaichao("3");
        } else if (equals(hongKang.getGan(), "2") || equals(hongKang.getDan(), "2") || equals(hongKang.getPi(), "2") || equals(hongKang.getYi(), "2") ||
                a.contains("单侧肾缺如") || a.contains("胆囊增大") || a.contains("胆囊占位性病变") || a.contains("腹壁疝") || a.contains("腹股沟淋巴结")
                || a.contains("腹股沟疝") || a.contains("肝弥漫性病变") || a.contains("肝内胆管结石") || a.contains("肝内异常回声")
                || a.contains("海绵肾") || a.contains("脾钙化灶") || a.contains("脾囊肿") || a.contains("脾血管瘤") || a.contains("脾脏未探及") || a.contains("胰腺囊肿") || a.contains("游走肾")
                || a.contains("脂肪瘤") || a.contains("子宫稍大") || a.contains("子宫体积增大") || a.contains("子宫未探及") || a.contains("子宫增大")) {
            hongKang.setFubucaichao("2");
        } else  if (equals(hongKang.getGan(), "0") && equals(hongKang.getDan(), "0") && equals(hongKang.getPi(), "0") && equals(hongKang.getYi(), "0")) {
            hongKang.setFubucaichao("0");
        } else{
            hongKang.setFubucaichao("1");
        }

        if (hongKang.getZuoshen()==null && hongKang.getYoushen()==null){
            hongKang.setShen("0");
        }else {
            if (hongKang.getZuoshen().contains("积水")||hongKang.getZuoshen().contains("肾盂扩张")||hongKang.getZuoshen().contains("马蹄肾")||hongKang.getZuoshen().contains("多囊肾")||
                    hongKang.getYoushen().contains("积水")|| hongKang.getYoushen().contains("肾盂扩张")||hongKang.getYoushen().contains("马蹄肾")||hongKang.getYoushen().contains("多囊肾")){
                hongKang.setShen("3");
            }else {
                String status = GetDataS_test.getStatus(hongKang.getZuoshen());
                String status1 = GetDataS_test.getStatus(hongKang.getYoushen());
                if (Integer.parseInt(status)==0||Integer.parseInt(status1)==0){
                    hongKang.setShen("2");
                }else {
                    hongKang.setShen("1");
                }
            }
        }

        //输尿管
        if (hongKang.getShuniaoguan() == null) {
            hongKang.setShuniaoguan("0");
        } else {
            if (hongKang.getShuniaoguan().contains("结石") || hongKang.getShuniaoguan().contains("扩张")) {
                hongKang.setShuniaoguan("3");
            } else if (hongKang.getShuniaoguan().contains("管壁增厚") || hongKang.getShuniaoguan().contains("狭窄")) {
                hongKang.setShuniaoguan("2");
            } else {
                hongKang.setShuniaoguan("1");
            }
        }

        //膀胱
        if (hongKang.getPangguang() == null) {
            hongKang.setPangguang("0");
        } else if (hongKang.getPangguang().contains("包块占位") || hongKang.getPangguang().contains("赘生物") || hongKang.getPangguang().contains("漂浮物")) {
            hongKang.setPangguang("3");
        } else if (hongKang.getPangguang().contains("结石")) {
            hongKang.setPangguang("2");
        } else {
            hongKang.setPangguang("1");
        }
        //子宫
        if (hongKang.getZigong() == null) {
            hongKang.setZigong("0");
        } else {
            String status = GetDataZG_test.getStatus(hongKang.getZigong());
            if (Integer.parseInt(status) == 0) {
                hongKang.setZigong("3");
            } else {
                hongKang.setZigong("1");
            }
        }
        if (hongKang.getQianliexian() == null) {
            hongKang.setQianliexian("0");
        } else {
            if (hongKang.getQianliexian().contains("囊肿") || hongKang.getQianliexian().contains("钙化") || hongKang.getQianliexian().contains("钙化")) {
                hongKang.setQianliexian("2");
            } else {
                hongKang.setQianliexian("1");
            }
        }


        //甲状腺 有问题
        if (hongKang.getJiazuangxian() == null) {
            hongKang.setJiazuangxian("0");
        } else {
            String status = GetDataJZX_test.getStatus(hongKang.getJiazuangxian());
            if (Integer.parseInt(status) == 0) {
                hongKang.setJiazuangxian("3");
            } else if (hongKang.getJiazuangxian().contains("结节")) {
                hongKang.setJiazuangxian("2");
            } else {
                hongKang.setJiazuangxian("1");
            }
        }
        //乳腺
        if (hongKang.getRuxian() != null) {
            String status = GetDataRX_test.getStatus(hongKang.getRuxian());
            if (Integer.parseInt(status) == 0) {
                hongKang.setRuxian("3");
            } else if (Integer.parseInt(status) == 1) {
                if (hongKang.getRuxian().contains("结节")) {
                    hongKang.setRuxian("2");
                } else {
                    hongKang.setRuxian("1");
                }
            }
        } else {
            hongKang.setRuxian("0");
        }



        //胸片
        if (hongKang.getXiongct() == null) {
            hongKang.setXiongct("0");
        } else if (hongKang.getXiongct().contains("磨玻璃结节") || hongKang.getXiongct().contains("纤维囊性病") || a.contains("肺部罗音") || a.contains("肺动脉增宽") || a.contains("肺间质性改变") || a.contains("肺结核")
                || a.contains("肺结节影") || a.contains("肺门影增大") || a.contains("肺内结节影") || a.contains("肺内硬结灶") || a.contains("肺气肿") || a.contains("肺炎") || a.contains("肺野内密度增高影性质待查")
                || a.contains("肺肿块影") || a.contains("肺转移瘤") || a.contains("慢性支气管炎") || a.contains("纵隔增宽")) {
            hongKang.setXiongct("3");
        } else if (hongKang.getXiongct().contains("结节") || hongKang.getXiongct().contains("阴影") || hongKang.getXiongct().contains("胸膜增厚") || hongKang.getXiongct().contains("肺大泡") || a.contains("肺纤维灶")) {
            hongKang.setXiongct("2");
        } else {
            hongKang.setXiongct("1");
        }

        //心电图
        if (hongKang.getXindiantu() == null) {
            hongKang.setXindiantu("0");
        } else {
            String status = GetDataXDT_test.getStatus(hongKang.getXindiantu());
            if ((a.contains("心电图左心室肥大伴劳损") || a.contains("心房纤颤") || a.contains("心肌梗塞") || a.contains("心肌缺血") || Integer.parseInt(status) == 0)) {
                hongKang.setXindiantu("3");
            } else if (a.contains("病窦综合征") || a.contains("长Q-T间期综合征") || a.contains("窦房结内游走心律") || a.contains("短P-R间期综合征") || a.contains("房室分离") || a.contains("冠状窦性心律") || a.contains("交界性心律") || a.contains("心电图:预激综合征（A型）")
                    || a.contains("心电图Q波异常") || a.contains("心电图ST-T改变") || a.contains("心电图ST段改变") || a.contains("心电图T波改变") || a.contains("心电图冠状窦性心律") || a.contains("心电图异常") || a.contains("心电图早期复极综合征") || a.contains("心电图左心房肥大")
                    || a.contains("心电图左心室肥大") || a.contains("心横位") || a.contains("心室高电压") || a.contains("心脏早搏") || a.contains("逸搏") || a.contains("预激综合征")) {
                hongKang.setXindiantu("2");
            } else {
                hongKang.setXindiantu("1");
            }
        }


        //尿常规
        if (hongKang.getNiaotang() == null) {
            hongKang.setNiaotang("0");
        } else if (hongKang.getNiaotang().contains("3+")) {
            hongKang.setNiaotang("3");
        } else if (hongKang.getNiaotang().contains("2+")) {
            hongKang.setNiaotang("2");
        } else {
            hongKang.setNiaotang("1");
        }

        if (hongKang.getNiaodanbai() == null) {
            hongKang.setNiaodanbai("0");
        } else if (hongKang.getNiaodanbai().contains("3+")) {
            hongKang.setNiaodanbai("3");
        } else if (hongKang.getNiaodanbai().contains("2+")) {
            hongKang.setNiaodanbai("2");
        } else {
            hongKang.setNiaodanbai("1");
        }

        if (hongKang.getNiaoqianxue() == null) {
            hongKang.setNiaoqianxue("0");
        } else if (hongKang.getNiaoqianxue().contains("3+")) {
            hongKang.setNiaoqianxue("3");
        } else if (hongKang.getNiaoqianxue().contains("2+")) {
            hongKang.setNiaoqianxue("2");
        } else {
            hongKang.setNiaoqianxue("1");
        }

        //血常规
        if (hongKang.getBaixibao() != null) {
            Double baixibao = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getBaixibao()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(baixibao, hongKang.getBaixibao_ref());
            int i = baixibao.compareTo(beiShu);
            if (i == 0) {
                hongKang.setBaixibao("1");
            } else {
                if (beiShu >= 1.5) {
                    hongKang.setBaixibao("3");
                } else if (beiShu < 0) {
                    double v = 1 / beiShu;
                    if (v > 1.2) {
                        hongKang.setBaixibao("2");
                    } else {
                        hongKang.setBaixibao("1");
                    }
                } else {
                    hongKang.setBaixibao("1");
                }
            }
        } else {
            hongKang.setBaixibao("0");
        }


        if (hongKang.getLixibao() != null) {
            Double lixibao = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getLixibao()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(lixibao, hongKang.getLixibao_ref());
            int i = lixibao.compareTo(beiShu);
            if (i == 0) {
                hongKang.setLixibao("1");
            } else {
                if (beiShu >= 1.5) {
                    hongKang.setLixibao("3");
                } else if (beiShu < 0) {
                    double v = 1 / beiShu;
                    if (v > 1.2) {
                        hongKang.setLixibao("2");
                    } else {
                        hongKang.setLixibao("1");
                    }
                } else {
                    hongKang.setHongxibao("1");
                }
            }
        } else {
            hongKang.setLixibao("0");
        }

        if (hongKang.getHongxibao() != null) {
            Double hongxibao = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getHongxibao()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(hongxibao, hongKang.getHongxibao_ref());
            int i = hongxibao.compareTo(beiShu);
            if (i == 0) {
                hongKang.setHongxibao("1");
            } else {
                if (beiShu >= 1.5) {
                    hongKang.setHongxibao("3");
                } else if (beiShu < 0) {
                    double v = 1 / beiShu;
                    if (v > 1.2) {
                        hongKang.setHongxibao("2");
                    } else {
                        hongKang.setHongxibao("1");
                    }
                } else {
                    hongKang.setHongxibao("1");
                }
            }
        } else {
            hongKang.setHongxibao("0");
        }


        if (hongKang.getXuexiaoban() != null) {
            Double xuexiaoban = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getXuexiaoban()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(xuexiaoban, hongKang.getXuexiaoban_ref());
            int i = xuexiaoban.compareTo(beiShu);
            if (i == 0) {
                hongKang.setXuexiaoban("1");
            } else {
                if (beiShu >= 1.5) {
                    hongKang.setXuexiaoban("3");
                } else if (beiShu < 0) {
                    double v = 1 / beiShu;
                    if (v > 1.2) {
                        hongKang.setXuexiaoban("2");
                    } else {
                        hongKang.setXuexiaoban("1");
                    }
                } else {
                    hongKang.setXuexiaoban("1");
                }
            }
            if (hongKang.getXuexiaoban() == null) {
                hongKang.setXuexiaoban_ref("0");
            }
        }
        //免疫
        if (hongKang.getEbbingdu() == null) {
            hongKang.setEbbingdu("0");
        } else if (Integer.parseInt(hongKang.getEbbingdu()) > 1) {
            hongKang.setEbbingdu("3");
        } else {
            hongKang.setEbbingdu("1");
        }

        if (hongKang.getLeifengshiyinzi() == null) {
            hongKang.setLeifengshiyinzi("0");
        } else if (Integer.parseInt(hongKang.getLeifengshiyinzi()) == 3) {
            hongKang.setLeifengshiyinzi("3");
        } else {
            hongKang.setLeifengshiyinzi("1");
        }
        if (hongKang.getBingganbingdukangti() == null) {
            hongKang.setBingganbingdukangti("0");
        } else if (Integer.parseInt(hongKang.getBingganbingdukangti()) > 1) {
            hongKang.setBingganbingdukangti("3");
        } else {
            hongKang.setBingganbingdukangti("1");
        }

        if (hongKang.getBingganbingdurna() == null) {
            hongKang.setBingganbingdurna("0");
        } else if (Integer.parseInt(hongKang.getBingganbingdurna()) > 1) {
            hongKang.setBingganbingdurna("3");
        } else {
            hongKang.setBingganbingdurna("1");
        }

        if (hongKang.getMeidu() == null) {
            hongKang.setMeidu("0");
        } else if (Integer.parseInt(hongKang.getMeidu()) > 1) {
            hongKang.setMeidu("3");
        } else {
            hongKang.setMeidu("1");
        }

        if (hongKang.getHiv() == null) {
            hongKang.setHiv("0");
        } else if (Integer.parseInt(hongKang.getHiv()) > 1) {
            hongKang.setHiv("3");
        } else {
            hongKang.setHiv("1");
        }

        if (hongKang.getYiganbiaomiankangyuan() == null) {
            hongKang.setYiganbiaomiankangyuan("0");
        } else if (Integer.parseInt(hongKang.getYiganbiaomiankangyuan()) > 1) {
            hongKang.setYiganbiaomiankangyuan("3");
        } else {
            hongKang.setYiganbiaomiankangyuan("1");
        }

        if (hongKang.getXuechen() == null) {
            hongKang.setXuechen("0");
        } else if (Integer.parseInt(hongKang.getXuechen()) > 1) {
            hongKang.setXuechen("3");
        } else {
            hongKang.setXuechen("1");
        }


        //肝功
        if (hongKang.getXuebiqiu() != null) {
            Double xuebiqiu = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getXuebiqiu()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(xuebiqiu, hongKang.getXuebiqiu_ref());
            System.out.println(beiShu);
            int i = xuebiqiu.compareTo(beiShu);
            if (i == 0) {
                hongKang.setXuebiqiu("1");
            } else {
                if (beiShu > 3) {
                    hongKang.setXuebiqiu("3");
                } else {
                    hongKang.setXuebiqiu("1");
                }
            }
        }
        if (hongKang.getXuebiqiu() == null) {
            hongKang.setXuebiqiu("0");
        }

        if (hongKang.getGt() != null) {
            Double gt = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getGt()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(gt, hongKang.getGt_ref());
            System.out.println(beiShu);
            int i = gt.compareTo(beiShu);
            if (i == 0) {
                hongKang.setGt("1");
            } else {
                if (beiShu > 3) {
                    hongKang.setGt("3");
                } else {
                    hongKang.setGt("1");
                }
            }
        }

        if (hongKang.getGt() == null || hongKang.getGt() == "" || hongKang.getGt().length() == 0) {
            hongKang.setGt("0");
        }

        if (hongKang.getAlt() != null) {
            Double alt = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getAlt()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(alt, hongKang.getAlt_ref());
            System.out.println(beiShu);
            int i = alt.compareTo(beiShu);
            if (i == 0) {
                hongKang.setAlt("1");
            } else {
                if (beiShu > 3) {
                    hongKang.setAlt("3");
                } else {
                    hongKang.setAlt("1");
                }
            }
        }
        if (hongKang.getAlt() == null) {
            hongKang.setAlt("0");
        }

        if (hongKang.getAst() != null) {
            Double ast = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getAst()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(ast, hongKang.getAst_ref());
            System.out.println(beiShu);
            int i = ast.compareTo(beiShu);
            if (i == 0) {
                hongKang.setAst("1");
            } else {
                if (beiShu > 3) {
                    hongKang.setAst("3");
                } else {
                    hongKang.setAst("1");
                }
            }
        }
        if (hongKang.getAst() == null) {
            hongKang.setAst("0");
        }

        if (hongKang.getTba() != null) {
            Double tba = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getTba()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(tba, hongKang.getAst_ref());
            System.out.println(beiShu);
            int i = tba.compareTo(beiShu);
            if (i == 0) {
                hongKang.setTba("1");
            } else {
                if (beiShu > 3) {
                    hongKang.setTba("3");
                } else {
                    hongKang.setTba("1");
                }
            }
        }
        if (hongKang.getTba() == null) {
            hongKang.setTba("0");
        }

        if (hongKang.getTbil() != null) {
            Double tbil = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getTbil()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(tbil, hongKang.getTbil_ref());
            System.out.println(beiShu);
            int i = tbil.compareTo(beiShu);
            if (i == 0) {
                hongKang.setTbil("1");
            } else {
                if (beiShu > 3) {
                    hongKang.setTbil("3");
                } else {
                    hongKang.setTbil("1");
                }
            }
        }

        if (hongKang.getTbil() == null) {
            hongKang.setTbil("0");
        }

        if (hongKang.getBaidanbai() != null) {
            Double baidanbai = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getBaidanbai()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(baidanbai, hongKang.getBaidanbai_ref());
            System.out.println(beiShu);
            int i = baidanbai.compareTo(beiShu);
            if (i == 0) {
                hongKang.setBaidanbai("1");
            } else {
                if (beiShu > 3) {
                    hongKang.setBaidanbai("3");
                } else {
                    hongKang.setBaidanbai("1");
                }
            }
        }
        if (hongKang.getBaidanbai() == null) {
            hongKang.setBaidanbai("0");
        }

        if (hongKang.getAlp() != null) {
            Double alp = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getAlp()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(alp, hongKang.getAlp_ref());
            System.out.println(beiShu);
            int i = alp.compareTo(beiShu);
            if (i == 0) {
                hongKang.setAlp("1");
            } else {
                if (beiShu > 3) {
                    hongKang.setAlp("3");
                } else {
                    hongKang.setAlp("1");
                }
            }
        }

        if (hongKang.getAlp() == null) {
            hongKang.setAlp("0");
        }

        if (hongKang.getTp() != null) {
            Double tp = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getTp()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(tp, hongKang.getTp_ref());
            System.out.println(beiShu);
            int i = tp.compareTo(beiShu);
            if (i == 0) {
                hongKang.setTp("1");
            } else {
                if (beiShu > 3) {
                    hongKang.setTp("3");
                } else {
                    hongKang.setTp("1");
                }
            }
        }

        if (hongKang.getTp() == null) {
            hongKang.setTp("0");
        }

        if (hongKang.getQiudanbai() != null) {
            Double qiudanbai = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getQiudanbai()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(qiudanbai, hongKang.getQiudanbai_ref());
            System.out.println(beiShu);
            int i = qiudanbai.compareTo(beiShu);
            if (i == 0) {
                hongKang.setQiudanbai("1");
            } else {
                if (beiShu > 3) {
                    hongKang.setQiudanbai("3");
                } else {
                    hongKang.setQiudanbai("1");
                }
            }
        }
        if (hongKang.getQiudanbai() == null) {
            hongKang.setQiudanbai("0");
        }

        //肾
        if (hongKang.getJigan() == null) {
            hongKang.setJigan("0");
        } else if (Integer.parseInt(hongKang.getJigan()) == 3) {
            hongKang.setJigan("3");
        } else {
            hongKang.setJigan("1");
        }

        //血脂
        if (hongKang.getTc() != null) {
            Double tc = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getTc()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(Double.parseDouble(hongKang.getTc()), hongKang.getTc_ref());
            int i = tc.compareTo(beiShu);
            if (i == 0) {
                hongKang.setTc("1");
            } else {
                if (beiShu >= 1.5) {
                    hongKang.setTc("3");
                } else if (beiShu >= 1.2 && beiShu <= 1.5) {
                    hongKang.setTc("2");
                } else {
                    hongKang.setTc("1");
                }
            }
        }

        if (hongKang.getTc() == null) {
            hongKang.setTc("0");
        }

        if (hongKang.getTg() != null) {
            Double tg = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getTg()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(Double.parseDouble(hongKang.getTg()), hongKang.getTg_ref());
            int i = tg.compareTo(beiShu);
            if (i == 0) {
                hongKang.setTg("1");
            } else if (beiShu >= 1.5) {
                hongKang.setTg("3");
            } else if (beiShu >= 1.2 && beiShu <= 2) {
                hongKang.setTg("2");
            } else {
                hongKang.setTg("1");
            }
        }

        if (hongKang.getTg() == null) {
            hongKang.setTg("0");
        }
        //血糖

        double xuetang = 0;

        if (hongKang.getKongfuxuetang() == null) {
            hongKang.setKongfuxuetang("0");
        } else {
            if (xuetang >= 7.0) {
                hongKang.setKongfuxuetang("3");
            } else if (Integer.parseInt(hongKang.getKongfuxuetangid()) == 3 && xuetang < 7.0) {
                hongKang.setKongfuxuetang("2");
            } else {
                hongKang.setKongfuxuetang("1");
            }
        }

        if (hongKang.getTanghuaxuehongdanbai() == null) {
            hongKang.setTanghuaxuehongdanbai("0");
        } else if (Integer.parseInt(hongKang.getTanghuaxuehongdanbai()) == 3) {
            hongKang.setTanghuaxuehongdanbai("3");
        } else {
            hongKang.setTanghuaxuehongdanbai("1");
        }

        if (hongKang.getZhongliu() == null) {
            hongKang.setZhongliu("0");
        } else if (a.contains("CA125增高") || a.contains("CA153增高") || a.contains("CA199增高") || a.contains("CA242增高") || a.contains("CA50增高") || a.contains("CA724增高") || a.contains("CYFRA21-1偏高")
                || a.contains("癌胚抗原增高") || a.contains("骨瘤") || a.contains("甲胎蛋白增高") || a.contains("肋骨肿瘤")) {
            hongKang.setZhongliu("3");
        } else {
            hongKang.setZhongliu("1");
        }


//宫颈TCT 问题
        if (hongKang.getGongjingtct() == null) {
            hongKang.setGongjingtct("0");
        } else {
            String status = GetDataGJ_test.getStatus(hongKang.getGongjingtct());
            if (Integer.parseInt(status) == 0) {
                hongKang.setGongjingtct("3");
           } else if (Integer.parseInt(status) == 1) {
                hongKang.setGongjingtct("1");
            }else if(Integer.parseInt(status) == 2){
                hongKang.setGongjingtct("2");
            }
        }
/**
 * 汇总
 */
        if (equals(hongKang.getShousuoya(), "3") || equals(hongKang.getShuzhangya(), "3")) {
            hongKang.setXueya("3");
        } else if (equals(hongKang.getShousuoya(), "2") || equals(hongKang.getShuzhangya(), "2")) {
            hongKang.setXueya("2");
        } else if (equals(hongKang.getShousuoya(), "1") && equals(hongKang.getShuzhangya(), "1")) {
            hongKang.setXueya("1");
        } else if (equals(hongKang.getShousuoya(), "0") && equals(hongKang.getShuzhangya(), "0")) {
            hongKang.setXueya("0");
        }
        if (equals(hongKang.getNiaotang(), "3") || equals(hongKang.getNiaodanbai(), "3") || equals(hongKang.getNiaoqianxue(), "3")) {
            hongKang.setNiaojian("3");
        } else if (equals(hongKang.getNiaotang(), "2") || equals(hongKang.getNiaodanbai(), "2") || equals(hongKang.getNiaoqianxue(), "2")) {
            hongKang.setNiaojian("2");
        } else if (equals(hongKang.getNiaotang(), "1") || equals(hongKang.getNiaodanbai(), "1") || equals(hongKang.getNiaoqianxue(), "1")) {
            hongKang.setNiaojian("1");
        } else if (equals(hongKang.getNiaotang(), "0") && equals(hongKang.getNiaodanbai(), "0") && equals(hongKang.getNiaoqianxue(), "0")) {
            hongKang.setNiaojian("0");
        }

        if (equals(hongKang.getBaixibao(), "3") || equals(hongKang.getXuexiaoban(), "3") || equals(hongKang.getHongxibao(), "3") || equals(hongKang.getLixibao(), "3")) {
            hongKang.setXuechanggui("3");
        } else if (equals(hongKang.getBaixibao(), "2") || equals(hongKang.getXuexiaoban(), "2") || equals(hongKang.getHongxibao(), "2") || equals(hongKang.getLixibao(), "2")) {
            hongKang.setXuechanggui("2");
        } else if (equals(hongKang.getBaixibao(), "1") || equals(hongKang.getXuexiaoban(), "1") || equals(hongKang.getHongxibao(), "1") || equals(hongKang.getLixibao(), "1")) {
            hongKang.setXuechanggui("1");
        } else if (equals(hongKang.getBaixibao(), "0") || equals(hongKang.getXuexiaoban(), "0") || equals(hongKang.getHongxibao(), "0") || equals(hongKang.getLixibao(), "0")) {
            hongKang.setXuechanggui("0");
        }

        if (equals(hongKang.getEbbingdu(), "3") || equals(hongKang.getLeifengshiyinzi(), "3") || equals(hongKang.getBingganbingdurna(), "3")
                || equals(hongKang.getHiv(), "3") || equals(hongKang.getMeidu(), "3")) {
            hongKang.setMianyi("3");
        } else if (equals(hongKang.getXuechen(), "2") || equals(hongKang.getYiganbiaomiankangyuan(), "2")) {
            hongKang.setMianyi("2");
        } else if (equals(hongKang.getEbbingdu(), "1") || equals(hongKang.getLeifengshiyinzi(), "1") || equals(hongKang.getBingganbingdurna(), "1")
                || equals(hongKang.getHiv(), "1") || equals(hongKang.getMeidu(), "1") || equals(hongKang.getXuechen(), "1") || equals(hongKang.getYiganbiaomiankangyuan(), "1")) {
            hongKang.setMianyi("1");
        } else {
            hongKang.setMianyi("0");
        }

        if (equals(hongKang.getXuebiqiu(), "3") || equals(hongKang.getGt(), "3") || equals(hongKang.getAlt(), "3") || equals(hongKang.getAst(), "3") || equals(hongKang.getTba(), "3")
                || equals(hongKang.getTbil(), "3") || equals(hongKang.getTp(), "3") || equals(hongKang.getQiudanbai(), "3")) {
            hongKang.setGangong("3");
        } else if (equals(hongKang.getXuebiqiu(), "0") || equals(hongKang.getGt(), "0") || equals(hongKang.getAlt(), "0") || equals(hongKang.getAst(), "0") || equals(hongKang.getTba(), "0")
                || equals(hongKang.getTbil(), "0") || equals(hongKang.getTp(), "0") || equals(hongKang.getQiudanbai(), "0")) {
            hongKang.setGangong("0");
        } else {
            hongKang.setGangong("1");
        }

        if (equals(hongKang.getTc(), "3") || equals(hongKang.getTg(), "3")) {
            hongKang.setXuezhi("3");
        } else if (equals(hongKang.getTc(), "2") || equals(hongKang.getTg(), "2")) {
            hongKang.setXuezhi("2");
        } else if (equals(hongKang.getTc(), "1") || equals(hongKang.getTg(), "1")) {
            hongKang.setXuezhi("1");
        } else if (equals(hongKang.getTc(), "0") || equals(hongKang.getTg(), "0")) {
            hongKang.setXuezhi("0");
        }


        return hongKang;
    }

    //0未检测 1标体 2转人工 3拒保
    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        return cs1 == null ? cs2 == null : cs1.equals(cs2);
    }

    public void test() throws IOException {
        String idcard = null;
        String pathname = "D:\\workspace\\new\\spring-apps\\datag\\src\\main\\excel\\result3.txt";
        // String finalXlsxPath = "D:\\workspace\\new\\spring-apps\\datag\\src\\main\\excel\\测试数据.xls";
        FileReader reader = null;
        FileOutputStream out = null;
        // FileOutputStream fos = null;
        int i = 1;
        XSSFRow header = null;
        try {
            reader = new FileReader(pathname);
            BufferedReader br = new BufferedReader(reader);
            String line;

            while ((line = br.readLine()) != null) {
                idcard = line;
                System.out.println(idcard);
                HongKang hongKang = getHongkangValue(idcard);
                FileInputStream fs = new FileInputStream("d:/workbook4.xls");
                POIFSFileSystem ps = new POIFSFileSystem(fs);
                HSSFWorkbook wb = new HSSFWorkbook(ps);
                HSSFSheet sheet = wb.getSheetAt(0);
                HSSFRow row = sheet.getRow(0);
                out = new FileOutputStream("d:/workbook4.xls");
                row = sheet.createRow((sheet.getLastRowNum() + 1));
                row.createCell(0).setCellValue(idcard);
                row.createCell(1).setCellValue("0");
                row.createCell(2).setCellValue(hongKang.getBmi());
                row.createCell(3).setCellValue(hongKang.getXueya());
                row.createCell(4).setCellValue(hongKang.getWaike());
                row.createCell(5).setCellValue(hongKang.getErbihou());
                row.createCell(6).setCellValue(hongKang.getYanke());
                row.createCell(7).setCellValue(hongKang.getKouqiang());
                row.createCell(8).setCellValue(hongKang.getJingdongmai());
                row.createCell(9).setCellValue(hongKang.getXinzangcaichao());
                row.createCell(10).setCellValue(hongKang.getFubucaichao());
                row.createCell(11).setCellValue(hongKang.getShen());
                row.createCell(12).setCellValue(hongKang.getShuniaoguan());
                row.createCell(13).setCellValue(hongKang.getPangguang());
                row.createCell(14).setCellValue(hongKang.getZigong());
                row.createCell(15).setCellValue(hongKang.getQianliexian());
                row.createCell(16).setCellValue(hongKang.getJiazuangxian());
                row.createCell(17).setCellValue(hongKang.getRuxian());
                row.createCell(18).setCellValue(hongKang.getXiongct());
                row.createCell(19).setCellValue(hongKang.getXindiantu());
                row.createCell(20).setCellValue(hongKang.getNiaojian());
                row.createCell(21).setCellValue(hongKang.getXuechanggui());
                row.createCell(22).setCellValue(hongKang.getMianyi());
                row.createCell(23).setCellValue(hongKang.getGangong());
                row.createCell(24).setCellValue(hongKang.getKongfuxuetang());
                row.createCell(25).setCellValue(hongKang.getZhongliu());
                row.createCell(26).setCellValue(hongKang.getGongjingtct());
                out.flush();
                wb.write(out);
                System.out.println(row.getLastCellNum() + "测试一下---------------------------" + i);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.close();
    }

    public static void main(String[] args) {
        String REGEX = "[^0-9.]";
        Double tp = Double.parseDouble(Pattern.compile(REGEX).matcher("5.82").replaceAll("").trim());
        double beiShu = getBeiShu.getBeiShu(tp, "1-10");
        boolean c = tp == beiShu;
        System.out.println(c);
        String a = "延期拒";
        boolean s = equals(a, "3");
        System.out.println(s);
    }
}
