package com.haozhuo.datag.service;


import com.haozhuo.datag.model.report.Body;
import com.haozhuo.datag.model.report.HongKang;
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
    private final static String HBASENAME1 = "DATAETL:RPT_LABELS";


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
                if ((rownmaes[2].contains("血压") || rownmaes[2].contains("一般检查")) && rownmaes[3].equals("收缩压")) {
                    if (key.equals("rs_val")) {
                        set.add(value);
                    }
                }
                if (rownmaes[2].contains("血压") && rownmaes[3].equals("")) {
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

    public List<Body> getBody(List list1, String sex) {
        List<Body> list = new ArrayList<>();
        if (sex.equals("男"))
            list1.remove("内科");
        if (list1.size() > 0)
            for (int i = 0; i < list1.size(); i++) {
                Body body = new Body();
                body.setFlag(1);
                body.setItem((String) list1.get(i));
                list.add(body);
            }
        //人体图18项数组
        String[] bodys = {"肺部", "妇科", "肝脏", "化验", "甲状腺", "口腔", "内科", "尿液", "泌尿生殖系统", "肾脏", "外科", "胃肠", "心脏", "眼耳鼻喉", "一般检查", "影像", "肿瘤标志物", "其他"};
        List<String> list2 = Arrays.stream(bodys).collect(Collectors.toList());
        list2.removeAll(list1);
        if (sex.equals("男"))
            list2.remove("妇科");
        // System.out.println("移除异常"+list2.toString());
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

    /**
     * lljj 2019年7月29日16:19:03
     *
     * @param
     * @return
     */
    public RepAbnormal getAbnormalValue(String idcard) {
        String reportId = esService.getrptid(idcard);
        String day = esService.getchkday(idcard);
        System.out.println(day);
        System.out.println(reportId);
        StringBuffer sb = new StringBuffer(day);

        StringBuffer rowkey = sb.append("_" + reportId + "_");
        System.out.println(rowkey);
        String endrowkey = day + "_" + (Integer.parseInt(reportId) + 1) + "_";
        List list = new ArrayList();
        List list1 = new ArrayList();
        RepAbnormal repAbnormal = new RepAbnormal();
        //    SubstringComparator substringComparator = new SubstringComparator(reportId);
        //      RowFilter rowFilter = new RowFilter(CompareFilter.CompareOp.EQUAL, substringComparator);
        Scan scan = new Scan();
        scan.setStartRow(rowkey.toString().getBytes());
        scan.setStopRow(endrowkey.getBytes());
        // scan.setFilter(rowFilter);

        hbaseTemplate.find(HBASENAME, scan, (Result result, int i) -> {
            Cell[] cells = result.rawCells();
            for (Cell cell : cells) {
                String key = new String(CellUtil.cloneQualifier(cell));
                String value = new String(CellUtil.cloneValue(cell));
                String rowName = new String(CellUtil.cloneRow(cell));
                String[] rownmaes = rowName.split("_");
             //   System.out.println(rowName + "," + key + "," + value);
                if ((rownmaes[2].contains("血压") || rownmaes[2].contains("一般")) && rownmaes[3].equals("收缩压")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setGaoya(value);
                    }
                }
                if ((rownmaes[2].contains("血压") || rownmaes[2].contains("一般")) && rownmaes[3].equals("舒张压")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setDiya(value);
                    }
                }
                if ((rownmaes[2].contains("体重指数") || rownmaes[2].contains("一般") || rownmaes[2].contains("人体成分分析") || rownmaes[2].contains("体重")) && rownmaes[3].contains("体重指数")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setBmi(value);
                    }
                }
                if ((rownmaes[2].contains("血糖") || rownmaes[2].contains("生化")) && rownmaes[3].contains("血糖")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setXuetang(value);
                    }
                }
                if (rownmaes[2].contains("糖化血红蛋白") && rownmaes[3].contains("糖化血红蛋白")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setTanghuaxuehongdanbai(value);
                    }
                }

                if ((rownmaes[2].contains("甲状腺") || rownmaes[3].contains("小结")) || (rownmaes[2].contains("外科") && rownmaes[3].contains("甲状腺"))) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setJiazhuangxianjiejie(value);
                    }
                }
                //乳腺结节
                if ((rownmaes[2].contains("乳腺") || rownmaes[2].contains("双乳") || rownmaes[2].contains("乳房") && rownmaes[3].contains("小结")) || (rownmaes[2].contains("外科") && rownmaes[3].contains("乳房"))) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setRuxianjiejie(value);
                    }
                }
                //肺结节
                if (rownmaes[2].contains("肺") && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setFeijiejie(value);
                    }
                }
                //肝脏结节
                if ((rownmaes[2].contains("腹") || rownmaes[2].contains("肝")) && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setGanzangjiejie(value);
                    }
                }
                //胃息肉 直肠息肉
                if (rownmaes[2].contains("外科") && (rownmaes[3].contains("肛门") || rownmaes[3].contains("直肠"))) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setWeixirou(value);
                    }
                }

                //卵巢囊肿
                if ((rownmaes[2].contains("妇科") || rownmaes[2].contains("阴超") || rownmaes[2].contains("盆腔") || rownmaes[2].contains("腹")) && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setLuanchaolangzhong(value);
                    }
                }
                //宫颈TCT  疑议
                if ((rownmaes[2].contains("TCT") && rownmaes[3].contains("小结")) || (rownmaes[2].contains("阴道镜") && rownmaes[3].contains("结果"))) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setGongjingtct(value);
                    }
                }

                //宫颈HPV  存疑议
                if (rownmaes[2].contains("HPV")) {
                    if (rownmaes[3].contains("16")) {
                        if (key.equals("rs_flag_id")) {
                            repAbnormal.setGongjinghpv("r");
                            int a = Integer.parseInt(value);
                            if (a > 1) {
                                list.add(value);
                            }
                        }
                    } else if (rownmaes[3].contains("18")) {
                        if (key.equals("rs_flag_id")) {
                            repAbnormal.setGongjinghpv("r");
                            int a = Integer.parseInt(value);
                            if (a > 1) {
                                list.add(value);
                            }
                        }
                    } else {
                        if (key.equals("rs_flag_id")) {
                            repAbnormal.setGongjinghpv("r");
                            int a = Integer.parseInt(value);
                            if (a > 1) {
                                list1.add(value);
                            }
                        }
                    }
                }
                if (repAbnormal.getGongjinghpv() == null) {
                    repAbnormal.setGongjinghpv("0");
                } else if (list.size() != 0 && list1.size() == 0) {
                    repAbnormal.setGongjinghpv("2");
                } else if (list.size() == 0 && list1.size() != 0) {
                    repAbnormal.setGongjinghpv("3");
                } else if (list.size() != 0 && list1.size() != 0) {
                    repAbnormal.setGongjinghpv("2,3");
                } else {
                    repAbnormal.setGongjinghpv("1");
                }


                if ((rownmaes[2].contains("肝功能") || rownmaes[2].contains("ALT")) && (rownmaes[3].contains("丙") || rownmaes[3].contains("ALT"))) {
                    if (key.equals("rs_flag_id")) {
                        repAbnormal.setGangongnengalt(value);
                    }
                }
                if ((rownmaes[2].contains("肝功") || rownmaes[2].contains("AST")) && (rownmaes[3].contains("草") || rownmaes[3].contains("冬氨酸") || rownmaes[3].contains("AST"))) {
                    if (key.equals("rs_flag_id")) {
                        repAbnormal.setGangongnengast(value);
                    }
                }
                if (rownmaes[2].contains("乙肝") && rownmaes[3].contains("乙肝表面抗原")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setYiganbiaomiankangyuan(value);
                    }
                }
                if (rownmaes[2].contains("乙肝") && rownmaes[3].contains("乙肝表面抗体")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setYiganbiaomiankangti(value);
                    }
                }
                if (rownmaes[2].contains("乙肝") && rownmaes[3].contains("e抗原")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setYiganekangyuan(value);
                    }
                }
                if (rownmaes[2].contains("乙肝") && rownmaes[3].contains("e抗体")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setYiganekangti(value);
                    }
                }
                if (rownmaes[2].contains("乙肝") && rownmaes[3].contains("乙肝核心抗体")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setYiganhexinkangti(value);
                    }
                }

                if (rownmaes[2].contains("乙肝") && rownmaes[3].contains("S1")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setYiganqians1kangyuan(value);
                    }
                }
                if (rownmaes[2].contains("丙肝") && rownmaes[3].contains("丙")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setBingganbingdukangti(value);
                    }
                }
                if ((rownmaes[2].contains("丙肝") && rownmaes[2].contains("RNA")) && (rownmaes[3].contains("丙") || rownmaes[3].contains("RNA"))) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setBingganbingdurna(value);
                    }
                }
                if (rownmaes[2].contains("肝硬化") && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setGanyinghuachaosheng(value);
                    }
                }
                if (rownmaes[2].contains("心脏") && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setXinzangchaoshengyichang(value);
                    }
                }
                if (rownmaes[2].contains("心电图") && rownmaes[3].contains("小结")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setXindiantu(value);
                    }
                }

            }
            return repAbnormal;
        });

        return repAbnormal;
    }

    public RepAbnormal insurance(String idcard) {
        String rptid = esService.getrptid(idcard);

        RepAbnormal abnormalValue = getAbnormalValue(idcard);

        String a = esService.getLabelsByReportId(rptid);
        if (abnormalValue.getBmi() == null) {
            abnormalValue.setBmi("0");
        } else if (Double.parseDouble(abnormalValue.getBmi()) > 0 && Double.parseDouble(abnormalValue.getBmi()) < 17) {
            abnormalValue.setBmi("1");
        } else if (Double.parseDouble(abnormalValue.getBmi()) >= 17 && Double.parseDouble(abnormalValue.getBmi()) < 27) {
            abnormalValue.setBmi("2");
        } else if (Double.parseDouble(abnormalValue.getBmi()) >= 27 && Double.parseDouble(abnormalValue.getBmi()) < 30) {
            abnormalValue.setBmi("3");
        } else if (Double.parseDouble(abnormalValue.getBmi()) >= 30) {
            abnormalValue.setBmi("4");
        }

        //低压指数
        if (abnormalValue.getDiya() == null) {
            abnormalValue.setDiya("0");
        } else if (Integer.parseInt(abnormalValue.getDiya()) > 0 && Integer.parseInt(abnormalValue.getDiya()) < 90) {
            abnormalValue.setDiya("1");
        } else if (Integer.parseInt(abnormalValue.getDiya()) >= 90 && Integer.parseInt(abnormalValue.getDiya()) < 100) {
            abnormalValue.setDiya("2");
        } else if (Integer.parseInt(abnormalValue.getDiya()) >= 100 && Integer.parseInt(abnormalValue.getDiya()) < 110) {
            abnormalValue.setDiya("3");
        } else if (Integer.parseInt(abnormalValue.getDiya()) >= 110) {
            abnormalValue.setDiya("4");
        }

        //高压指数
        if (abnormalValue.getGaoya() == null) {
            abnormalValue.setGaoya("0");
        } else if (Integer.parseInt(abnormalValue.getGaoya()) > 0 && Integer.parseInt(abnormalValue.getGaoya()) < 140) {
            abnormalValue.setGaoya("1");
        } else if (Integer.parseInt(abnormalValue.getGaoya()) >= 140 && Integer.parseInt(abnormalValue.getGaoya()) < 160) {
            abnormalValue.setGaoya("2");
        } else if (Integer.parseInt(abnormalValue.getGaoya()) >= 160 && Integer.parseInt(abnormalValue.getGaoya()) < 180) {
            abnormalValue.setGaoya("3");
        } else if (Integer.parseInt(abnormalValue.getGaoya()) >= 180) {
            abnormalValue.setGaoya("4");
        }

        //血糖
        String REGEX = "[^0-9.]";
        double xuetang = 0;
        if (abnormalValue.getXuetang() != null)
            xuetang = Double.parseDouble(Pattern.compile(REGEX).matcher(abnormalValue.getXuetang()).replaceAll("").trim());
        if (abnormalValue.getXuetang() == null) {
            abnormalValue.setXuetang("0");
        } else if (xuetang > 0 && xuetang < 6.1) {
            abnormalValue.setXuetang("1");
        } else if (xuetang >= 6.1 && xuetang < 7.0) {
            abnormalValue.setXuetang("2");
        } else if (xuetang >= 7.0) {
            abnormalValue.setXuetang("3");
        }

        //糖化血红蛋白
        double tanghua = 0;
        if (abnormalValue.getTanghuaxuehongdanbai() != null) {
            tanghua = Double.parseDouble(Pattern.compile(REGEX).matcher(abnormalValue.getTanghuaxuehongdanbai()).replaceAll("").trim());
        }
        if (abnormalValue.getTanghuaxuehongdanbai() == null) {
            abnormalValue.setTanghuaxuehongdanbai("0");
        } else if (tanghua > 0 && tanghua < 6.1) {
            abnormalValue.setTanghuaxuehongdanbai("1");
        } else if (tanghua >= 7.0) {
            abnormalValue.setTanghuaxuehongdanbai("2");
        }


        //甲状腺结节
        if (abnormalValue.getJiazhuangxianjiejie() == null) {
            abnormalValue.setJiazhuangxianjiejie("0");
        } else if (a.contains("甲状腺结节") || a.contains("甲状腺钙化") || a.contains("甲状腺腺瘤") || a.contains("甲状腺占位性病变")) {
            abnormalValue.setJiazhuangxianjiejie("2");
        } else {
            abnormalValue.setJiazhuangxianjiejie("1");
        }

        //乳腺结节
        if (abnormalValue.getRuxianjiejie() == null) {
            abnormalValue.setRuxianjiejie("0");
        } else if (a.contains("乳腺钙化") || a.contains("乳腺结节") || a.contains("乳腺实性占位") || a.contains("乳腺纤维瘤") || a.contains("乳腺异常回声")) {
            abnormalValue.setRuxianjiejie("2");
        } else {
            abnormalValue.setRuxianjiejie("1");
        }

        //肺结节
        if (abnormalValue.getFeijiejie() == null) {
            abnormalValue.setFeijiejie("0");
        } else if (a.contains("肺大泡") || a.contains("肺间质性改变") || a.contains("肺结节影") || a.contains("肺门影增大") || a.contains("肺内硬结灶") || a.contains("肺肿块影") || a.contains("肺转移瘤")) {
            abnormalValue.setFeijiejie("2");
        } else {
            abnormalValue.setFeijiejie("1");
        }
        //肝脏结节

        if (abnormalValue.getGanzangjiejie() == null) {
            abnormalValue.setGanzangjiejie("0");
        } else if (a.contains("肝弥漫性病变") || a.contains("肝内占位病变") || a.contains("肝异常回声") || a.contains("肝硬化") || a.contains("肝脏增大")) {
            abnormalValue.setGanzangjiejie("2");
        } else {
            abnormalValue.setGanzangjiejie("1");
        }
        //胃息肉
        if (abnormalValue.getWeixirou() == null) {
            abnormalValue.setWeixirou("0");
        } else if (a.contains("直肠息肉") || a.contains("直肠肿物") || a.contains("胃息肉")) {
            abnormalValue.setWeixirou("2");
        } else {
            abnormalValue.setWeixirou("1");
        }


        //卵巢囊肿
        if (abnormalValue.getLuanchaolangzhong() == null) {
            abnormalValue.setLuanchaolangzhong("0");
        } else if (a.contains("多囊卵巢综合征")) {
            abnormalValue.setLuanchaolangzhong("2");
        } else if (a.contains("卵巢畸胎瘤") || a.contains("卵巢囊肿") || a.contains("卵巢增大") || a.contains("卵巢占位性病变")) {
            abnormalValue.setLuanchaolangzhong("3");
        } else {
            abnormalValue.setLuanchaolangzhong("1");
        }
        //宫颈TCT   有问题
        if (abnormalValue.getGongjingtct() == null) {
            abnormalValue.setGongjingtct("0");
        } else if (a.contains("宫颈炎") || a.contains("慢性宫颈炎")) {
            abnormalValue.setGongjingtct("2");
        } else if (a.contains("鳞状上皮炎症反应性改变")) {
            abnormalValue.setGongjingtct("3");
        } else {
            abnormalValue.setGongjingtct("1");
        }
        //宫颈HPV 已查

        //alt
        if (abnormalValue.getGangongnengalt() == null) {
            abnormalValue.setGangongnengalt("0");
        } else if (Integer.parseInt(abnormalValue.getGangongnengalt()) > 1) {
            abnormalValue.setGangongnengalt("1");
        }

        //ast
        if (abnormalValue.getGangongnengast() == null) {
            abnormalValue.setGangongnengast("0");
        } else if (Integer.parseInt(abnormalValue.getGangongnengast()) > 1) {
            abnormalValue.setGangongnengast("1");
        }

        //乙肝表面抗原
        if (abnormalValue.getYiganbiaomiankangyuan() == null) {
            abnormalValue.setYiganbiaomiankangyuan("0");
        } else if (abnormalValue.getYiganbiaomiankangyuan().contains("阴")) {
            abnormalValue.setYiganbiaomiankangyuan("1");
        } else if (abnormalValue.getYiganbiaomiankangyuan().contains("阳")) {
            abnormalValue.setYiganbiaomiankangyuan("2");
        }

        //乙肝表面抗体
        if (abnormalValue.getYiganbiaomiankangti() == null) {
            abnormalValue.setYiganbiaomiankangti("0");
        } else if (abnormalValue.getYiganbiaomiankangti().contains("阴")) {
            abnormalValue.setYiganbiaomiankangti("1");
        } else if (abnormalValue.getYiganbiaomiankangti().contains("阳")) {
            abnormalValue.setYiganbiaomiankangti("2");
        }

        //乙肝e抗原
        if (abnormalValue.getYiganekangyuan() == null) {
            abnormalValue.setYiganekangyuan("0");
        } else if (abnormalValue.getYiganekangyuan().contains("阴")) {
            abnormalValue.setYiganekangyuan("1");
        } else if (abnormalValue.getYiganekangyuan().contains("阳")) {
            abnormalValue.setYiganekangyuan("2");
        }

        //乙肝e抗体
        if (abnormalValue.getYiganekangti() == null) {
            abnormalValue.setYiganekangti("0");
        } else if (abnormalValue.getYiganekangti().contains("阴")) {
            abnormalValue.setYiganekangti("1");
        } else if (abnormalValue.getYiganekangti().contains("阳")) {
            abnormalValue.setYiganekangti("2");
        }

        //乙肝核心抗体
        if (abnormalValue.getYiganhexinkangti() == null) {
            abnormalValue.setYiganhexinkangti("0");
        } else if (abnormalValue.getYiganhexinkangti().contains("阴")) {
            abnormalValue.setYiganhexinkangti("1");
        } else if (abnormalValue.getYiganhexinkangti().contains("阳")) {
            abnormalValue.setYiganhexinkangti("2");
        }

       /* //乙肝dna  问题
        if (abnormalValue.getYigandna() == null) {
            abnormalValue.setYigandna("0");
        } else if (abnormalValue.getYigandna().contains("阴")) {
            abnormalValue.setYigandna("1");
        } else if (abnormalValue.getYigandna().contains("阳")) {
            abnormalValue.setYigandna("2");
        }*/

        //乙肝前s1
        if (abnormalValue.getYiganqians1kangyuan() == null) {
            abnormalValue.setYiganqians1kangyuan("0");
        } else if (abnormalValue.getYiganqians1kangyuan().contains("阴")) {
            abnormalValue.setYiganqians1kangyuan("1");
        } else if (abnormalValue.getYiganqians1kangyuan().contains("阳")) {
            abnormalValue.setYiganqians1kangyuan("2");
        }

        //丙肝病毒抗体
        if (abnormalValue.getBingganbingdukangti() == null) {
            abnormalValue.setBingganbingdukangti("0");
        } else if (abnormalValue.getBingganbingdukangti().contains("阴")) {
            abnormalValue.setBingganbingdukangti("1");
        } else if (abnormalValue.getBingganbingdukangti().contains("阳")) {
            abnormalValue.setBingganbingdukangti("2");
        }

        //丙肝RNA
        if (abnormalValue.getBingganbingdurna() == null) {
            abnormalValue.setBingganbingdurna("0");
        } else if (abnormalValue.getBingganbingdurna().contains("阴")) {
            abnormalValue.setBingganbingdurna("1");
        } else if (abnormalValue.getBingganbingdurna().contains("阳")) {
            abnormalValue.setBingganbingdurna("2");
        }

        //肝硬化超声
        if (abnormalValue.getGanyinghuachaosheng() == null) {
            abnormalValue.setGanyinghuachaosheng("0");
        } else if (a.contains("肝硬化")) {
            abnormalValue.setGanyinghuachaosheng("2");
        } else {
            abnormalValue.setGanyinghuachaosheng("1");
        }

        //心脏超声异常
        if (abnormalValue.getXinzangchaoshengyichang() == null) {
            abnormalValue.setXinzangchaoshengyichang("0");
        } else if (abnormalValue.getXinzangchaoshengyichang().contains("未发现明显异常")) {
            abnormalValue.setXinzangchaoshengyichang("1");
        } else {
            abnormalValue.setXinzangchaoshengyichang("2");
        }
        //心电图
        //a =a,b,c,d,  []
        if (abnormalValue.getXindiantu() == null) {
            abnormalValue.setXindiantu("0");
        } else if (a.contains("窦房结内游走心律") || a.contains("窦性心律不齐") || a.contains("冠状窦性心律") || a.contains("完全性右束支传导阻滞") || a.contains("心电图低电压") || a.contains("心电图冠状窦性心律") || a.contains("心电图逆钟向转位") || a.contains("心电图室性早搏")
                || a.contains("心电图顺钟向转位") || a.contains("心电图心电轴右偏") || a.contains("心电图心电轴左偏") || a.contains("心电图早期复极综合征") || a.contains("心电轴偏移") || a.contains("心横位") || a.contains("心室高电压") || a.contains("心脏早搏")
                || a.contains("逸搏") || a.contains("肢体导联低电压")) {
            abnormalValue.setXindiantu("1");
        } else if (a.contains("R波递增不良") || a.contains("长Q-T间期综合征") || a.contains("窦性心动过缓") || a.contains("窦性心动过速") || a.contains("短P-R间期综合征") || a.contains("房室传导阻滞") || a.contains("交界性心律") || a.contains("束支传导阻滞")
                || a.contains("心电图:预激综合征（A型）") || a.contains("心电图ST-T改变") || a.contains("心电图ST段改变") || a.contains("心电图T波改变") || a.contains("心电图室性早搏二联律") || a.contains("心电图异常") || a.contains("预激综合征")) {
            abnormalValue.setXindiantu("2");
        } else if (a.contains("病窦综合征") || a.contains("房室分离") || a.contains("心电图Q波异常") || a.contains("心电图完全性左束支传导阻滞") || a.contains("心电图左前分支传导阻滞") || a.contains("心电图左心房肥大")
                || a.contains("心电图左心室肥大伴劳损") || a.contains("心房纤颤") || a.contains("心肌梗塞") || a.contains("心肌缺血")) {
            abnormalValue.setXindiantu("3");
        } else {
            abnormalValue.setXindiantu("1");
        }
        return abnormalValue;
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
                RepAbnormal abnormal = insurance(idcard);
                FileInputStream fs = new FileInputStream("d:/workbook1.xls");
                POIFSFileSystem ps = new POIFSFileSystem(fs);
                HSSFWorkbook wb = new HSSFWorkbook(ps);
                HSSFSheet sheet = wb.getSheetAt(0);
                HSSFRow row = sheet.getRow(0);
                out = new FileOutputStream("d:/workbook1.xls");
                row = sheet.createRow((sheet.getLastRowNum() + 1));
                row.createCell(0).setCellValue(idcard);
                row.createCell(1).setCellValue(abnormal.getBmi());
                row.createCell(2).setCellValue(abnormal.getDiya());
                row.createCell(3).setCellValue(abnormal.getGaoya());
                row.createCell(4).setCellValue(abnormal.getXuetang());
                row.createCell(5).setCellValue(abnormal.getTanghuaxuehongdanbai());
                row.createCell(6).setCellValue(abnormal.getJiazhuangxianjiejie());
                row.createCell(7).setCellValue(abnormal.getRuxianjiejie());
                row.createCell(8).setCellValue(abnormal.getFeijiejie());
                row.createCell(9).setCellValue(abnormal.getGanzangjiejie());
                row.createCell(10).setCellValue(abnormal.getWeixirou());
                row.createCell(11).setCellValue(abnormal.getLuanchaolangzhong());
                row.createCell(12).setCellValue(abnormal.getGongjingtct());
                row.createCell(13).setCellValue(abnormal.getGongjinghpv());
                row.createCell(14).setCellValue(abnormal.getGangongnengalt());
                row.createCell(15).setCellValue(abnormal.getGangongnengast());
                row.createCell(16).setCellValue(abnormal.getYiganbiaomiankangyuan());
                row.createCell(17).setCellValue(abnormal.getYiganbiaomiankangti());
                row.createCell(18).setCellValue(abnormal.getYiganekangyuan());
                row.createCell(19).setCellValue(abnormal.getYiganekangti());
                row.createCell(20).setCellValue(abnormal.getYiganhexinkangti());
                row.createCell(21).setCellValue(abnormal.getYiganqians1kangyuan());
                row.createCell(22).setCellValue(abnormal.getBingganbingdukangti());
                row.createCell(23).setCellValue(abnormal.getBingganbingdurna());
                row.createCell(24).setCellValue(abnormal.getGanyinghuachaosheng());
                row.createCell(25).setCellValue(abnormal.getXinzangchaoshengyichang());
                row.createCell(26).setCellValue(abnormal.getXindiantu());
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


