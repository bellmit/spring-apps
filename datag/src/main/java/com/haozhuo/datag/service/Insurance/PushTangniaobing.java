package com.haozhuo.datag.service.Insurance;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.model.report.InsuranceMap;

import java.util.Map;
import java.util.regex.Pattern;

public class PushTangniaobing {
    public String Pushtangniaobing(InsuranceMap insuranceMap) {
        String rsa= "";
        String rs = "1";
        String REGEX = "[^0-9.]";
        Map<String, String> valueMap = insuranceMap.getValueMap();
        Map<String, String> textRefMap = insuranceMap.getTextRefMap();
        Map<String, String> flagIdMap = insuranceMap.getFlagIdMap();

        String str = "HIV，艾滋、抗\"艾滋病\"病毒抗体(HIV)、人类免疫缺陷病毒抗体、 HIV、抗人类免疫缺陷病毒抗体、人类免疫缺陷病毒、艾滋病毒抗体、艾滋病联合检查、人类免疫缺陷病毒抗体（Anti-HIV)、抗艾滋病病毒抗体(HIV)、人免疫缺陷病毒抗体检测（HIV）、艾滋病抗体、人免疫缺陷病毒抗体-艾滋病筛查、艾滋病抗体1+2型（HIV）、艾滋病毒抗体HIV ab、艾滋病抗体初筛、艾滋病抗体1 2型（HIV）、艾滋病病毒抗体检测（HIV1/2-Ab）、血清艾滋病毒抗体、HIV抗体艾滋病检测、艾滋病病毒(HIV)抗体、艾滋病检测（HIV抗体）、艾滋病毒抗体（HIV-Ab）、艾滋病联合检测、人免疫缺陷病毒抗体初筛(Anti-HIV)实验、艾滋病抗体（HIV）、抗HIV、人类免疫缺陷病毒抗体（HIV）、抗人类免疫缺陷病毒抗体(HIV-Ab)、HIV抗体筛查、人免疫缺陷病毒抗体(Anti-HIV)试验、HIV抗体(I/II)(初筛试验)、人免疫缺陷病毒抗体测定（Anti-HIV）、人免疫缺陷病毒抗体初筛(Anti-HIV)试验,ELISA法、人免疫缺陷病毒抗体（HIV）、抗人免疫缺陷病毒抗体初筛试验（Anti-HIV）、抗人类免疫功能缺陷病毒（HIV）、人类免疫缺陷病毒、类免疫缺陷病毒抗体测定、人类免疫缺陷病毒（胶体金法）、人类免疫缺陷病毒抗体检测、人类免疫缺陷病毒(ELISA）、抗人体免疫缺陷病毒抗体，" +
                "肌酸激酶，肌酸激酶、肌酸激酶 CK、肌酸激酶(CK)、血清肌酸激酶测定（CK)、肌酸激酶[CK]、肌酸激酶(CK)、血清肌酸激酶(CK)、肌酸激酶（CK）测定、血清肌酸激酶（CK）测定、磷酸肌酸激酶、肌酸激酶CK、、、、，" +
                "血清乳酸脱氢酶，血清乳酸脱氢酶、乳酸脱氢酶(LD)、乳酸脱氢酶、 血清乳酸脱氢酶、血清乳酸脱氢酶 LDH、乳酸脱氢酶[LDH]、乳酸脱氢酶（LDH）、血清乳酸脱氢酶测定（LDH）、乳酸脱氢酶（LDH）测定、血清乳酸脱氢酶(LDH)、血清乳酸脱氢酶（LDH）测定、乳酸脱氢酶-1、乳酸脱氢酶、血清乳酸脱氢酶测定（LD)、血清乳酸脱氢酶测定(LD)、血\uE5C7乳酸脱氢酶、乳酸脱氢酶-I、CAF-乳酸脱氢酶、血清乳酸脱\uE2C7酶、血清乳酸脱氢测定（LD)、血清乳\uE1CB脱氢酶，" +
                "冠脉缺血计算阀值，冠脉缺血计算阀值，" +
                "血清谷草转氨酶，血清谷草转氨酶、谷草转氨酶[AST] 、 天门冬氨酸氨基转移酶、谷草转氨酶 、血清谷草转氨酶 AST 、天冬氨酸氨基转移酶、血清天冬氨酸氨基转移酶、血清谷草转氨酶、(AST)天门冬氨酸氨基转移酶、天门冬氨酸氨基转移酶（AST）、谷胺酸氨基转移酶(AST)、谷草转氨酶AST、血清天门冬氨酸氨基转移酶测定(AST)、血\uE5C7谷草转氨酶、天门冬氨酸氨转移酶测定、血清谷草转氨酶测定（AST）、血清谷草转氨酶（AST）、谷草转氨酶[AST]、血清天门冬氨酸氨基转移酶(AST)、天门冬氨酸氨基转移酶AST、天门冬氨酸氨基转换酶（AST）、血清天门冬氨酸转移酶测定(AST)、血清谷草转氨酶（AST）测定、血清天门冬氨酸氨基转移酶测定[AST]、天 门冬氨酸转氨酶(AST)、血清天门东氨酸氨基转移酶测定（AST）、血清天门冬氨酸氨基转移氨酶测定(AST)、天门冬氨酸氨基转移酶（AST）测定、AST、血清谷草转氨酶[AST]、血清天冬门氨酸氨基转氨酶测定（AST）、AST线粒体同功酶、天门冬氨酸氨基转移酶线粒体同工酶、天门冬氨酸氨基转移酶①、天门冬氨酸氨基转换酶，" +
                "谷丙转氨酶，谷-丙转氨酶、 血清谷丙转氨酶、丙氨酸氨基转移酶、血清谷丙转氨酶（ALT）、血清丙氨酸氨基转移酶测定、血清谷丙转移酶、血清丙氨酸氨基转移酶（ALT）、谷丙转氨酶[ALT]、天门冬氨酸氨基转移酶测定（ALT)、血清谷丙转氨酶[ALT]、血清谷丙转氨酶 ALT、血清谷丙转氨酶（结合胆红素）、血清谷丙转氨酶（ALT）测定、血清谷丙转氨酶测定（ALT）、谷丙转氨酶酶（ALT）、谷丙转氨酶ALT、血清谷丙丙氨酸基转氨酶测定(ALT)、血\uE5C7谷丙转氨酶、血清丙氨酸氨基转酶测定(ALT)、血清丙氨酸氨基转移氨酶测定(ALT)、丙氨酸氨基转移酶（ALT）测定、血清丙氨酸氨基转氨梅测定（ALT）、血清丙氨酸基转移酶(ALT)、血清丙氨酸氨基转移酶测定（ALT）、血清丙氨酸氨基转氨酶测定(ALT)、血清丙氨酸氨基转移酶测定[ALT]、(ALT)丙氨酸氨基转移酶、，" +
                "肌红蛋白，肌红蛋白、肌红蛋白（Mb）、 肌红蛋白测定 、血清肌红蛋白测定(Mb)、肌红蛋白Mb、肌红蛋白（定量），" +
                "肌钙蛋白，肌钙蛋白、肌钙蛋白 T、肌钙蛋白I、肌钙蛋白测定、超敏肌钙蛋白I、血清肌钙蛋白I测定（Tn1）、血清肌钙蛋白T测定(TnT)、肌钙蛋白Ⅰ测定、肌钙蛋白I测定(cTnI)、肌钙蛋白TNT、肌钙蛋白Ⅰ(TnI)、肌钙蛋白I(cTnI)、肌钙蛋白 I、肌钙蛋白-T定量、TNI，" +
                "Q波 ，Q波 ，" +
                "甲肝抗体检测，甲肝抗体检测、甲肝病毒检测、甲肝抗体（HAV）、甲肝病毒抗体检测，" +
                "甲肝病毒IgG，甲肝病毒IgG、甲肝抗体IgG、甲肝抗体检测IGG、甲肝病毒抗体IgG，" +
                "丙肝抗体，丙肝抗体、丙肝抗体(anti-HCV)、丙肝抗体(胶体金法)、丙肝抗体检测（HCV-Ab）、丙肝肝炎病毒抗体（Anti-HCV）、丙肝抗体(HCV-Ab)、丙肝病毒抗体、丙肝病毒抗体检测、丙型肝炎抗体测定HCV、丙型肝炎病毒抗体(胶体金法)、，" +
                "戊型肝炎病毒IgG抗体，戊型肝炎病毒IgG抗体、戊型肝炎病毒抗体IgG(Anti-HEV)、戊型肝炎病毒抗体IgG(Anti-HEV )、戊型肝炎病毒抗体(HEV-IgG)、戊型肝炎病毒抗体IgG(HEV-IgG)、戊肝抗体IgG、戊肝抗体-IgG、戊肝抗体检测(IgG)、，" +
                "戊型肝炎病毒IgM抗体，戊型肝炎病毒IgM抗体、戊型肝炎病毒抗体IgM(Anti-HEV )、戊型肝炎病毒抗体(HEV-IgM)、戊型肝炎病毒抗体IgM(Anti-HEV)、、戊型肝抗炎病毒抗体IgM（Anti-HEV）、戊肝抗体HEV-IgM、戊肝抗体IgM、戊肝抗体检测IgM（HEV-Ab）、戊肝IgM抗体、戊肝抗体-IgM，" +
                "肌酐，肌酐、尿肌酐、尿肌酐检测、肌酐(CRE)、尿肌酐(UC)、尿肌酐(CREA)、尿肌酐（CR）、肌酐（Cr）测定，" +
                "谷氨酰氨转移酶，谷氨酰转t肽酶、谷氨酰氨基转移酶、血清r-谷氨酰氨基转移酶测定（γ-GT)、(γGT)γ-谷氨酰转肽酶(γGT)、血\uE5C7r-谷氨酰转肽酶、血清r-谷氨酰基转移酶测定（r-GGT)、谷氨酰氨转移酶、谷胺酰转肽酶、谷氨酰转肽酶、血清r-谷氨酰转肽酶、Υ-谷氨酰酶(GGT)、γ-谷氨酰转肽酶、r-谷氨酰基转移酶（GGT）、血清γ－谷氨酰转肽酶测定(γ-GT)、血清r-谷氨酰转肽酶(r-GT)、血清r-谷氨酰转肽酶（r-GT）测定、谷氨酰氨基转肽酶(GGT)、γ－谷氨酰转肽酶(GGT)、γ-谷氨酰基转移酶(GGT)、γ-谷氨酰转移酶、γ-谷氨酰转肽酶同工酶、γ-谷氨酰基转移酶（GGT）、L-谷氨酰转肽酶、血清γ-谷氨酰基转移酶测定（γ-GT）、谷氨酸氨基转移酶、r谷氨酰转肽酶(r-GGT)、血清r-谷氨酰转移酶测定、血清r-谷氨酰基转移酶测定(γ-GT)、γ-谷氨酰基转肽酶、血清r-谷氨酰转肽酶[GGT]、血清r-谷氨酰基转移酶测定（r-GT）、血清r-谷氨酰转移酶测定（r-GT)、血清r-谷氨酰基转肽酶测定（r-GT）、血清γ-谷胺酰基转酶测定（γ-GT)、谷酰氨转肽酶、γ-谷氨酰转移酶GGT、血清γ-谷氨酰基转移酶测定(γ-GGT)、γ-谷氨酰基转移酶(GGT)测定、谷胺酰基移换酶、谷酰转氨酶、γ—谷氨酰氨基转肽酶(GGT)、血清r-谷氨酰基转移酶测定(r_GT)、血\uE5C7r-谷氨酰基转移酶、血清r-谷氨酰转肽酶(r-GGT)、血清r-谷氨酰转肽酶 GGT、血清γ-谷氨酰转肽酶（γ-GT）、r-谷氨酸转肽酶、γ-谷氨酰转移酶（GGT）、γ谷氨酰转肽酶、血清γ-谷氨酰基转移酶测定[γ-GT]、谷氨酰转肽酶(GGT)、r-谷氨酰氨基转移酶(GGT)、血清Y-谷氨酰基转移酶测定（Y-GT)、血清r-谷氨酰转肽酶测定（γ-GT）、血清γ-谷氨酰转移酶测定（GGT）、血清γ-谷氨酰基转移酶测定(GGT)、血清r-谷氨酰转肽酶GGT、血清转肽酶，" +
                "血清总胆红素测定，血清总胆红素测定、血清总胆红素、总胆红素、总胆红素（T-Bil）测定、总胆红素测定(T-Bil)、血清总胆红素测定(TBil)、血清总胆红素（TBIL）、血清总胆红素、血清总胆红素测定（T-Bil）、总胆红素、血清总胆红素测定(T-BBil)、血清总胆红素（T-BIL）测定、血清总胆红素 TBIL、血清总胆红素(S-TBIL)、血清总胆红素测定（D-Bil）、血清总胆红素[TBIL]、总胆红素(TB-K)、总胆红素(T Bli)、血\uE5C7总胆红素、，" +
                "血清直接胆红素测定，直接胆红素、血清直接胆红素测定（D-Bil）综合胆红素、血清直接胆红素测定、血清直接胆红素（D-Bil）、直接胆红素(D-Bil)测定、血\uE5C7直接胆红素、直接胆红素[DBIL]、血清直接胆红素测定(D-BIL)(结合胆红素)、血清直接胆红素(D-BiL)(结合胆红素）、直接胆红素(DBIL)、血清直接胆红素测定(DBIL)、血清直接胆红素测定（结合胆红素）、血清直接胆红素测定（D-Bil）（结合胆红素））、血清直接胆红素测定（D-Bil）结合胆红素、血清直接胆红素测定（D-Bil）（结合胆红）、直接胆红素(DB-K)、血清直接胆红素 DBIL、血清直接胆红素测定(DBil)（结合胆红素）、血清直接胆红素、血清直接胆红素（DBIL)、血清直接胆红素[DBIL]、、，" +
                "间接胆红素，间接胆红素、血清间接胆红素、血清间接胆红素(I-Bil)(计算值)、血清间接胆红素（I-Bil）（计算值）（非结合胆红素）、血清间接胆红素(I-BiL)(非结合胆红素）、血清间接胆红素（计算值）（非结合胆红素）、间接胆红素(IBIL)、血清间接胆红素测定（I-BiL）、非结合胆红素(NCBli)、血清间接胆红素（l-Bil）（计算值）（非结合胆红素）、血清间接胆红素(IBIL)（计算值）、血\uE5C7间接胆红素、间接胆红素（I-Bil）测定、血清间接胆红素测定（I-Bil)(非结合胆红素）、间接胆红素(IBILI)、血清间接胆红素 I-BIL、血清间接胆红素（I-Bil）（计算值）（非结合）、血清间接胆红素（I-Bi1）（计算值）（非结合胆红素）、间接胆红素[IBIL]、血清\uE4BC接胆红素、\uE4BC接胆红素(IBIL)，" +
                "血清碱性磷酸酶，血清碱性磷酸酶、血清碱性磷酸酶（ALP）、碱性磷酸酶、碱性磷酸酶（ALP）、血清碱性磷酸酶 ALP、血清碱性磷酸酶测定、血清碱性磷酸酶测定（ALP)、血清碱性磷酸酶测定(ALP))、血清碱性磷酸酶[ALP]、血清碱性磷酸酶测定[ALP]、血清碱性磷酸酶（ALP）测定、血\uE5C7碱性磷酸酶、碱性磷酸酶、碱性磷酸酶(AKP/ALP)、(ALP)碱性磷酸酶、碱性磷酸酶(ALKP)、血清碱性磷\uE1CB酶，" +
                "戊型肝炎病毒IgM抗体，戊型肝炎病毒IgM抗体、戊型肝炎病毒抗体IgM(HEV-IgM)、抗HEV-IgM，" +
                "甲肝病毒IgM ，甲肝病毒IgM 、甲肝病毒抗体IgM、甲肝病毒IgM 抗体、甲肝IgM抗体测定、甲肝抗体检测(IgM)、甲肝抗体定性IGM、甲肝病毒IgM抗体、甲肝IgM抗体定性，" +
                "弹性值kpa，弹性值kpa、肝脏硬度kpa、肝脏硬度中位数、肝脏硬度值(kPa)，" +
                "肝纤维化PC-III，肝纤维化PC-III、PCIII，" +
                "肝纤维化LN，肝纤维化LN、血清层粘连蛋白测定(LN)、血清层粘连蛋白测定、血清层粘连蛋白（LN）、层粘连蛋白定量、LN(层粘连蛋白)、层粘连蛋白LN、层粘蛋白，" +
                "血清尿素氮，血清尿素氮、血清尿素氮(BU)、尿素氮、尿素氮(UREA)、血\uE5C7尿素氮、尿素氮(BON)、血清尿素氮(UREA)、血清尿素氮测定（Urea）、尿素氮(BUN)、尿素(BUN)，" +
                "血清尿素，血清尿素、血清尿素Urea、尿素、血清尿素测定(Urea)、尿素、尿素（Urea）测定、血清尿素[BUN]、血清尿素 UREA、、尿素（Urea)、尿素[UREA]、，" +
                "血清肌酐 ，血清肌酐、肌酐（Cr）、血清肌测定（CREA)、血清肌酐(S-Cr)、血清肌酐(Cr)、血清肌酐测定（CREA)、血肌酐、血清肌酐[CREA]、肌酐(CER)、血清肌酐测定(Cr)、血清肌酐测定（CARE）、血清肌酐(CREA)、血\uE5C7肌酐、血清肌酐 CREA，" +
                "甲胎蛋白，甲胎蛋白、甲胎蛋白（AFP)、甲胎蛋白(AFP定性)、甲胎蛋白（定性）、甲胎蛋白(AFP)、甲胎蛋白(AFP定量)、甲胎蛋白[AFP]、AFP、甲胎蛋白AFP(C6)、AFP（C6）、AFP(C12)、甲胎蛋白AFP(定性)、甲胎蛋白(蛋白芯片)、甲胎蛋白定量（放免）、甲胎蛋白（酶联法）、甲胎蛋白测定(AFP)定量、甲胎蛋白(AFP) MOM、甲胎蛋白(AFP）定性、甲胎蛋白(TM6)、甲胎蛋白测定(AFP）定性、甲胎蛋白(HD)、甲胎蛋白（芯片）、甲胎蛋白（芯片法）、甲胎蛋白异质体（AFP-L3）、甲胎蛋白AFP(C12)、甲胎蛋白(放射免疫)、甲胎蛋白(AFP）定量、甲胎蛋白AFP.、甲胎蛋白检测、甲胎蛋白测定 (AFP)定量.、甲胎蛋白(AFP酶免)、甲胎蛋白(化学发光)、甲胎蛋白(AFP化学发光)、甲胎蛋白(AFP化学)、甲胎蛋白（发光法）、甲胎蛋白（发光法）、甲胎蛋白(AFP定量)H、甲胎蛋白（AFP）（定性）、甲胎蛋白AFP(生物芯片)、甲胎蛋白(AFP)测定、甲胎蛋白定量(AFP)(发光法)、甲胎蛋白定性(AFP定性)、甲胎蛋白(单项定量）、甲胎蛋白定性（AFP）、甲胎蛋白(华大)、甲胎蛋白(1)、AFP  甲胎蛋白、甲胎蛋白测定(AFP)定量(芯片)、甲胎蛋白.、甲胎蛋白（定量）、甲胎蛋白(TM7)、甲胎蛋白(C12)、甲胎蛋白（T13）、甲胎蛋白(C)、甲胎蛋白AFP（发光）、甲胎蛋白AFP（定量）、甲胎球蛋白AFP、AFP(C7)、AFP(TM)、甲胎球蛋白(AFP).、AFP中位数(C6)、甲胎蛋测定（AFP）定量、甲胎球蛋白AFP(TM7)、AFP（C6-原发性肝癌标志物）、AFP(C8)、甲胎球蛋白AFP(TM6)、.AFP.、AFP(FC12)、AFP(C)、甲胎球蛋白（AFP）定性、甲胎球蛋白AFP(C6)，" +
                "癌胚抗原，癌胚抗原(CEA定性)、癌胚抗原（定性）、癌胚抗原CEA(C6)、CEA(C6)、CEA(C12)、CEA、癌胚抗原(CEA定量)、癌胚抗原(CEA)、癌胚抗原(CEA定性)、癌胚抗原测定（CEA）定量、癌胚抗原(C12)、癌胚抗原(CEA酶免)、（华大）癌胚抗原、癌胚抗原CEA(TM7)、癌胚抗原CEA(TM6)、癌胚抗原定量(CEA)、癌胚抗原（CEA）定量、癌胚抗原CEA定量、CEA  癌胚抗原、癌胚抗原测定(CEA)定量.、癌胚抗原(CEA化学)、癌胚抗原测定(CEA）定性、癌胚抗原(化学发光)、癌、胚抗原(蛋白芯片))、癌胚抗原（酶免）、癌胚抗原CEA定性、癌胚抗原(蛋白芯片)、癌胚抗原（T13）、癌胚抗原(TM6)、癌胚抗原CEA(生物芯片)、癌胚抗原(C)、癌胚抗原定量(CEA)(发光法)、癌胚抗原CEA(TM)、癌胚抗原（芯片）、癌胚抗原定性、癌胚抗原(HD)、癌胚抗原(CEA化学发光)、癌胚抗原(定量)、癌胚抗原CEA（定性）、癌胚抗原 （CEA)、癌胚抗原测定 (CEA)定量、癌胚抗原测定（AFP）定量、癌胚抗原.、癌胚抗原（CEA）定性、癌胚抗原(1)、癌胚抗原定量（放免）、癌胚抗原(TM7)、癌胚抗原(华大)、癌胚抗原(放射免疫)、癌胚抗原(单项定量）、癌胚抗原（CEA）（定性）、癌胚抗原CEA.、癌胚抗原（发光法）、癌胚抗原（芯片法）、癌胚抗原(CEA)测定、癌胚抗原[CEA]、癌胚抗原定性(CEA定性)、CEA（C6-消化道系统广谱筛查）CEA(C7)、CEA(FC12)、CEA(C8)、CEA(C)、癌胚抗，" +
                "总前列腺特异性抗原，总前列腺特异性抗原、前列腺总特异抗原、总前列腺特异性抗原[t-PSA]、总前列腺特异性抗原(TPSA)、总前列腺抗原（TPSA）、总前列腺特异抗原测定、总前列腺特异性抗原(T-PSA)、总前列腺特异性抗原TPSA、总前列腺特异性抗原 （tPSA）、T-PSA(TM)、总前列腺特异性抗原测定(T-PSA)、总前列腺特异性抗(TPSA)、前列腺特异性抗原(T-PSA化学)、总前列腺特异性抗原（TPSA）测定、tPSA(FC12)、前列腺特异性抗原T-PSA(TM)、前列腺特异抗原(T-PSA）、前列腺特异抗原TPSA、总前列腺特异性抗原测定(TPSA)、总前列腺特异性抗原PSA(C6)、tPSA(C)、T-PSA、tPSA(C7)、tPSA、总前列腺特异性抗原T-PSA、tPSA(C8)、总前列腺特异抗原(TPSA)、总前列腺特异性抗原(定量)、总前列腺特异性抗原检测、前列腺特异性抗原.，" +
                "前列腺特异抗原，前列腺特异抗原、PSA、PSA(定量) 、前列腺特异性抗原(定量)、PSA(C6)、前列腺特异性抗原(PSA)、前列腺特异性抗原PSA(C6)、前列腺特异性抗原、前列腺特异性抗原(蛋白芯片)、PSA(C12)、前列腺特异性抗原测定(PSA)、前列腺特异性抗原(PSA)(定量)、前列腺特异性抗原PSA(TM)、前列腺特异性抗原PSA(蛋白芯片)、PSA（C6-前列腺癌特异性标志物）、前列腺特异抗原PSA（C12）、前列腺特异性抗原PSA(TM6)、总前列腺特异性抗原(PSA)定量、PSA(C7)、(重复)前列腺特异性抗原(PSA)、总前列腺特异抗原PSA（C12）、PSA(6)、前列腺特异性抗原PSA(TM7)、前列腺抗原PSA、前列腺特异性抗原定量(PSA)、前列腺特异性抗原PSA(生物芯片)、前列腺特异性抗原测定 (PSA)定量、前列腺特异抗原(TM7)、前列腺特异抗原(TM6)、前列腺抗原、前列腺特异性抗原(华大)、（华大）前列腺特异性抗原、前列腺特异性抗原(定性)、前列腺特异性抗原（定量）（T13）、前列腺特异性抗原(HD)、前列腺特异性抗原(1)、、，" +
                "游离前列腺特异抗原(FPSA)，游离前列腺特异性抗原、游离前列腺特异抗原(FPSA)、游离前列腺特异性抗原F-PSA、FPSA、游离前列腺特异性抗原(F-PSA)、F-PSA(C6)、游离前列腺特异性抗原（芯片）、游离前列腺特异抗原(C12)、游离前列腺特异抗原测定(F-PSA)、游离前列腺特异性抗原测定（FPSA）、F-PSA、游离前列腺特异性抗原测定(F-PSA)、游离前列腺特异性抗原测定（F-PSA）TM、、游离前列腺特异性抗原(FPSA)、游离前列腺特异抗原(F-PSA化学)、游离前列腺特异性抗原定量(FPSA)、游离前列腺特异性抗原F-PSA(TM)、游离前列腺特异性抗原F-PSA.、F-PSA(C12)、总前列腺特异性抗原测定（FPSA）、FPSA(FC12)、游离前列腺特异抗原定量(FPSA)、游离前列腺特异性抗原(F-PSA)测定、游离前列腺特异性抗原 （FPSA）、游离列腺特异抗原、游离前列腺特异性抗原(免疫)、游离前列腺特异性抗原(C12)、游离前列腺特异性抗原(生物芯片)、游离前列腺特异性抗原(定量)、游离前列腺特异性抗原（芯片法）、游离前列腺特异性抗原（T13）、游离前列腺特异性抗原(发光法)、游离前列腺特异性抗原(蛋白芯片)、游离前列腺抗原、前列腺特异性抗原（芯片），" +
                "糖类抗原CA153，糖类抗原CA153、CA153(C6)、糖类抗原CA15-3、CA153（C12）、CA-153(C6)、糖类抗原15-3、 CA-153（C6）、糖类抗原CA-153(C6)、糖链抗原CA15-3(CA15-3)、糖类抗原15-3(CA15-3、糖蛋白抗原153、CA15-3(C8)、CA15-3(TM)、糖类抗原CA153(TM)、CA153（C7）、糖类抗原CA153(C12)、糖类抗原（CA153）、糖类抗原153测定、糖链抗原153、糖链抗原(CA153)、糖类抗原(CA153)、糖原抗原153、CA153、CA153（C6）、糖链抗原CA-153、糖类抗原CA153(C6)、CA153(FC12)、糖类抗原CA153.、癌抗原(CA153)、CA153（C）、糖类抗原-153、糖类抗原153(CA153)、CA153(C)、癌抗原153(CA153)、糖类抗原CA153(定性)、糖链抗原CA153、糖链抗原CA153测定、糖类抗原CA153(蛋白芯片)、糖类抗原(CA-153)、糖类抗原CA153(TM7)、糖类抗原CA-153(生物芯片)、癌抗原153(CA153化学)、肿瘤标志物(CA153)、癌抗原CA153、糖链抗原 153、不用CA153（C12）、(重复)糖链抗原CA153、糖类抗原CA153(TM6)、CA-153(C7)、CA-153（C6-乳腺癌特异性标志物）、糖链抗原CA15-3测定、糖类抗原（CA15-3）、糖类抗原15-3(CA 15-3)、糖类抗原CA15-3(华大)、糖链抗原15-3测定、CA15-3(C6)、糖类抗原CA15-3（芯片）、（华大）糖类抗原CA15-3、糖链抗原CA15-3测定(CA15-3)、癌抗原15-3（芯片法）、糖基类抗CA15-3、糖链抗原CA15-3测定.、糖类抗原CA15-3(TM7)、糖类抗原CA15-3测定、糖类抗原CA15-3(1)、糖类抗原CA15-3(C12)、糖类抗原15-3测定(CA15-3)、糖类抗原15-3(CA15-3)、糖类抗原CA15-3(TM)、糖类抗原15-3 （CA15-3）、糖类抗原15-3(T13)、糖类抗原CA15-3(TM6)、糖类抗原CA15-3(蛋白芯片)、癌抗原CA15-3、CA15-3(C12)、糖类抗原15-3(HD)、糖基类抗原15-3、CA15-3(C)、糖类抗原1-53，" +
                "糖类抗原(CA125)     ，糖类抗原(CA125)、CA125 、糖类抗原125、CA125(C12)、CA125(C6)糖类抗原CA125(C6)、 糖类抗原125测定、糖类抗原125(HD)、糖类抗原CA125(TM)、糖类抗原CA125.、肿瘤标志物(CA125)、糖蛋白抗原125测定(CA125)、癌抗原125、癌抗原(CA125)、CA125(FC12)、CA125、糖链抗原 125、糖链抗原CA125测定.、癌抗原125(CA125化学)、CA-125(C7)、糖类抗原125（T13）、糖类抗原CA-125(1)、癌抗原125(CA125)、糖类抗原CA125(CA125)、糖类抗原CA125(TM6)、糖链抗原(CA125)、糖链抗原125、糖类抗原CA125(TM7)、糖类抗原CA125(C12)、CA125（卵巢癌标志物）、糖蛋白抗原CA125测定、糖类抗原125(CA125)、CA125(C8)、糖类抗原125(CA-125)、糖类抗原125 (CA125)、糖类抗原CA125(生物芯片)、糖链抗原125测定、糖类抗原CA125(蛋白芯片)、CA125（C6-卵巢癌特异性标志物）、糖类抗原125(CA 125)、糖链抗原CA125测定（CA125）、糖类抗原CA-125(华大)、糖链抗原CA125（CA125)、CA125(C)、肿瘤相关抗原CA125、糖类抗原(CA-125)、（华大）糖类抗原CA-125、糖类抗原CA-125(蛋白芯片)、癌抗原12-5、糖类抗原12-5、CA12-5、，" +
                "糖类抗原19-9 ，糖类抗原19-9、CA199（C12）、CA19-9（C6）、CA199(C12)、糖类抗原CA19-9(C6)、糖类抗原CA19-9、糖类抗原CA19-9)、糖链抗原199、糖类抗原（CA19-9）、CA19-9(TM)、糖链抗原 199、癌抗原199(CA199化学)、糖链抗原CA19-9测定.、糖蛋白抗原19-9、糖类抗原19-9（定量）、（华大）糖类抗原CA19-9、糖l类抗原CA19-9、糖类抗原CA199(蛋白芯片)、糖类抗原19-癌抗原(CA199)、糖蛋白抗原19-9测定(CA199)、肿瘤标志物(CA199)、糖类抗原CA199(C12)、糖类抗原CA199.、糖类抗原(CA199)、糖类抗原CA199(C6)、癌抗原199(CA199)、糖类抗原199(CA199)、CA199(C7)、糖链抗原CA199测定、糖类抗原CA199测定、CA199(TM)、糖类抗原199测定、CA199(C)、糖类抗原CA199(TM)、CA199(FC12)、CA19-9(C7)、CA-199(C7)、糖类抗原-199、CA-199(C6)、(重复)糖链抗原CA19-9、糖类抗原-19-9(CA-19-9)、癌抗原19-9、糖链抗原CA19-9测定(CA19-9)、糖类抗原CA19-9(C12)、糖类抗原CA19-9(蛋白芯片)、不用CA19-9(C6)、糖类抗原19-9测定(CA19-9)、CA19-9(C)、(重复)糖类抗原19-9、CA19-9（C6-胰腺癌特异性标志物）、糖类抗原-19-9(CA19-9)、糖链抗原19-9、糖类抗原CA19-9（芯片）、糖类抗原CA19-9(TM6)、糖类抗原19-9（CA19-9）、糖类抗原CA19-9(CA19-9)、糖链抗原19-9（芯片法）、糖类抗原CA19-9(1)、糖类抗原CA19-9(华大)、糖\uE0C0抗原CA19-9、(不用)糖类抗原19-9、CA19-9(C8)、糖链抗原19-9测定、糖类抗原19-9(HD)、糖类抗原19-9（T13）、糖类抗原CA19-9测定(CA19-9)、糖类抗原19-9(CA 19-9)、糖类抗原CA19-9(TM)、糖\uE0C0抗原19-9、糖链抗原CA19-9（CA19-9)、糖类抗原CA19-9 （CA19-9）、糖类抗原19-9 (CA19-9)、糖类抗原CA19-9(TM7)、，" +
                "CA211，糖类抗原CA211、糖类抗原211（CA211）、CA211(C12)、糖类抗原CA211(生物芯片)、癌抗原211(CA211化学)，" +
                "fPSA/PSA，游离PSA与总PSA比值（FPSA/PSA）、F-PSA/PSA(蛋白芯片)、游离PSA与总PSA比值、F-PSA/PSA比值、F-PSA/PSA(C12)、F-PSA／PSA、游离前列腺/前列腺特异性抗原、游离与总前列腺特异性抗原比值，" +
                "鳞状细胞癌抗原，鳞状细胞癌抗原、鳞癌细胞抗原 SCC、鳞状细胞癌抗原(1)、鳞癌细胞抗原SCC、鳞状细胞癌相关抗原测定（SCC）、鳞状上皮细胞抗原（SCC）、SCC-ag(C)、鳞癌细胞抗原（SCC）、鳞状细胞癌抗原(华大)、（华大）鳞状细胞癌抗原、鳞状细胞癌抗原SCC(C8)、鳞状细胞癌抗原(SCC)、鳞状细胞癌抗原（SCC）测定、鳞状细胞癌抗原（SCC）（T13）、鳞状细胞癌抗原(SCCA)、鳞状细胞癌抗原测定（SCCA)、鳞状细胞癌抗原SCC(TM)、鳞状细胞癌抗原(TM6)、鳞状细胞癌抗原(SCC化学)、鳞状细胞癌抗原(TM7)、SCC(鳞状细胞癌抗原)、SCCA(鳞状细胞癌抗原)、鳞状上皮细胞癌抗原、鳞状上皮细胞癌相关抗原（SCC）、鳞状上皮细胞抗原、鳞状细胞癌相关抗原(C12)、鳞状细胞癌相关抗原（SCC）、鳞状细胞抗原SCC、鳞状细胞癌、SCCA(C7)、SCC-ag(C8)、鳞癌抗原SCC、SCC、SCC-ag(C7)、肺癌标志物检测SCC、SCC(C7)、鳞状细胞相关抗原测定（SCC）、SCCA(C12)，" +
                "糖类抗原242 ，糖类抗原242 、糖类抗原242（C12）、CA242、糖类抗原CA24-2（C12）、糖类抗原(CA24-2)、糖链抗原24-2测定、糖链抗原CA242、CA242（C12）、C-CA242（C12）、癌抗原242(CA242化学)、糖链抗原CA242测定（CA242）、糖链抗原(CA242)、糖类抗原(CA242)、糖蛋白抗原242（不用）、糖类抗原CA242测定、糖类抗原242（化学发光）、糖链抗原CA242测定（C12)、CA242(TM)、糖链抗原 CA242、糖类抗原CA242（C12）、糖类抗原242（T13）、糖类链抗原CA242、糖蛋白抗原CA242测定、CA242(FC12)、糖类抗原测定CA242、糖类抗原CA242(生物芯片)、糖类抗原242（CA242）、糖类抗原CA242(TM)、糖链抗原242（芯片法）、糖链抗原242测定、糖类抗原CA24-2(CA24-2)、(重复)糖类抗原CA24-2(1)、糖类抗原CA24-2（芯片）、糖类抗原CA24-2(蛋白芯片)、、糖类抗原CA24-2(华大)、糖链抗原CA24-2测定、糖链抗原 CA24-2测定、糖类抗原24-2，" +
                "乙肝病毒e抗原，乙肝病毒e抗原、乙肝病毒e抗原（定量）、乙型肝炎病毒e抗原(HBeAg)定量、乙型肝炎病毒e抗原定量(HBeAg)、乙型肝炎病毒e抗原定性（HBeAg）、乙型肝炎病毒e抗原(定性)、乙型肝炎病毒e抗原（定量）、乙型肝炎病毒e抗原（定量）（HBeAg）、乙肝e抗原（HBeAg）定性、乙肝肝炎病毒e抗原定量（HBeAg）、乙型肝类病毒e抗原定量（HBeAg）、、乙型肝炎e抗原(定量)、乙型肝炎e抗原HBeAg(定量)、乙肝e抗原半定量、乙肝病毒e抗原定量(HBeAg)、乙肝e抗原(定量)、乙肝e抗原HBeAg(定量)、乙肝e抗原（化学发光法）、乙肝病毒e抗原（HBeAg）定量、乙肝e抗原定性、乙型肝炎病毒表e抗原定性(HBeAg)、乙肝病毒e抗原定性(HBeAg)、乙肝病毒e抗原（定性）、乙型肝炎e抗原定性(HBeAg)、乙型肝类病毒e抗原定性（HBeAg）、乙肝e抗原(HBeAg)定量、乙型肝炎e抗原(定性)、乙型肝炎e抗原测定（HBeAg）、乙肝e抗原(定量)HBeAg、(重复)乙肝病毒e抗原、HBeAg   ( E 抗原 )，" +
                "甲型肝炎病毒抗体IgM，甲型肝炎病毒抗体IgM测定（HAV-IgM）、甲型肝炎抗体IgM检测、甲型肝炎IgM、甲型肝炎病毒IgM抗体(HAV)、甲型肝炎病毒抗体-IgM、甲型肝炎病毒抗体IgM(HAV-IgM)、甲型肝炎病毒抗体检测(IgM)、甲型肝炎病毒抗体测定（IgM）、甲型肝炎病毒抗体测定抗（IGM），" +
                "HCV RNA丙型肝炎病毒核酸，丙型肝炎抗体(HCV)、丙型肝炎病毒抗体、丙型肝炎病毒抗体（Anti-HCV）、丙型肝炎抗体定性、丙型肝炎病毒抗体测定（HCV-Ab）、丙型肝炎病毒抗体定性（Anti-HCV）、，" +
                "丁型肝炎IgM，丁型肝炎病毒抗体IgM、丁型肝炎病毒抗体IgM(Anti-HDV)、丁型肝炎病毒表面抗体IgM、丁型肝炎病毒抗体检测IgM、丁型肝炎病毒抗体HDV-IgM、，" +
                "糖类抗原CA72-4(华大)，糖类抗原CA72-4(TM)、糖类抗原CA72-4、癌抗原724、消化道三项CA72-4、CA724、糖类抗原CA72-4（芯片）、糖链抗原CA72-4（CA72-4)、糖基类抗原CA72-4、糖类抗原CA72-4(蛋白芯片)、糖蛋白抗原72-4测定(CA72-4)、糖链抗原72-4、CA72-4(C)、糖蛋白CA72-4(C12)、糖基类抗原72-4、(重复)糖链抗原CA72-4、糖链抗原72-4测定、癌抗原72-4、糖类抗原CA72-4(1)、糖链抗原CA72-4测定、糖链抗原CA72-4测定（CA72-4）、CA72-4、糖类抗原72-4、糖类抗原72-4（CA72-4）、糖链抗原72-4(CA 72-4)、糖类抗原CA72-4测定、糖类抗原（CA72-4）、CA724（C12）、糖链抗原 CA724、癌抗原724(CA724化学)、糖类抗原CA724(生物芯片)、糖链抗原CA724测定、CA724(T13)、糖类抗原724(CA724)、CA-724、糖类抗原CA724(化学发光）、糖链抗原724、糖类抗原CA724(TM)、糖类抗原CA724.、糖类抗原CA724 TM、糖类抗原(CA724)、癌类抗原CA724、CA724(FC12)、(重复)CA724，" +
                "冠心病相关，冠心病相关，" +
                "乙肝病毒表面抗原（定量），乙肝病毒表面抗原定量(HBsAg)、乙肝表面抗原HBsAg(定量)、乙型肝炎病毒表面抗原（定量）（HBsAg）、乙型肝类病毒表面抗原定量（HBsAg）、乙肝病毒表面抗原定性(HBsAg)、乙肝表面抗原（HBsAg）定量、乙型肝炎病毒表面抗定量(HBsAg)、乙型肝炎表面抗原HBsAg(定量)、乙型肝炎表面抗原测定（HBsAg）、HBsAg   (表面抗原)、乙型肝炎病毒表面抗原（定性）、乙型肝炎表面抗原(定量)、乙肝表面抗原（HBsAg）定性、乙型肝炎病毒表面抗原定性(HBsAg）、乙型肝炎病毒表面抗原定量(HBsAg)、乙肝表面抗原(化学发光法）、乙型肝炎表面抗原(定性)、乙肝病毒表面抗原（HBsAg）、乙型肝炎表面抗原定量（HBsAg)、乙肝表面抗原(定量)HBsAg、乙肝表面抗原定性、乙型肝炎表面抗原定性(HBsAg)、乙型肝类病毒表面抗原定性（HBsAg）、乙性肝炎病毒表面抗原定性（HBsAg）、乙肝病毒表面抗原（HBsAg）定量、乙型肝炎病毒表面抗原(HBsAg)定量、乙型肝炎病毒表表面抗原定性、乙肝表面抗原半定量(HBsAg)、乙肝病毒表面抗原（定性）、乙肝表面e抗原、乙肝病毒表\uE6C3抗原，" +
                "HCV-RNA检测，高灵敏度HCV-RNA，" +
                "糖类抗原CA测定，糖链抗原CA测定、糖类抗原测定(AFP)定量，" +
                "乙肝DNA定量，乙肝病毒DNA定量(HBV-DNA)、HBV-DNA、HBV-DNA定量、乙肝病毒(HBV)DNA、乙型肝炎病毒DNA测定(HBV-DNA)、乙型肝炎DNA（HBV-DNA）、乙肝DNA测定(HBV-DNA)、乙型肝炎病毒DNA定量、高敏乙肝DNA定量、乙肝DNA定量（HBV-DNA）、乙型肝炎病毒(HBV)DNA测定、乙型肝炎病毒定量、乙肝病毒-DNA、乙肝病毒-DNA定量、乙肝病毒DNA检测、乙型肝炎病毒DNA荧光定性、乙肝病毒定量（HBV-DNA）、乙型肝炎DNA测定，" +
                "冠心病(家族史)，家族性冠心病，" +
                "心脏病(既往病史)，心脏病(既往病史)，" +
                "小细胞肺癌筛查PROGRO，肺癌早筛，" +
                "男性肿瘤相关抗原13项，男性肿瘤相关抗原13项，" +
                "高灵敏丙肝病毒RNA定量检测，丙肝病毒抗体(HCV-RNA)定性、丙肝病毒-RNA定量（HCV-RNA）、丙肝病毒定量HCV-RNA、丙肝RNA测定(HCV－RNA)、丙肝病毒（HCV-RNA）定量、丙肝HCV-RNA定量、丙肝RNA定量、丙肝RNA、丙肝病毒定量（HCV-RNA）、丙肝HCV RNA、丙肝病毒RNA检测、丙肝RNA定性、丙肝基因分型、丙肝病毒基因、丙肝病毒RNA(定量)、丙型肝炎病毒(HCV-RNA)、丙型肝炎病毒核糖核酸扩增定量(HCV-RNA)、丙型肝炎病毒（HCV)RNA检测、丙型肝炎病毒（HCV-RNA）定量、丙型肝炎核糖核酸定量检测（HCV-RNA）、丙型肝炎病毒核酸(HCV RNA)测定、丙型肝炎病毒核糖核酸扩增定量(HCV-RNV)、丙型肝炎病毒核糖核酸扩增定量检测、丙型肝炎病毒核酸测定、，" +
                "HCV-IgM，HCV-IgM，" +
                "谷草转氨酶同工酶，谷草转氨酶同工酶，" +
                "血清β-谷氨酰转肽酶，血清β-谷氨酰转肽酶，" +
                "丙肝抗体IgG，丙肝病毒抗体(HCV-IgG)、丙肝抗体HCV-IgG、丙肝病毒抗体IgG、丙肝抗体定性IGG、丙型肝炎抗体IgG、丙型肝炎病毒抗体IgG（HCV-IgG）、HCV-IgG、HCV－Ag，" +
                "丙肝核心抗原，丙肝核心抗原，" +
                "丙肝病毒抗体(HCV-IGM)，丙型肝炎病毒抗体IgM（HCV-IgM）、丙型肝炎病毒抗体HCV-IgM、，" +
                "乙肝病毒核心抗原，乙肝病毒核心抗原，" +
                "丁型肝炎病毒，乙型肝炎病毒核心抗体IgM（Anti-HBcIgM）（定性）、乙肝病毒核心抗体HBcAb-IgM、乙型肝炎核心抗体IgM、乙肝核心抗体IGM(HBc-IgM)、乙型肝炎核心抗体IgM(定性)、 乙肝核心抗体IGM、乙肝核心抗体HBC-IGM、乙型肝炎IgM型核心抗体、乙肝病毒核心抗体IgM，" +
                "乙肝核心抗体IgM，乙肝核心IGM抗体定性，" +
                "庚肝标志物，庚型肝炎病毒标志物，" +
                "乙肝五项结论，乙肝五项定性结论、乙肝结论、乙肝五项（定量），" +
                "脑血管病(既往病史，脑血管病(既往病史，" +
                "肿瘤病史 ，肿瘤病史 ，" +
                "乙型肝炎病毒脱氧核糖核酸扩增定量检测(磁球法)，乙型肝炎病毒脱氧核糖核酸扩增定量(HBV-DNA)、乙型肝炎病毒脱氧核糖酸扩增定量（HBV-DNA）、乙型肝炎病毒脱氧核糖核酸扩增定量检测（HBV-DNA)、乙型肝炎病毒核酸(HBV DNA)测定、乙型肝炎病毒(HBV-DNA)定量(磁珠法)、乙肝病毒脱氧核糖核酸扩增定量（HBV-DNA）、乙肝病毒核酸(HBV-DNA)定量，" +
                "乙肝前蛋白S1，乙型肝炎病毒前S1抗原、乙型肝炎病毒外膜蛋白前S1抗原、乙型肝炎病毒S1抗原、乙肝病毒前S1抗原、前S1抗体、乙肝病毒外膜蛋白前S1抗原、，" +
                "甲肝抗原检测，甲肝抗原检测，" +
                "甲型肝炎抗体测定，甲型肝炎抗体测定(HAV)，" +
                "其他恶性肿瘤，其他恶性肿瘤，" +
                "癌细胞，癌细胞，" +
                "HLA-B27 ，HLA-B27、人类白细胞抗原HLA-B27、人类白细胞分化抗原HLA-B27、细胞免疫检测(HLA-B27 B7)、HLA-B27%、人类白细胞抗原B27、人类白细胞抗原B27(HLA-B27)、人类白细胞分化抗原(HLA-B27 )、人类白细胞抗原-B27、人类白细胞抗原(HLA-B27 )、人类白细胞抗原B-27(HLA-B27)、白细胞分化抗原B27、HLA-B7、细胞免疫功能检测，" +
                "谷氨酸脱羧酶抗体，抗谷氨酸羧酶抗体(GAD-Ab)、谷氨酸脱羧酶抗体、血清抗谷氨酸脱羧酶抗体、抗谷氨酸脱羧酶(GAD)抗体、抗谷氨酸脱羧酶抗体(GAD-Ab)，" +
                "高血压(既往病史，高血压(既往病史，" +
                "舒张压（左），左侧舒张压、舒张压(左侧)，" +
                "舒张压(第二次)，舒张压二次，" +
                "高血压病，高血压病，" +
                "复测舒张压，复测舒张压，" +
                "收缩压，收缩压(mmHg)  ，" +
                "舒张压，舒张压(mmHg)  ，" +
                "复测血压，复测血压，" +
                "高血压常见辩证分型，高血压常见辩证分型，";
        for (String a : valueMap.keySet()) {
            String s1 = valueMap.get(a);
            String[] split = a.split(",");
            if (s1.contains("弃")||s1.contains("查")){
                continue;
            }
            boolean a1 = str.contains(split[1]);
            if (a1 == true) {
                String s = flagIdMap.get(a);
                int i = Integer.parseInt(s);
                if (i > 1) {
                    rs = "0";
                    rsa=a+s1;
                    break;
                }
            }

            if (a.contains("糖化血红蛋白") || a.contains("总糖化血红蛋白")) {
                String trim = Pattern.compile(REGEX).matcher(s1).replaceAll("").trim();
                if (JavaUtils.isEmpty(trim)){

                }else {
                    double v = Double.parseDouble(trim);
                    if (v > 7.0) {
                        rs = "0";
                        rsa=a+s1;
                        break;
                    }
                }

            }

            if (a.contains("血糖") || a.contains("空腹血糖") || a.contains("平均血糖") || a.contains("血液葡萄糖") || a.contains("血清葡萄糖") || a.contains("葡萄糖") || a.contains("快速血糖")) {
                String trim = Pattern.compile(REGEX).matcher(s1).replaceAll("").trim();
                if (trim.equals("")) {

                } else {
                    double v = Double.parseDouble(trim);
                    if (v > 8.0) {
                        rs = "0";
                        rsa=a+s1;
                        break;
                    }
                }

            }

            if (a.contains("血糖") && a.contains("餐后")) {
                String trim = Pattern.compile(REGEX).matcher(s1).replaceAll("").trim();
                if (JavaUtils.isEmpty(trim)){

                }else {
                    double v = Double.parseDouble(trim);
                    if (v > 12.0) {
                        rs = "0" ;
                        rsa=a+s1;
                        break;
                    }
                }

            }

        }

       // System.out.println("糖尿病："+rsa);

        return rs;
    }
}
