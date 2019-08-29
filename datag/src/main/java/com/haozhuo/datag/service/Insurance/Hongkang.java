package com.haozhuo.datag.service.Insurance;

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
                System.out.println(rowName + "," + key + "," + value);
                if ((rownmaes[2].contains("外科") && rownmaes[3].equals("小结"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setWaike(value);
                    }
                }
                if ((rownmaes[2].contains("血压") || rownmaes[2].contains("一般")||rownmaes[2].contains("基础")) && rownmaes[3].equals("收缩压")) {
                    if (key.equals("rs_val")) {
                        hongKang.setShousuoya(value);
                    }
                }

                if ((rownmaes[2].contains("血压") || rownmaes[2].contains("一般")||rownmaes[2].contains("基础")) && rownmaes[3].equals("舒张压")) {
                    if (key.equals("rs_val")) {
                        hongKang.setShuzhangya(value);

                    }
                }



                if ((rownmaes[2].contains("体重指数") || rownmaes[2].contains("一般") || rownmaes[2].contains("人体成分分析") || rownmaes[2].contains("体重")||rownmaes[2].contains("基础")) && rownmaes[3].contains("体重指数")) {
                    if (key.equals("rs_val")) {
                        hongKang.setBmi(value);
                    }
                }
                if ((rownmaes[2].contains("耳鼻喉")) && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        hongKang.setErbihou(value);
                    }
                }

                //待定
                if ((rownmaes[2].contains("眼科")) && rownmaes[3].contains("小结") || (rownmaes[2].contains("视力")) && rownmaes[3].contains("小结") || (rownmaes[2].contains("眼底")) && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        hongKang.setYanke(value);
                    }
                }

                if ((rownmaes[2].contains("口腔")) && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        hongKang.setKouqiang(value);
                    }
                }
                if ((rownmaes[2].contains("颈动脉")) && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        hongKang.setJingdongmai(value);

                    }
                }

                if ((rownmaes[2].contains("心脏")) &&  rownmaes[3].contains("描述")) {
                    if (key.equals("rs_val")) {
                        hongKang.setXinzangcaichao(value);

                    }
                }
                //待定
                if ((rownmaes[2].contains("腹部") && rownmaes[3].contains("小结")) || rownmaes[2].contains("肝胆脾胰肾") && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        hongKang.setFubucaichao(value);
                    }
                }


                if ((rownmaes[2].contains("输尿管")) && rownmaes[3].contains("输尿管")) {
                    if (key.equals("rs_val")) {
                        hongKang.setShuniaoguan(value);

                    }
                }

                if ((rownmaes[2].contains("膀胱")||rownmaes[2].contains("泌尿")) && rownmaes[3].contains("膀胱")) {
                    if (key.equals("rs_val")) {
                        hongKang.setPangguang(value);
                    }
                }

                if ((rownmaes[2].contains("子宫")||rownmaes[2].contains("妇科")||rownmaes[2].contains("阴式")) && rownmaes[3].contains("子宫")) {
                    if (key.equals("rs_val")) {
                        hongKang.setZigong(value);
                    }
                }

                if ((rownmaes[2].contains("甲状腺") || rownmaes[3].contains("小结")) || (rownmaes[2].contains("外科") && rownmaes[3].contains("甲状腺"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setJiazuangxian(value);
                    }
                }
                if ((rownmaes[2].contains("乳腺") || rownmaes[2].contains("双乳") || rownmaes[2].contains("乳房") && rownmaes[3].contains("描述")) || (rownmaes[2].contains("外科") && rownmaes[3].contains("乳房"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setRuxian(value);
                    }
                }
                /**
                 * 胸片
                 */
                if ((((rownmaes[2].contains("胸")&&rownmaes[2].contains("CT"))) && rownmaes[3].contains("小结")) || ((rownmaes[2].contains("肺")&&rownmaes[2].contains("CT")) && rownmaes[3].contains("小结"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setXiongct(value);
                    }
                }
                /**
                 * 心电图
                 */
                if (rownmaes[2].contains("心电图") && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        hongKang.setXindiantu(value);
                    }
                }
                /**
                 * 尿常规
                 */
                if ((rownmaes[2].contains("尿常规") || rownmaes[2].contains("尿检")) && rownmaes[3].contains("糖")) {
                    if (key.equals("rs_val")) {
                        hongKang.setNiaotang(value);
                    }
                }
                if ((rownmaes[2].contains("尿常规") || rownmaes[2].contains("尿检")) && rownmaes[3].contains("蛋白")) {
                    if (key.equals("rs_val")) {
                        hongKang.setNiaodanbai(value);
                    }
                }
                if ((rownmaes[2].contains("尿常规") || rownmaes[2].contains("尿检")) && rownmaes[3].contains("潜血")) {
                    if (key.equals("rs_val")) {
                        hongKang.setNiaoqianxue(value);
                    }
                }
                /**
                 * 血常规
                 */
                if (rownmaes[2].contains("血常规") && rownmaes[3].contains("白细胞计数")) {
                    if (key.equals("rs_val")) {
                        hongKang.setBaixibao(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setBaixibao_ref(value);
                    }

                }
                if (rownmaes[2].contains("血常规") && rownmaes[3].contains("中性粒细胞绝对值")) {
                    if (key.equals("rs_val")) {
                        hongKang.setLixibao(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setLixibao_ref(value);
                    }

                }
                if (rownmaes[2].contains("血常规") && rownmaes[3].contains("红细胞计数")) {
                    if (key.equals("rs_val")) {
                        hongKang.setHongxibao(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setHongxibao_ref(value);
                    }
                }
                if (rownmaes[2].contains("血常规") && rownmaes[3].contains("血小板")) {
                    if (key.equals("rs_val")) {
                        hongKang.setXuexiaoban(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setXuexiaoban_ref(value);
                    }

                }

                /**
                 * 免疫
                 */
                if (rownmaes[3].contains("EB病毒")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setEbbingdu(value);

                    }
                }

                if (rownmaes[2].contains("风湿") && rownmaes[3].contains("风湿")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setLeifengshiyinzi(value);

                    }
                }

                if (rownmaes[2].contains("丙肝") && rownmaes[3].contains("丙")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setBingganbingdukangti(value);
                    }

                }
                if ((rownmaes[2].contains("丙肝") && rownmaes[2].contains("RNA")) && (rownmaes[3].contains("丙") || rownmaes[3].contains("RNA"))) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setBingganbingdurna(value);
                    }

                }
                if (rownmaes[2].contains("梅毒") && rownmaes[3].contains("梅毒")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setMeidu(value);
                    }

                }
                if (rownmaes[2].contains("HIV") && rownmaes[3].contains("HIV")) {
                    if (key.equals("rs_hflag_id")) {
                        hongKang.setHiv(value);
                    }

                }

                if (rownmaes[2].contains("乙肝") && rownmaes[3].contains("乙肝表面抗原")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setYiganbiaomiankangyuan(value);

                    }

                }

                if (rownmaes[2].contains("血沉") && rownmaes[3].contains("血沉")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setXuechen(value);
                    }

                }
                /**
                 * 肝
                 */
                if (rownmaes[2].contains("肝功") && rownmaes[3].contains("/")) {
                    if (key.equals("rs_val")) {
                        hongKang.setXuebiqiu(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setXuebiqiu_ref(value);
                    }


                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("谷氨酰基") || rownmaes[3].contains("GT"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setGt(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setGt_ref(value);
                    }

                }

                if ((rownmaes[2].contains("肝功") || rownmaes[2].contains("ALT")) && (rownmaes[3].contains("丙") || rownmaes[3].contains("ALT"))) {
                    double beiShu;
                    if (key.equals("rs_val")) {
                        hongKang.setAlt(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setAlt_ref(value);
                    }

                }
                if ((rownmaes[2].contains("肝功") || rownmaes[2].contains("AST")) && (rownmaes[3].contains("草") || rownmaes[3].contains("冬氨酸") || rownmaes[3].contains("AST"))) {
                    double beiShu;
                    if (key.equals("rs_val")) {
                        hongKang.setAst(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setAst_ref(value);
                    }

                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("胆汁酸") || rownmaes[3].contains("TBA"))) {
                    double beiShu;
                    if (key.equals("rs_val")) {
                        hongKang.setTba(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setAst_ref(value);
                    }

                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("胆红素") || rownmaes[3].contains("T-Bil"))) {
                    double beiShu;
                    if (key.equals("rs_val")) {
                        hongKang.setTbil(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setTbil_ref(value);
                    }

                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("总蛋白") || rownmaes[3].contains("TP"))) {
                    double beiShu;
                    if (key.equals("rs_val")) {
                        hongKang.setTp(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setTp_ref(value);
                    }

                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("球蛋白"))) {
                    double beiShu;
                    if (key.equals("rs_val")) {
                        hongKang.setQiudanbai(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setQiudanbai_ref(value);
                    }

                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("血清白蛋白") || rownmaes[3].contains("Alb"))) {
                    double beiShu;
                    if (key.equals("rs_val")) {
                        hongKang.setBaidanbai(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setBaidanbai_ref(value);
                    }

                }
                if (rownmaes[2].contains("肝功") && (rownmaes[3].contains("碱性磷酸酶") || rownmaes[3].contains("ALP"))) {
                    double beiShu;
                    if (key.equals("rs_val")) {
                        hongKang.setAlp(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setAlp_ref(value);
                    }


                }
                /**
                 * 肾
                 */
                if (rownmaes[2].contains("肾功") && (rownmaes[3].contains("肌酐"))) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setJigan(value);
                    }

                }
                /**
                 * 血脂
                 */
                if ((rownmaes[2].contains("血脂")||rownmaes[2].contains("血清")) && (rownmaes[3].contains("TC"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setTc(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setTc_ref(value);
                    }
                }
                if ((rownmaes[2].contains("血脂")||rownmaes[2].contains("血清")) && (rownmaes[3].contains("TG"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setTg(value);
                    }
                    if (key.equals("text_ref")) {
                        hongKang.setTg_ref(value);
                    }

                }
                /**
                 * 血糖
                 */
                if ((rownmaes[2].contains("血糖") || rownmaes[2].contains("生化")) && rownmaes[3].contains("血糖")) {
                    if (key.equals("rs_val")) {
                        hongKang.setKongfuxuetang(value);

                    }
                }
                if (rownmaes[2].contains("糖化血红蛋白") && rownmaes[3].contains("糖化血红蛋白")) {
                    if (key.equals("rs_flag_id")) {
                        hongKang.setTanghuaxuehongdanbai(value);
                    }
                }
                if ((rownmaes[2].contains("肿瘤"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setZhongliu(value);
                    }
                }
                if ((rownmaes[2].contains("TCT") && rownmaes[3].contains("小结")) || (rownmaes[2].contains("阴道镜") && rownmaes[3].contains("结果"))) {
                    if (key.equals("rs_val")) {
                        hongKang.setGongjingtct(value);
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
            hongKang.setWaike("未检查");
        } else if (hongKang.getWaike().contains("肢体残疾") || hongKang.getWaike().contains("疤痕") || hongKang.getWaike().contains("压痛")||hongKang.getWaike().contains("疼痛")
                ||hongKang.getWaike().contains("包块")||hongKang.getWaike().contains("淋巴结")) {
            hongKang.setWaike("转人工");
        } else {
            hongKang.setWaike("标体");
        }
        if (hongKang.getShousuoya() == null) {
            hongKang.setShousuoya("未检查");
        } else if (Integer.parseInt(hongKang.getShousuoya()) >= 170) {
            hongKang.setShousuoya("延期拒保");
        } else if (Integer.parseInt(hongKang.getShousuoya()) >= 140 && Integer.parseInt(hongKang.getShousuoya()) < 170) {
            hongKang.setShousuoya("转人工");
        } else {
            hongKang.setShousuoya("标体");
        }

        if (hongKang.getShuzhangya() == null) {
            hongKang.setShuzhangya("未检查");
        } else if (Integer.parseInt(hongKang.getShuzhangya()) >= 105) {
            hongKang.setShuzhangya("延期拒保");
        } else if (Integer.parseInt(hongKang.getShuzhangya()) < 105 && Integer.parseInt(hongKang.getShuzhangya()) >= 90) {
            hongKang.setShuzhangya("转人工");
        } else {
            hongKang.setShuzhangya("标体");
        }

        if (equals(hongKang.getShousuoya(),"延期拒保")||equals(hongKang.getShuzhangya(),"延期拒保")){
            hongKang.setXueya("延期拒保");
        }else if(equals(hongKang.getShousuoya(),"转人工")||equals(hongKang.getShuzhangya(),"转人工")){
            hongKang.setXueya("转人工");
        }else if(equals(hongKang.getShousuoya(),"标体")||equals(hongKang.getShuzhangya(),"标体")){
            hongKang.setXueya("标体");
        }else {
            hongKang.setXueya("未检查");
        }

        if (hongKang.getBmi() == null) {
            hongKang.setBmi("未检查");
        } else if (Double.parseDouble(hongKang.getBmi()) >= 35 || Double.parseDouble(hongKang.getBmi()) <= 15) {
            hongKang.setBmi("延期拒保");
        } else if ((Double.parseDouble(hongKang.getBmi()) > 29 && Double.parseDouble(hongKang.getBmi()) < 35) || (Double.parseDouble(hongKang.getBmi()) > 15 && Double.parseDouble(hongKang.getBmi()) < 18)) {
            hongKang.setBmi("转人工");
        } else {
            hongKang.setBmi("标体");
        }


        if (hongKang.getErbihou() == null) {
            hongKang.setErbihou("未检查");
        } else if (hongKang.getErbihou().contains("听力异常") || hongKang.getErbihou().contains("鼓膜穿孔")) {
            hongKang.setErbihou("转人工");
        } else {
            hongKang.setErbihou("标体");
        }


        //眼科 问题------------
        if (hongKang.getYanke() == null) {
            hongKang.setYanke("未检查");
        } else {
            hongKang.setYanke("标体");
        }
        //口腔
        if (hongKang.getKouqiang() == null) {
            hongKang.setKouqiang("未检查");
        } else if (hongKang.getKouqiang().contains("黏膜白斑") || hongKang.getKouqiang().contains("肿物") || hongKang.getKouqiang().contains("包块") || hongKang.getKouqiang().contains("赘生物")) {
            hongKang.setKouqiang("转人工");
        } else {
            hongKang.setKouqiang("标体");
        }

        //颈椎
        if (hongKang.getJingdongmai() == null) {
            hongKang.setJingdongmai("未检查");
        } else if (hongKang.getJingdongmai().contains("斑块")) {
            hongKang.setJingdongmai("延期拒保");
        } else if (hongKang.getJingdongmai().contains("增厚")) {
            hongKang.setJingdongmai("延期拒保");
        } else {
            hongKang.setJingdongmai("标体");
        }

        //心脏
        if (hongKang.getXinzangcaichao() == null) {
            hongKang.setXinzangcaichao("未检查");
        } else if (hongKang.getXinzangcaichao().contains("扩大") || hongKang.getXinzangcaichao().contains("增厚") || hongKang.getXinzangcaichao().contains("同向运动") || hongKang.getXinzangcaichao().contains("赘生物") || hongKang.getXinzangcaichao().contains("肺动脉高压")) {
            hongKang.setXinzangcaichao("延期拒保");
        } else if (hongKang.getXinzangcaichao().contains("返流") || hongKang.getXinzangcaichao().contains("关闭不全") || hongKang.getXinzangcaichao().contains("脱垂") || hongKang.getXinzangcaichao().contains("假键索")) {
            hongKang.setXinzangcaichao("转人工");
        } else {
            hongKang.setXinzangcaichao("标体");
        }

        //腹部
        if (hongKang.getFubucaichao() == null) {
            hongKang.setFubucaichao("未检查");
        } else if (a.contains("单侧肾缺如") || a.contains("胆囊增大") || a.contains("胆囊占位性病变") || a.contains("腹壁疝") || a.contains("腹股沟淋巴结")
                || a.contains("腹股沟疝") || a.contains("肝弥漫性病变") || a.contains("肝内胆管结石") || a.contains("肝内异常回声")
                || a.contains("海绵肾") || a.contains("脾钙化灶") || a.contains("脾囊肿") || a.contains("脾血管瘤") || a.contains("脾脏未探及") || a.contains("胰腺囊肿") || a.contains("游走肾")
                || a.contains("脂肪瘤") || a.contains("子宫稍大") || a.contains("子宫体积增大") || a.contains("子宫未探及") || a.contains("子宫增大")) {
            hongKang.setFubucaichao("转人工");
        } else if (a.contains("胆管结石") || a.contains("胆管扩张") || a.contains("胆囊萎缩") || a.contains("多囊肝") || a.contains("多囊脾") || a.contains("多囊肾") || a.contains("肝内占位病变")
                || a.contains("肝异常回声") || a.contains("肝脏增大") || a.contains("卵巢畸胎瘤") || a.contains("卵巢占位性病变")) {
            hongKang.setFubucaichao("延期拒保");
        } else {
            hongKang.setFubucaichao("标体");
        }
        //输尿管
        if (hongKang.getShuniaoguan() == null) {
            hongKang.setShuniaoguan("未检查");
        } else if (hongKang.getShuniaoguan().contains("结石") || hongKang.getShuniaoguan().contains("扩张")) {
            hongKang.setShuniaoguan("延期拒保");
        } else if (hongKang.getShuniaoguan().contains("管壁增厚") || hongKang.getShuniaoguan().contains("狭窄")) {
            hongKang.setShuniaoguan("转人工");
        } else {
            hongKang.setShuniaoguan("标体");
        }

        //膀胱
        if (hongKang.getPangguang() == null) {
            hongKang.setPangguang("未检查");
        } else if (hongKang.getPangguang().contains("包块占位") || hongKang.getPangguang().contains("赘生物") || hongKang.getPangguang().contains("漂浮物")) {
            hongKang.setPangguang("延期拒保");
        } else if (hongKang.getPangguang().contains("结石")) {
            hongKang.setPangguang("转人工");
        } else {
            hongKang.setPangguang("标体");
        }
        //子宫 有问题
        if (hongKang.getZigong() == null) {
            hongKang.setZigong("未检查");
        }
        //甲状腺 有问题
        if (hongKang.getJiazuangxian() == null) {
            hongKang.setJiazuangxian("没检查");
        } else {
            hongKang.setJiazuangxian("标体");
        }
        //乳腺
     /*   if (hongKang.getRuxian() != null) {
            //String status = GetDataRX_test.getStatus(hongKang.getRuxian());
            if (Integer.parseInt(status) == 0) {
                hongKang.setRuxian("延期拒保");
            } else if (Integer.parseInt(status) == 1) {
                if (hongKang.getRuxian().contains("结节")) {
                    hongKang.setRuxian("转人工");
                } else {
                    hongKang.setRuxian("标体");
                }
            }
        } else {
            hongKang.setRuxian("标体");
        }
        if (hongKang.getRuxian() == null) {
            hongKang.setRuxian("未检查");
        }*/


        //胸片
        if (hongKang.getXiongct() == null) {
            hongKang.setXiongct("未检查");
        } else if (hongKang.getXiongct().contains("磨玻璃结节") || hongKang.getXiongct().contains("纤维囊性病") || a.contains("肺部罗音") || a.contains("肺动脉增宽") || a.contains("肺间质性改变") || a.contains("肺结核")
                || a.contains("肺结节影") || a.contains("肺门影增大") || a.contains("肺内结节影") || a.contains("肺内硬结灶") || a.contains("肺气肿") || a.contains("肺炎") || a.contains("肺野内密度增高影性质待查")
                || a.contains("肺肿块影") || a.contains("肺转移瘤") || a.contains("慢性支气管炎") || a.contains("纵隔增宽")) {
            hongKang.setXiongct("延期拒保");
        } else if (hongKang.getXiongct().contains("结节") || hongKang.getXiongct().contains("阴影") || hongKang.getXiongct().contains("胸膜增厚") || hongKang.getXiongct().contains("肺大泡") || a.contains("肺纤维灶")) {
            hongKang.setXiongct("转人工");
        } else {
            hongKang.setXiongct("标体");
        }

        //心电图
        if (hongKang.getXindiantu() == null) {
            hongKang.setXindiantu("未检查");
        } else if (a.contains("心电图左心室肥大伴劳损") || a.contains("心房纤颤") || a.contains("心肌梗塞") || a.contains("心肌缺血")) {
            hongKang.setXindiantu("延期拒保");
        } else if (a.contains("病窦综合征") || a.contains("长Q-T间期综合征") || a.contains("窦房结内游走心律") || a.contains("短P-R间期综合征") || a.contains("房室分离") || a.contains("冠状窦性心律") || a.contains("交界性心律") || a.contains("心电图:预激综合征（A型）")
                || a.contains("心电图Q波异常") || a.contains("心电图ST-T改变") || a.contains("心电图ST段改变") || a.contains("心电图T波改变") || a.contains("心电图冠状窦性心律") || a.contains("心电图异常") || a.contains("心电图早期复极综合征") || a.contains("心电图左心房肥大")
                || a.contains("心电图左心室肥大") || a.contains("心横位") || a.contains("心室高电压") || a.contains("心脏早搏") || a.contains("逸搏") || a.contains("预激综合征")) {
            hongKang.setXindiantu("转人工");
        } else {
            hongKang.setXindiantu("标体");
        }

        //尿常规
        if (hongKang.getNiaotang() == null) {
            hongKang.setNiaotang("未检查");
        } else if (hongKang.getNiaotang().contains("3+")) {
            hongKang.setNiaotang("延期拒保");
        } else if (hongKang.getNiaotang().contains("2+")) {
            hongKang.setNiaotang("转人工");
        } else {
            hongKang.setNiaotang("标体");
        }

        if (hongKang.getNiaodanbai() == null) {
            hongKang.setNiaodanbai("未检查");
        } else if (hongKang.getNiaodanbai().contains("3+")) {
            hongKang.setNiaodanbai("延期拒保");
        } else if (hongKang.getNiaodanbai().contains("2+")) {
            hongKang.setNiaodanbai("转人工");
        } else {
            hongKang.setNiaodanbai("标体");
        }

        if (hongKang.getNiaoqianxue() == null) {
            hongKang.setNiaoqianxue("未检查");
        } else if (hongKang.getNiaoqianxue().contains("3+")) {
            hongKang.setNiaoqianxue("延期拒保");
        } else if (hongKang.getNiaoqianxue().contains("2+")) {
            hongKang.setNiaoqianxue("转人工");
        } else {
            hongKang.setNiaoqianxue("标体");
        }

        //血常规
        if (hongKang.getBaixibao() != null) {
            Double baixibao = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getBaixibao()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(baixibao, hongKang.getBaixibao_ref());
            if (beiShu >= 1.5) {
                hongKang.setBaixibao("延期拒保");
            } else {
                hongKang.setBaixibao("标体");
            }
        } else {
            hongKang.setBaixibao("未检查");
        }

        if (hongKang.getBaixibao() == null) {
            hongKang.setLixibao("未检查");
        }

        if (hongKang.getLixibao() != null) {
            Double lixibao = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getLixibao()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(lixibao, hongKang.getLixibao_ref());
            if (beiShu <= 1) {
                hongKang.setLixibao("延期拒保");
            } else {
                hongKang.setLixibao("标体");
            }
        }

        if (hongKang.getHongxibao() != null) {
            Double hongxibao = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getHongxibao()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(hongxibao, hongKang.getHongxibao_ref());
            if (beiShu >= 1.5) {
                hongKang.setHongxibao("延期拒保");
            } else {
                hongKang.setHongxibao("标体");
            }
        }
        if (hongKang.getHongxibao() == null) {
            hongKang.setHongxibao("未检查");
        }

        if (hongKang.getXuexiaoban() != null) {
            Double xuexiaoban = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getXuexiaoban()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(xuexiaoban, hongKang.getXuexiaoban_ref());
            if (beiShu >= 1.5) {
                hongKang.setXuexiaoban_ref("延期拒保");
            } else {
                hongKang.setXuexiaoban_ref("标体");
            }
        }
        if (hongKang.getXuexiaoban() == null) {
            hongKang.setXuexiaoban_ref("未检查");
        }

        //免疫
        if (hongKang.getEbbingdu() == null) {
            hongKang.setEbbingdu("未检查");
        } else if (Integer.parseInt(hongKang.getEbbingdu()) > 1) {
            hongKang.setEbbingdu("延期拒保");
        } else {
            hongKang.setEbbingdu("标体");
        }

        if (hongKang.getLeifengshiyinzi() == null) {
            hongKang.setLeifengshiyinzi("未检查");
        } else if (Integer.parseInt(hongKang.getLeifengshiyinzi()) == 3) {
            hongKang.setLeifengshiyinzi("延期拒保");
        } else {
            hongKang.setLeifengshiyinzi("标体");
        }
        if (hongKang.getBingganbingdukangti() == null) {
            hongKang.setBingganbingdukangti("未检查");
        } else if (Integer.parseInt(hongKang.getBingganbingdukangti()) > 1) {
            hongKang.setBingganbingdukangti("延期拒保");
        } else {
            hongKang.setBingganbingdukangti("标体");
        }

        if (hongKang.getBingganbingdurna() == null) {
            hongKang.setBingganbingdurna("未检查");
        } else if (Integer.parseInt(hongKang.getBingganbingdurna()) > 1) {
            hongKang.setBingganbingdurna("延期拒保");
        } else {
            hongKang.setBingganbingdurna("标体");
        }

        if (hongKang.getMeidu() == null) {
            hongKang.setMeidu("未检查");
        } else if (Integer.parseInt(hongKang.getMeidu()) > 1) {
            hongKang.setMeidu("延期拒保");
        } else {
            hongKang.setMeidu("标体");
        }

        if (hongKang.getHiv() == null) {
            hongKang.setHiv("未检查");
        } else if (Integer.parseInt(hongKang.getHiv()) > 1) {
            hongKang.setHiv("延期拒保");
        } else {
            hongKang.setHiv("标体");
        }

        if (hongKang.getYiganbiaomiankangyuan() == null) {
            hongKang.setYiganbiaomiankangyuan("未检查");
        } else if (Integer.parseInt(hongKang.getYiganbiaomiankangyuan()) > 1) {
            hongKang.setYiganbiaomiankangyuan("延期拒保");
        } else {
            hongKang.setYiganbiaomiankangyuan("标体");
        }

        if (hongKang.getXuechen() == null) {
            hongKang.setXuechen("未检查");
        } else if (Integer.parseInt(hongKang.getXuechen()) > 1) {
            hongKang.setXuechen("延期拒保");
        } else {
            hongKang.setXuechen("标体");
        }


        //肝功
        if (hongKang.getXuebiqiu() != null) {
            Double xuebiqiu = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getXuexiaoban()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(xuebiqiu, hongKang.getXuebiqiu_ref());
            if (beiShu >= 3) {
                hongKang.setXuebiqiu("延期拒保");
            } else {
                hongKang.setXuebiqiu("标体");
            }
        }
        if (hongKang.getXuebiqiu() == null) {
            hongKang.setXuebiqiu("未检查");
        }

        if (hongKang.getGt() != null) {
            Double gt = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getGt()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(gt, hongKang.getGt_ref());
            if (beiShu >= 3) {
                hongKang.setGt("延期拒保");
            } else {
                hongKang.setGt("标体");
            }
        }
        if (hongKang.getGt() == null) {
            hongKang.setGt("未检查");
        }

        if (hongKang.getAlt() != null) {
            Double alt = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getAlt()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(alt, hongKang.getAlt_ref());
            if (beiShu >= 3) {
                hongKang.setAlt("延期拒保");
            } else {
                hongKang.setAlt("标体");
            }
        }
        if (hongKang.getAlt() == null) {
            hongKang.setAlt("未检查");
        }

        if (hongKang.getAst() != null) {
            Double ast = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getAst()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(ast, hongKang.getAst_ref());
            if (beiShu >= 3) {
                hongKang.setAst("延期拒保");
            } else {
                hongKang.setAst("标体");
            }
        }
        if (hongKang.getAst() == null) {
            hongKang.setAst("未检查");
        }

        if (hongKang.getTba() != null) {
            Double tba = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getTba()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(tba, hongKang.getAst_ref());
            if (beiShu >= 3) {
                hongKang.setAst("延期拒保");
            } else {
                hongKang.setAst("标体");
            }
        }
        if (hongKang.getTba() == null) {
            hongKang.setTba("未检查");
        }

        if (hongKang.getTbil() != null) {
            Double tbil = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getTbil()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(tbil, hongKang.getTbil_ref());
            if (beiShu >= 3) {
                hongKang.setTbil("延期拒保");
            } else {
                hongKang.setTbil("标体");
            }
        }

        if (hongKang.getTbil() == null) {
            hongKang.setTbil("未检查");
        }

        if (hongKang.getBaidanbai() != null) {
            Double baidanbai = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getBaidanbai()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(baidanbai, hongKang.getBaidanbai_ref());
            if (beiShu >= 3) {
                hongKang.setBaidanbai("延期拒保");
            } else {
                hongKang.setBaidanbai("标体");
            }
        }
        if (hongKang.getBaidanbai() == null) {
            hongKang.setBaidanbai("未检查");
        }

        if (hongKang.getAlp() != null) {
            Double alp = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getAlp()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(alp, hongKang.getAlp_ref());
            if (beiShu >= 3) {
                hongKang.setAlp("延期拒保");
            } else {
                hongKang.setAlp("标体");
            }
        }

        if (hongKang.getAlp() == null) {
            hongKang.setAlp("未检查");
        }

        if (hongKang.getTp() != null) {
            Double tp = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getTp()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(tp, hongKang.getTp_ref());
            if (beiShu >= 3) {
                hongKang.setTp("延期拒保");
            } else {
                hongKang.setTp("标体");
            }
        }

        if (hongKang.getTp() == null) {
            hongKang.setTp("未检查");
        }

        if (hongKang.getQiudanbai() != null) {
            Double qiudanbai = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getQiudanbai()).replaceAll("").trim());
            beiShu = getBeiShu.getBeiShu(qiudanbai, hongKang.getQiudanbai_ref());
            if (beiShu >= 3) {
                hongKang.setQiudanbai("延期拒保");
            } else {
                hongKang.setQiudanbai("标体");
            }
        }
        if (hongKang.getQiudanbai() == null) {
            hongKang.setQiudanbai("未检查");
        }

        //肾
        if (hongKang.getJigan() == null) {
            hongKang.setJigan("未检查");
        } else if (Integer.parseInt(hongKang.getJigan()) == 3) {
            hongKang.setJigan("延期拒保");
        } else {
            hongKang.setJigan("标体");
        }

        //血脂
        if (hongKang.getTc() != null) {
            beiShu = getBeiShu.getBeiShu(Double.parseDouble(hongKang.getTc()), hongKang.getTc_ref());
            if (beiShu >= 1.5) {
                hongKang.setTc("延期拒保");
            } else if (beiShu >= 1.2 && beiShu <= 1.5) {
                hongKang.setTc("转人工");
            } else {
                hongKang.setTc("标体");
            }
        }

        if (hongKang.getTc() == null) {
            hongKang.setTc("未检查");
        }

        if (hongKang.getTg() != null) {
            beiShu = getBeiShu.getBeiShu(Double.parseDouble(hongKang.getTg()), hongKang.getTg_ref());
            if (beiShu >= 2) {
                hongKang.setTg("延期拒保");
            } else if (beiShu >= 1.2 && beiShu <= 2) {
                hongKang.setTg("转人工");
            } else {
                hongKang.setTg("标体");
            }
        }

        if (hongKang.getTg() == null) {
            hongKang.setTg("未检查");
        }
        //血糖

        double xuetang = 0;
        if (hongKang.getKongfuxuetang() != null)
            xuetang = Double.parseDouble(Pattern.compile(REGEX).matcher(hongKang.getKongfuxuetang()).replaceAll("").trim());
        if (hongKang.getKongfuxuetang() == null) {
            hongKang.setKongfuxuetang("未检查");
        } else if (xuetang >= 7.0) {
            hongKang.setKongfuxuetang("延期拒保");
        } else if (xuetang < 7.0) {
            hongKang.setKongfuxuetang("转人工");
        } else {
            hongKang.setKongfuxuetang("标体");
        }

        if (hongKang.getTanghuaxuehongdanbai() == null) {
            hongKang.setTanghuaxuehongdanbai("未检查");
        } else if (Integer.parseInt(hongKang.getTanghuaxuehongdanbai()) == 3) {
            hongKang.setTanghuaxuehongdanbai("延期拒保");
        } else {
            hongKang.setTanghuaxuehongdanbai("标体");
        }

        if (hongKang.getZhongliu() == null) {
            hongKang.setZhongliu("未检查");
        } else if (a.contains("CA125增高") || a.contains("CA153增高") || a.contains("CA199增高") || a.contains("CA242增高") || a.contains("CA50增高") || a.contains("CA724增高") || a.contains("CYFRA21-1偏高")
                || a.contains("癌胚抗原增高") || a.contains("骨瘤") || a.contains("甲胎蛋白增高") || a.contains("肋骨肿瘤")) {
            hongKang.setZhongliu("延期拒保");
        } else {
            hongKang.setZhongliu("标体");
        }

        if (hongKang.getGongjingtct() != null) {
          //  ArrayBuffer<String> status = GetDataGJ_test.getStatus(hongKang.getGongjingtct());
        }

        if (hongKang.getGongjingtct() == null) {
            hongKang.setGongjingtct("未检查");
        }

/*//眼 甲状腺 子宫 肾 宫颈
        if (hongKang.getShousuoya().contains("拒保") || hongKang.getShuzhangya().contains("拒保")) {
            hongKang.setXueya("拒保");
        } else if (hongKang.getShousuoya().contains("人工") && hongKang.getShuzhangya().contains("人工")) {
            hongKang.setXueya("转人工");
        } else if (hongKang.getShousuoya().contains("未") && hongKang.getShuzhangya().contains("未")) {
            hongKang.setXueya("未检查");
        } else {
            hongKang.setXueya("标体");
        }

        if (hongKang.getNiaotang().contains("拒保") || hongKang.getNiaodanbai().contains("拒保") || hongKang.getNiaoqianxue().contains("拒保")) {
            hongKang.setNiaojian("拒保");
        } else if (hongKang.getNiaotang().contains("人工") && hongKang.getNiaodanbai().contains("人工") && hongKang.getNiaoqianxue().contains("人工")) {
            hongKang.setNiaojian("人工");
        } else if (hongKang.getNiaotang().contains("未") && hongKang.getNiaodanbai().contains("未") && hongKang.getNiaoqianxue().contains("未")) {
            hongKang.setNiaojian("未检查");
        } else {
            hongKang.setNiaojian("标体");
        }

        if(equals(hongKang.getBaixibao(),"延期拒保")||equals( hongKang.getXuexiaoban(),"延期拒保")||equals(hongKang.getHongxibao(),"延期拒保")||equals(hongKang.getLixibao(),"延期拒保")){
            hongKang.setXuechanggui("延期拒保");
        }else if (equals(hongKang.getBaixibao(),"转人工")||equals( hongKang.getXuexiaoban(),"转人工")||equals(hongKang.getHongxibao(),"转人工")||equals(hongKang.getLixibao(),"转人工")) {
            hongKang.setXuechanggui("人工");
        } else if (equals(hongKang.getBaixibao(),"未检查")||equals( hongKang.getXuexiaoban(),"未检查")||equals(hongKang.getHongxibao(),"未检查")||equals(hongKang.getLixibao(),"未检查")) {
            hongKang.setXuechanggui("未检查");
        } else {
            hongKang.setXuechanggui("标体");
        }



        if (hongKang.getEbbingdu().contains("拒保") || hongKang.getLeifengshiyinzi().contains("拒保") || hongKang.getBingganbingdurna().contains("拒保") || hongKang.getHiv().contains("拒保") || hongKang.getMeidu().contains("拒保")) {
            hongKang.setMianyi("拒保");
        } else if (hongKang.getXuechen().contains("人工") || hongKang.getYiganbiaomiankangyuan().contains("人工")) {
            hongKang.setMianyi("人工");
        } else if (hongKang.getEbbingdu().contains("未") || hongKang.getLeifengshiyinzi().contains("未") || hongKang.getBingganbingdurna().contains("未") || hongKang.getHiv().contains("未") || hongKang.getMeidu().contains("未")) {
            hongKang.setMianyi("未检查");
        } else {
            hongKang.setMianyi("标体");
        }

        if (hongKang.getXuebiqiu().contains("拒保") || hongKang.getGt().contains("拒保") || hongKang.getAlt().contains("拒保") || hongKang.getAst().contains("拒保") || hongKang.getTba().contains("拒保") ||
                hongKang.getTbil().contains("拒保") || hongKang.getTp().contains("拒保") || hongKang.getQiudanbai().contains("拒保") ) {
            hongKang.setGangong("拒保");
        } else {
            hongKang.setGangong("标体");
        }

        if (hongKang.getTc().contains("拒保")||hongKang.getTg().contains("拒保")){
            hongKang.setXuezhi("拒保");
        }else if (hongKang.getTc().contains("人工")&&hongKang.getTg().contains("人工")){
            hongKang.setXuezhi("人工");
        }else {
            hongKang.setXuezhi("标体");
        }*/


        return hongKang;
    }

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
                FileInputStream fs = new FileInputStream("d:/workbook2.xls");
                POIFSFileSystem ps = new POIFSFileSystem(fs);
                HSSFWorkbook wb = new HSSFWorkbook(ps);
                HSSFSheet sheet = wb.getSheetAt(0);
                HSSFRow row = sheet.getRow(0);
                out = new FileOutputStream("d:/workbook2.xls");
                row = sheet.createRow((sheet.getLastRowNum() + 1));
                row.createCell(0).setCellValue(idcard);
                row.createCell(1).setCellValue(hongKang.getBmi());
                row.createCell(2).setCellValue(hongKang.getXueya());
                row.createCell(3).setCellValue(hongKang.getWaike());
                row.createCell(4).setCellValue(hongKang.getErbihou());
                row.createCell(5).setCellValue(hongKang.getYanke());
                row.createCell(6).setCellValue(hongKang.getKouqiang());
                row.createCell(7).setCellValue(hongKang.getJingdongmai());
                row.createCell(8).setCellValue(hongKang.getXinzangcaichao());
                row.createCell(9).setCellValue(hongKang.getFubucaichao());
                row.createCell(10).setCellValue(hongKang.getShuniaoguan());
                row.createCell(11).setCellValue(hongKang.getPangguang());
                row.createCell(12).setCellValue(hongKang.getZigong());
                row.createCell(13).setCellValue(hongKang.getQianliexian());
                row.createCell(14).setCellValue(hongKang.getJiazuangxian());
                row.createCell(15).setCellValue(hongKang.getRuxian());
                row.createCell(16).setCellValue(hongKang.getXiongct());
                row.createCell(17).setCellValue(hongKang.getXindiantu());
                row.createCell(18).setCellValue(hongKang.getNiaojian());
                row.createCell(19).setCellValue(hongKang.getXuechanggui());
                row.createCell(20).setCellValue(hongKang.getMianyi());
                row.createCell(21).setCellValue(hongKang.getGangong());
                row.createCell(22).setCellValue(hongKang.getKongfuxuetang());
                row.createCell(23).setCellValue(hongKang.getZhongliu());
                row.createCell(24).setCellValue(hongKang.getGongjingtct());
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


}
