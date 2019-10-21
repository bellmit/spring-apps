package com.haozhuo.datag.service.Insurance;

import com.haozhuo.datag.com.service.Insurance.MatchGan;
import com.haozhuo.datag.com.service.Insurance.getBeiShu;
import com.haozhuo.datag.model.report.InsuranceMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class PushGan {
    @Autowired
    private UserReport userReport;

    public String PushGan(InsuranceMap insuranceMap) {
        String rs = "1";
        String rsv = "";
        String REGEX = "[^0-9.]";
        String str = "肝纤维化PC-III，肝纤维化PC-III、PCIII，" +
                "肝纤维化LN，肝纤维化LN、血清层粘连蛋白测定(LN)、血清层粘连蛋白测定、血清层粘连蛋白（LN）、层粘连蛋白定量、LN(层粘连蛋白)、层粘连蛋白LN、层粘蛋白，" +
                "HIV，艾滋、抗\"艾滋病\"病毒抗体(HIV)、人类免疫缺陷病毒抗体、 HIV、抗人类免疫缺陷病毒抗体、人类免疫缺陷病毒、艾滋病毒抗体、艾滋病联合检查、人类免疫缺陷病毒抗体（Anti-HIV)、抗艾滋病病毒抗体(HIV)、人免疫缺陷病毒抗体检测（HIV）、艾滋病抗体、人免疫缺陷病毒抗体-艾滋病筛查、艾滋病抗体1+2型（HIV）、艾滋病毒抗体HIV ab、艾滋病抗体初筛、艾滋病抗体1 2型（HIV）、艾滋病病毒抗体检测（HIV1/2-Ab）、血清艾滋病毒抗体、HIV抗体艾滋病检测、艾滋病病毒(HIV)抗体、艾滋病检测（HIV抗体）、艾滋病毒抗体（HIV-Ab）、艾滋病联合检测、人免疫缺陷病毒抗体初筛(Anti-HIV)实验、艾滋病抗体（HIV）、抗HIV、人类免疫缺陷病毒抗体（HIV）、抗人类免疫缺陷病毒抗体(HIV-Ab)、HIV抗体筛查、人免疫缺陷病毒抗体(Anti-HIV)试验、HIV抗体(I/II)(初筛试验)、人免疫缺陷病毒抗体测定（Anti-HIV）、人免疫缺陷病毒抗体初筛(Anti-HIV)试验,ELISA法、人免疫缺陷病毒抗体（HIV）、抗人免疫缺陷病毒抗体初筛试验（Anti-HIV）、抗人类免疫功能缺陷病毒（HIV）、人类免疫缺陷病毒、类免疫缺陷病毒抗体测定、人类免疫缺陷病毒（胶体金法）、人类免疫缺陷病毒抗体检测、人类免疫缺陷病毒(ELISA）、抗人体免疫缺陷病毒抗体，" +
                "活化部分凝血活酶时间(APTT) ，活化部分凝血活酶时间(APTT) 、激活凝血活酶时间(APTT)、部分凝血活酶时间、部分凝血活酶原时间，" +
                "血浆凝血酶原时间(PT)，凝血酶原时间(PT)、血浆凝血酶原时间(PT)、凝血酶原时间PT、凝血酶原时间、血浆凝血酶时间(TT)、血浆凝血酶原时间(PT)、凝血酶原时间[PT(INS)]、血浆凝血酶原时间测定 PT、 凝血酶原时间、凝血酶时间TT、凝血酶时间测定、PT比值、PT活动度，" +
                "凝血酶时间，凝血酶凝结时间(TT)  、凝血酶时间(TT)，" +
                "国际标准化比值(PT-INR)，凝血酶原时间国际标准化比值(PT-INR)、国际标准化比值(INR），" +
                "丙肝抗体，丙肝抗体、丙肝抗体(anti-HCV)、丙肝抗体(胶体金法)、丙肝抗体检测（HCV-Ab）、丙肝肝炎病毒抗体（Anti-HCV）、丙肝抗体(HCV-Ab)、丙肝病毒抗体、丙肝病毒抗体检测、丙型肝炎抗体测定HCV、丙型肝炎病毒抗体(胶体金法)、，" +
                "肌酐，肌酐、尿肌酐、尿肌酐检测、肌酐(CRE)、尿肌酐(UC)、尿肌酐(CREA)、尿肌酐（CR）、肌酐（Cr）测定，" +
                "血清尿酸，血清尿酸、尿酸、尿酸（UA）、尿酸[UA]、 血清尿酸[UA]、血清尿酸测定(UA)、血清尿酸 UA、血清尿酸测（UA）、血尿酸、血清尿酸(UA)、尿酸（UA）测定、血尿酸（UA）、血\uE5C7尿酸、血清尿酸、尿酸（UA）、尿酸测定，" +
                "尿素氮，" +
                "24小时尿蛋白，24小时尿蛋白，" +
                "尿蛋白质，尿蛋白质、蛋白质、蛋白质(尿常规)、蛋白、尿蛋白质(PRO)、尿蛋白(PRO)、尿蛋白定量、尿蛋白质（RPO）、尿蛋白定性试验、尿蛋白质(Protein)、蛋白(PRO)，" +
                "血清胱抑素测定，血清胱抑素测定、血清胱抑素（Cys-C)、血清胱抑素C[SYS-C]、血清胱抑素C、血清胱抑素 C、胱抑素C、胱抑素(Cys-C)测定、血清胱抑素C Cys-C、胱抑素C(CysC)、胱抑素（CYSC）、血清胱抑素测定（Cys-C）、血清胱抑素测定(Cys)、胱抑素C检测、胱抑素C(CYS-C)、胱抑素、血清胱抑素测定(Cys-C)、血清胱抑素C测定、血清胱抑素、血清胱抑素 C、血清胱抑素C(CYSC)、胱抑素C、血清胱抑素C测定（Cys-C），" +
                "血清尿素氮，血清尿素氮、血清尿素氮(BU)、尿素氮、尿素氮(UREA)、血\uE5C7尿素氮、尿素氮(BON)、血清尿素氮(UREA)、血清尿素氮测定（Urea）、尿素氮(BUN)、尿素(BUN)，" +
                "血清尿素，血清尿素、血清尿素Urea、尿素、血清尿素测定(Urea)、尿素、尿素（Urea）测定、血清尿素[BUN]、血清尿素 UREA、、尿素（Urea)、尿素[UREA]、，" +
                "血清肌酐 ，血清肌酐、肌酐（Cr）、血清肌测定（CREA)、血清肌酐(S-Cr)、血清肌酐(Cr)、血清肌酐测定（CREA)、血肌酐、血清肌酐[CREA]、肌酐(CER)、血清肌酐测定(Cr)、血清肌酐测定（CARE）、血清肌酐(CREA)、血\uE5C7肌酐、血清肌酐 CREA，" +
                "戊型肝炎病毒IgM抗体，戊型肝炎病毒IgM抗体、戊型肝炎病毒抗体IgM(HEV-IgM)、抗HEV-IgM，" +
                "甲胎蛋白，甲胎蛋白、甲胎蛋白（AFP)、甲胎蛋白(AFP定性)、甲胎蛋白（定性）、甲胎蛋白(AFP)、甲胎蛋白(AFP定量)、甲胎蛋白[AFP]、AFP、甲胎蛋白AFP(C6)、AFP（C6）、AFP(C12)、甲胎蛋白AFP(定性)、甲胎蛋白(蛋白芯片)、甲胎蛋白定量（放免）、甲胎蛋白（酶联法）、甲胎蛋白测定(AFP)定量、甲胎蛋白(AFP) MOM、甲胎蛋白(AFP）定性、甲胎蛋白(TM6)、甲胎蛋白测定(AFP）定性、甲胎蛋白(HD)、甲胎蛋白（芯片）、甲胎蛋白（芯片法）、甲胎蛋白异质体（AFP-L3）、甲胎蛋白AFP(C12)、甲胎蛋白(放射免疫)、甲胎蛋白(AFP）定量、甲胎蛋白AFP.、甲胎蛋白检测、甲胎蛋白测定 (AFP)定量.、甲胎蛋白(AFP酶免)、甲胎蛋白(化学发光)、甲胎蛋白(AFP化学发光)、甲胎蛋白(AFP化学)、甲胎蛋白（发光法）、甲胎蛋白（发光法）、甲胎蛋白(AFP定量)H、甲胎蛋白（AFP）（定性）、甲胎蛋白AFP(生物芯片)、甲胎蛋白(AFP)测定、甲胎蛋白定量(AFP)(发光法)、甲胎蛋白定性(AFP定性)、甲胎蛋白(单项定量）、甲胎蛋白定性（AFP）、甲胎蛋白(华大)、甲胎蛋白(1)、AFP  甲胎蛋白、甲胎蛋白测定(AFP)定量(芯片)、甲胎蛋白.、甲胎蛋白（定量）、甲胎蛋白(TM7)、甲胎蛋白(C12)、甲胎蛋白（T13）、甲胎蛋白(C)、甲胎蛋白AFP（发光）、甲胎蛋白AFP（定量）、甲胎球蛋白AFP、AFP(C7)、AFP(TM)、甲胎球蛋白(AFP).、AFP中位数(C6)、甲胎蛋测定（AFP）定量、甲胎球蛋白AFP(TM7)、AFP（C6-原发性肝癌标志物）、AFP(C8)、甲胎球蛋白AFP(TM6)、.AFP.、AFP(FC12)、AFP(C)、甲胎球蛋白（AFP）定性、甲胎球蛋白AFP(C6)，" +
                "HCV RNA丙型肝炎病毒核酸，丙型肝炎抗体(HCV)、丙型肝炎病毒抗体、丙型肝炎病毒抗体（Anti-HCV）、丙型肝炎抗体定性、丙型肝炎病毒抗体测定（HCV-Ab）、丙型肝炎病毒抗体定性（Anti-HCV）、，" +
                "丁型肝炎IgM，丁型肝炎病毒抗体IgM、丁型肝炎病毒抗体IgM(Anti-HDV)、丁型肝炎病毒表面抗体IgM、丁型肝炎病毒抗体检测IgM、丁型肝炎病毒抗体HDV-IgM、，" +
                "冠心病相关，冠心病相关，" +
                "丁型肝炎病毒抗原（HDVAg），丁型肝炎病毒抗原(HDVAg)、丁型肝炎抗原、丁肝抗体检测（HDV-Ab），" +
                "艾滋病毒抗体(HIV Ab)，艾滋病毒抗体(HIV Ab)，" +
                "HCV-RNA检测，高灵敏度HCV-RNA，" +
                "丁型肝炎病毒表面抗体，丁肝病毒抗体，" +
                "HCV-IgM，HCV-IgM，" +
                "CEA、AFP、CA19-9，CEA、AFP、CA19-9，" +
                "丙肝抗体IgG，丙肝病毒抗体(HCV-IgG)、丙肝抗体HCV-IgG、丙肝病毒抗体IgG、丙肝抗体定性IGG、丙型肝炎抗体IgG、丙型肝炎病毒抗体IgG（HCV-IgG）、HCV-IgG、HCV－Ag，" +
                "丙肝核心抗原，丙肝核心抗原，" +
                "丙肝病毒抗体(HCV-IGM)，丙型肝炎病毒抗体IgM（HCV-IgM）、丙型肝炎病毒抗体HCV-IgM、，" +
                "丁型肝炎病毒，乙型肝炎病毒核心抗体IgM（Anti-HBcIgM）（定性）、乙肝病毒核心抗体HBcAb-IgM、乙型肝炎核心抗体IgM、乙肝核心抗体IGM(HBc-IgM)、乙型肝炎核心抗体IgM(定性)、 乙肝核心抗体IGM、乙肝核心抗体HBC-IGM、乙型肝炎IgM型核心抗体、乙肝病毒核心抗体IgM，" +
                "丁肝抗体检测IgM（HDV-Ab），丁肝抗体检测IgM（HDV-Ab），" +
                "内生肌肝清除率，内生肌酐清除率，" +
                "肾小球滤过率，肾小球滤过率，" +
                "血小板，血小板、血小板总数(PLT)、血小板计数、血小板总数(五分类)(PLT)、血小板计数（PLT)血小板数目(PLT)、血小板计数（PLT）、血小板计数[PLT]、血小板计数.、血小板计数(TLT)、血小板计数（PLC）、血小板[PLT]、血小板(PLT)、血小板.";
        /* InsuranceMap insuranceMap = userReport.UserRep(rptid);*/
        Map<String, String> valueMap = insuranceMap.getValueMap();
        Map<String, String> textRefMap = insuranceMap.getTextRefMap();
        Map<String, String> flagIdMap = insuranceMap.getFlagIdMap();
        String rsval = insuranceMap.getRsval();
        String nonum = MatchGan.nonum(rsval);
        String[] split1 = nonum.split("_");
        String num = MatchGan.num(rsval);
        String[] split2 = num.split("_");
        if (Integer.parseInt(split1[0]) == 0) {
            rs = "0";
            return rs + split1[1];
        } else {
            if (Integer.parseInt(split2[0]) == 0) {
                rs = "0";
                return rs + split2[1];
            }
        }

        for (String a : valueMap.keySet()) {
            String flagid = flagIdMap.get(a);
            String s1 = valueMap.get(a);
            String ref = textRefMap.get(a);
            String[] key = a.split(",");
            boolean a1 = str.contains(key[1]);
            if (a1 == true) {
                int i = Integer.parseInt(flagid);
                if (i > 1) {
                    rs = "0" + a + "," + flagid;
                    break;
                }
            }

            if (a.contains("弹性值kpa") || a.contains("kpa") || a.contains("肝脏硬度中位数")) {
                String trim = Pattern.compile(REGEX).matcher(s1).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    if (v >= 12) {
                        rs = "0";
                        rsv = key[1];
                        break;
                    }
                }
            }

            if (a.contains("血红蛋白") || a.contains("Hb")) {
                String trim = Pattern.compile(REGEX).matcher(s1).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    if (v < 90) {
                        rs = "0";
                        rsv = key[1];
                        break;
                    }
                }
            }

            if (a.contains("肝功") && (key[1].contains("ALT") || key[1].contains("丙") || key[1].contains("丙氨酸"))) {
                System.out.println(a+","+s1+","+ref+","+flagid);
                if (a.contains("/")){

                }else {
                    double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s1).replaceAll("").trim());
                    double beiShu = getBeiShu.getBeiShu(v, ref);
                    getBeiShu.getBeiShu(v, ref);
                    if (beiShu != v) {
                        if (beiShu > 10) {
                            rs = "0";
                            rsv = key[1];
                            break;
                        }
                    }
                }
            }

            if (a.contains("肝功") && (key[1].contains("AST") || key[1].contains("谷草") || key[1].contains("冬氨酸"))) {
                System.out.println(a+","+s1+","+ref+","+flagid);
                if (a.contains("/")){

                }else {
                    double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s1).replaceAll("").trim());
                    double beiShu = getBeiShu.getBeiShu(v, ref);
                    if (beiShu != v) {
                        if (beiShu > 10) {
                            rs = "0";
                            rsv = key[1];
                            break;
                        }
                    }
                }
            }

            if (key[1].contains("C3") || key[1].contains("CA125") || key[1].contains("CA153") || key[1].contains("CA199") || key[1].contains("CA242") || key[1].contains("CA50") || key[1].contains("CA724")
                    || key[1].contains("CYFRA21-1")
                    || key[1].contains("癌胚抗原")
                    || key[1].contains("骨瘤")
                    || key[1].contains("甲胎蛋白")
                    || key[1].contains("肋骨肿瘤") || key[1].contains("AFP") || key[1].contains("CEA")) {
                int i = Integer.parseInt(flagid);
                if (i > 1) {
                    rs = "0";
                    rsv = key[1];
                    break;
                }
            }

            if (key[1].contains("总胆红") || key[1].contains("T-Bil") || key[1].contains("TBil") || key[1].contains("TB-K") || key[1].contains("血清白蛋白") || key[1].contains("ALB")) {
                double v = Double.parseDouble(Pattern.compile(REGEX).matcher(s1).replaceAll("").trim());
                double beiShu = getBeiShu.getBeiShu(v, ref);

                if (beiShu != v) {
                    if (beiShu > 10) {
                        rs = "0";
                        rsv = key[1];
                        break;
                    }
                }
            }
        }




/*        String trim = Pattern.compile(REGEX).matcher(s1).replaceAll("").trim();
        if (trim.equals("")){

        }else {
            double v = Double.parseDouble(trim);
        }*/

        return rs;
    }


}
