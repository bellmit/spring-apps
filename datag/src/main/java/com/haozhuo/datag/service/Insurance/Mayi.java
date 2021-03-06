package com.haozhuo.datag.service.Insurance;

import com.haozhuo.datag.model.report.RepAbnormal;
import com.haozhuo.datag.service.EsService;
import com.haozhuo.datag.service.HbaseService;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class Mayi {
    private static final Logger logger = LoggerFactory.getLogger(HbaseService.class);
    @Autowired
    private HbaseTemplate hbaseTemplate;
    @Autowired
    private EsService esService;
    private final static String HBASENAME = "DATAETL:RPT_IND";

    public RepAbnormal getAbnormalValue(String idcard) {
        String reportId = esService.getrptid(idcard);
        String day = esService.getchkday(idcard);
      //  System.out.println(day);
       // System.out.println(reportId);
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
                //System.out.println(rowName + "," + key + "," + value);
                if ((rownmaes[2].contains("??????") || rownmaes[2].contains("??????")) && rownmaes[3].equals("?????????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setGaoya(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if ((rownmaes[2].contains("??????") || rownmaes[2].contains("??????")) && rownmaes[3].equals("?????????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setDiya(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if ((rownmaes[2].contains("????????????") || rownmaes[2].contains("??????") || rownmaes[2].contains("??????????????????") || rownmaes[2].contains("??????")) && rownmaes[3].contains("????????????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setBmi(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if ((rownmaes[2].contains("??????") || rownmaes[2].contains("??????")) && rownmaes[3].contains("??????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setXuetang(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if (rownmaes[2].contains("??????????????????") && rownmaes[3].contains("??????????????????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setTanghuaxuehongdanbai(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }

                if ((rownmaes[2].contains("?????????") && rownmaes[3].contains("??????"))) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setJiazhuangxianjiejie(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                //????????????
                if ((rownmaes[2].contains("??????") || rownmaes[2].contains("??????") || rownmaes[2].contains("??????") && rownmaes[3].contains("??????")) || (rownmaes[2].contains("??????") && rownmaes[3].contains("??????"))) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setRuxianjiejie(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                //?????????
                if (rownmaes[2].contains("???") && rownmaes[3].contains("??????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setFeijiejie(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                //????????????
                if ((rownmaes[2].contains("???") || rownmaes[2].contains("???")) && rownmaes[3].contains("??????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setGanzangjiejie(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                //????????? ????????????
                if (rownmaes[2].contains("??????") && (rownmaes[3].contains("??????") || rownmaes[3].contains("??????"))) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setWeixirou(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }

                //????????????
                if ((rownmaes[2].contains("??????") || rownmaes[2].contains("??????") || rownmaes[2].contains("??????") || rownmaes[2].contains("???")) && rownmaes[3].contains("??????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setLuanchaolangzhong(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                //??????TCT  ??????
                if ((rownmaes[2].contains("TCT") && rownmaes[3].contains("??????")) || (rownmaes[2].contains("?????????") && rownmaes[3].contains("??????"))) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setGongjingtct(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }

                //??????HPV  ?????????
                if (rownmaes[2].contains("HPV")) {
                    System.out.println(rowName+"----"+key+"----"+value);
                    if (rownmaes[3].contains("16")) {
                        if (key.equals("rs_flag_id")) {
                            System.out.println(rowName+"----"+key+"----"+value);
                            repAbnormal.setGongjinghpv("r");
                            int a = Integer.parseInt(value);
                            if (a > 1) {
                                list.add(value);
                            }
                        }
                    } else if (rownmaes[3].contains("18")) {
                        if (key.equals("rs_flag_id")) {
                            System.out.println(rowName+"----"+key+"----"+value);
                            repAbnormal.setGongjinghpv("r");
                            int a = Integer.parseInt(value);
                            if (a > 1) {
                                list.add(value);
                            }
                        }
                    } else {
                        if (key.equals("rs_flag_id")) {
                            System.out.println(rowName+"----"+key+"----"+value);
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
                } else{
                    if (list.size() != 0 && list1.size() == 0) {
                        repAbnormal.setGongjinghpv("2");
                    } else if (list.size() == 0 && list1.size() != 0) {
                        repAbnormal.setGongjinghpv("3");
                    } else if (list.size() != 0 && list1.size() != 0) {
                        repAbnormal.setGongjinghpv("2,3");
                    } else if (repAbnormal.getGongjinghpv().equals("r")||(repAbnormal.getGongjinghpv()!=null&&Integer.parseInt(repAbnormal.getGongjinghpv())!=0)){
                        repAbnormal.setGongjinghpv("1");
                    }
                }


                if ((rownmaes[2].contains("?????????") || rownmaes[2].contains("ALT")) && (rownmaes[3].contains("???") || rownmaes[3].contains("ALT"))) {
                    if (key.equals("rs_flag_id")) {
                        repAbnormal.setGangongnengalt(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }if (key.equals("rs_val")){
                        System.out.println(rowName+"----"+key+"----"+value);
                    }if (key.equals("text_ref")){
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if ((rownmaes[2].contains("??????") || rownmaes[2].contains("AST")) && (rownmaes[3].contains("???") || rownmaes[3].contains("?????????") || rownmaes[3].contains("AST"))) {
                    if (key.equals("rs_flag_id")) {
                        repAbnormal.setGangongnengast(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }if (key.equals("rs_val")){
                        System.out.println(rowName+"----"+key+"----"+value);
                    }if (key.equals("text_ref")){
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if (rownmaes[2].contains("??????") && rownmaes[3].contains("??????????????????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setYiganbiaomiankangyuan(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if (rownmaes[2].contains("??????") && rownmaes[3].contains("??????????????????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setYiganbiaomiankangti(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if (rownmaes[2].contains("??????") && rownmaes[3].contains("e??????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setYiganekangyuan(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if (rownmaes[2].contains("??????") && rownmaes[3].contains("e??????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setYiganekangti(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if (rownmaes[2].contains("??????") && rownmaes[3].contains("??????????????????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setYiganhexinkangti(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }

                if (rownmaes[2].contains("??????") && rownmaes[3].contains("S1")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setYiganqians1kangyuan(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if (rownmaes[2].contains("??????") && rownmaes[3].contains("???")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setBingganbingdukangti(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if ((rownmaes[2].contains("??????") && rownmaes[2].contains("RNA")) && (rownmaes[3].contains("???") || rownmaes[3].contains("RNA"))) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setBingganbingdurna(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if (rownmaes[2].contains("?????????") && rownmaes[3].contains("??????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setGanyinghuachaosheng(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if (rownmaes[2].contains("??????") && rownmaes[3].contains("??????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setXinzangchaoshengyichang(value);
                        System.out.println(rowName+"----"+key+"----"+value);
                    }
                }
                if (rownmaes[2].contains("?????????") && rownmaes[3].contains("??????")) {
                    if (key.equals("rs_val")) {
                        repAbnormal.setXindiantu(value);
                        System.out.println(rowName+"----"+key+"----"+value);
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

        //????????????
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

        //????????????
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

        //??????
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

        //??????????????????
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


        //???????????????
        if (abnormalValue.getJiazhuangxianjiejie() == null) {
            abnormalValue.setJiazhuangxianjiejie("0");
        } else if (a.contains("???????????????") || a.contains("???????????????") || a.contains("???????????????") || a.contains("????????????????????????")||abnormalValue.getJiazhuangxianjiejie().contains("??????")) {
            abnormalValue.setJiazhuangxianjiejie("2");
        } else {
            abnormalValue.setJiazhuangxianjiejie("1");
        }

        //????????????
        if (abnormalValue.getRuxianjiejie() == null) {
            abnormalValue.setRuxianjiejie("0");
        } else if (a.contains("????????????") || a.contains("????????????") || a.contains("??????????????????") || a.contains("???????????????") || a.contains("??????????????????")) {
            abnormalValue.setRuxianjiejie("2");
        } else {
            abnormalValue.setRuxianjiejie("1");
        }

        //?????????
        if (abnormalValue.getFeijiejie() == null) {
            abnormalValue.setFeijiejie("0");
        } else if (a.contains("?????????") || a.contains("??????????????????") || a.contains("????????????") || a.contains("???????????????") || a.contains("???????????????") || a.contains("????????????") || a.contains("????????????")) {
            abnormalValue.setFeijiejie("2");
        } else {
            abnormalValue.setFeijiejie("1");
        }
        //????????????

        if (abnormalValue.getGanzangjiejie() == null) {
            abnormalValue.setGanzangjiejie("0");
        } else if (a.contains("??????????????????") || a.contains("??????????????????") || a.contains("???????????????") || a.contains("?????????") || a.contains("????????????")) {
            abnormalValue.setGanzangjiejie("2");
        } else {
            abnormalValue.setGanzangjiejie("1");
        }
        //?????????
        if (abnormalValue.getWeixirou() == null) {
            abnormalValue.setWeixirou("0");
        } else if (a.contains("????????????") || a.contains("????????????") || a.contains("?????????")) {
            abnormalValue.setWeixirou("2");
        } else {
            abnormalValue.setWeixirou("1");
        }


        //????????????
        if (abnormalValue.getLuanchaolangzhong() == null) {
            abnormalValue.setLuanchaolangzhong("0");
        } else if (a.contains("?????????????????????")) {
            abnormalValue.setLuanchaolangzhong("2");
        } else if (a.contains("???????????????") || a.contains("????????????") || a.contains("????????????") || a.contains("?????????????????????")) {
            abnormalValue.setLuanchaolangzhong("3");
        } else {
            abnormalValue.setLuanchaolangzhong("1");
        }
        //??????TCT   ?????????
        if (abnormalValue.getGongjingtct() == null) {
            abnormalValue.setGongjingtct("0");
        } else if (a.contains("?????????") || a.contains("???????????????")) {
            abnormalValue.setGongjingtct("2");
        } else if (a.contains("?????????????????????????????????")) {
            abnormalValue.setGongjingtct("3");
        } else {
            abnormalValue.setGongjingtct("1");
        }
        //??????HPV ??????

        //alt
        if (abnormalValue.getGangongnengalt() == null) {
            abnormalValue.setGangongnengalt("0");
        } else if (Integer.parseInt(abnormalValue.getGangongnengalt()) == 1) {
            abnormalValue.setGangongnengalt("1");
        }else{
            abnormalValue.setGangongnengalt("2");
        }

        //ast
        if (abnormalValue.getGangongnengast() == null) {
            abnormalValue.setGangongnengast("0");
        } else if (Integer.parseInt(abnormalValue.getGangongnengast()) == 1) {
            abnormalValue.setGangongnengast("1");
        }else{
            abnormalValue.setGangongnengast("2");
        }

        //??????????????????
        if (abnormalValue.getYiganbiaomiankangyuan() == null) {
            abnormalValue.setYiganbiaomiankangyuan("0");
        } else if (abnormalValue.getYiganbiaomiankangyuan().contains("???")) {
            abnormalValue.setYiganbiaomiankangyuan("1");
        } else if (abnormalValue.getYiganbiaomiankangyuan().contains("???")) {
            abnormalValue.setYiganbiaomiankangyuan("2");
        }

        //??????????????????
        if (abnormalValue.getYiganbiaomiankangti() == null) {
            abnormalValue.setYiganbiaomiankangti("0");
        } else if (abnormalValue.getYiganbiaomiankangti().contains("???")) {
            abnormalValue.setYiganbiaomiankangti("1");
        } else if (abnormalValue.getYiganbiaomiankangti().contains("???")) {
            abnormalValue.setYiganbiaomiankangti("2");
        }

        //??????e??????
        if (abnormalValue.getYiganekangyuan() == null) {
            abnormalValue.setYiganekangyuan("0");
        } else if (abnormalValue.getYiganekangyuan().contains("???")) {
            abnormalValue.setYiganekangyuan("1");
        } else if (abnormalValue.getYiganekangyuan().contains("???")) {
            abnormalValue.setYiganekangyuan("2");
        }

        //??????e??????
        if (abnormalValue.getYiganekangti() == null) {
            abnormalValue.setYiganekangti("0");
        } else if (abnormalValue.getYiganekangti().contains("???")) {
            abnormalValue.setYiganekangti("1");
        } else if (abnormalValue.getYiganekangti().contains("???")) {
            abnormalValue.setYiganekangti("2");
        }

        //??????????????????
        if (abnormalValue.getYiganhexinkangti() == null) {
            abnormalValue.setYiganhexinkangti("0");
        } else if (abnormalValue.getYiganhexinkangti().contains("???")) {
            abnormalValue.setYiganhexinkangti("1");
        } else if (abnormalValue.getYiganhexinkangti().contains("???")) {
            abnormalValue.setYiganhexinkangti("2");
        }

       /* //??????dna  ??????
        if (abnormalValue.getYigandna() == null) {
            abnormalValue.setYigandna("0");
        } else if (abnormalValue.getYigandna().contains("???")) {
            abnormalValue.setYigandna("1");
        } else if (abnormalValue.getYigandna().contains("???")) {
            abnormalValue.setYigandna("2");
        }*/

        //?????????s1
        if (abnormalValue.getYiganqians1kangyuan() == null) {
            abnormalValue.setYiganqians1kangyuan("0");
        } else if (abnormalValue.getYiganqians1kangyuan().contains("???")) {
            abnormalValue.setYiganqians1kangyuan("1");
        } else if (abnormalValue.getYiganqians1kangyuan().contains("???")) {
            abnormalValue.setYiganqians1kangyuan("2");
        }

        //??????????????????
        if (abnormalValue.getBingganbingdukangti() == null) {
            abnormalValue.setBingganbingdukangti("0");
        } else if (abnormalValue.getBingganbingdukangti().contains("???")) {
            abnormalValue.setBingganbingdukangti("1");
        } else if (abnormalValue.getBingganbingdukangti().contains("???")) {
            abnormalValue.setBingganbingdukangti("2");
        }

        //??????RNA
        if (abnormalValue.getBingganbingdurna() == null) {
            abnormalValue.setBingganbingdurna("0");
        } else if (abnormalValue.getBingganbingdurna().contains("???")) {
            abnormalValue.setBingganbingdurna("1");
        } else if (abnormalValue.getBingganbingdurna().contains("???")) {
            abnormalValue.setBingganbingdurna("2");
        }

        //???????????????
        if (abnormalValue.getGanyinghuachaosheng() == null) {
            abnormalValue.setGanyinghuachaosheng("0");
        } else if (a.contains("?????????")) {
            abnormalValue.setGanyinghuachaosheng("2");
        } else {
            abnormalValue.setGanyinghuachaosheng("1");
        }

        //??????????????????
        if (abnormalValue.getXinzangchaoshengyichang() == null) {
            abnormalValue.setXinzangchaoshengyichang("0");
        } else if (abnormalValue.getXinzangchaoshengyichang().contains("?????????????????????")) {
            abnormalValue.setXinzangchaoshengyichang("1");
        } else {
            abnormalValue.setXinzangchaoshengyichang("2");
        }
        //?????????
        //a =a,b,c,d,  []
        if (abnormalValue.getXindiantu() == null) {
            abnormalValue.setXindiantu("0");
        } else if (a.contains("????????????????????????") || a.contains("??????????????????") || a.contains("??????????????????") || a.contains("??????????????????????????????") || a.contains("??????????????????") || a.contains("???????????????????????????") || a.contains("????????????????????????") || a.contains("?????????????????????")
                || a.contains("????????????????????????") || a.contains("????????????????????????") || a.contains("????????????????????????") || a.contains("??????????????????????????????") || a.contains("???????????????") || a.contains("?????????") || a.contains("???????????????") || a.contains("????????????")
                || a.contains("??????") || a.contains("?????????????????????")) {
            abnormalValue.setXindiantu("1");
        } else if (a.contains("R???????????????") || a.contains("???Q-T???????????????") || a.contains("??????????????????") || a.contains("??????????????????") || a.contains("???P-R???????????????") || a.contains("??????????????????") || a.contains("???????????????") || a.contains("??????????????????")
                || a.contains("?????????:??????????????????A??????") || a.contains("?????????ST-T??????") || a.contains("?????????ST?????????") || a.contains("?????????T?????????") || a.contains("??????????????????????????????") || a.contains("???????????????") || a.contains("???????????????")) {
            abnormalValue.setXindiantu("2");
        } else if (a.contains("???????????????") || a.contains("????????????") || a.contains("?????????Q?????????") || a.contains("???????????????????????????????????????") || a.contains("?????????????????????????????????") || a.contains("????????????????????????")
                || a.contains("?????????????????????????????????") || a.contains("????????????") || a.contains("????????????") || a.contains("????????????")) {
            abnormalValue.setXindiantu("3");
        } else {
            abnormalValue.setXindiantu("1");
        }
        return abnormalValue;
    }
}
