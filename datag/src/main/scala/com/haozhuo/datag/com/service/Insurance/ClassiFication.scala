package com.haozhuo.datag.com.service.Insurance

import com.haozhuo.datag.common.RedisUtil
import com.haozhuo.datag.model.report.InsuranceMap
import com.haozhuo.datag.service.Insurance.{PushGan, PushGaoxueya, PushTangniaobing}
import org.springframework.beans.factory.annotation.Autowired

object ClassiFication {


  val checkIndexRsFlagIdSpecialRules: Array[(String, String, String)] = Array(
    ("^(?=.*DNA|.*基因)(?=.*HPV).*$", "4", "HPV-DNA分型检测阳性"),
    ("^(?=.*RPR).*$", "4", "RPR阳性"),
    ("^(?=.*TPPA).*$", "4", "TPPA阳性"),
    ("^(?=.*中值细胞百分|.*中值细胞比|.*中值细胞%).*$", "2", "中值细胞百分率偏低"),
    ("^(?=.*中值细胞百分|.*中值细胞比|.*中值细胞%).*$", "3", "中值细胞百分率增高"),
    ("^(?i)(?=.*人乳头瘤病毒|.*HPV)(?!.*DNA|.*基因).*$", "4", "人乳头瘤病毒（HPV）阳性"),
    ("^(?=.*疱疹)(?=.*I型)(?=.*IGG|.*IgG).*$", "4", "单纯疱疹I型IGG阳性"),
    ("^(?=.*疱疹)(?=.*I型)(?=.*IGM|.*IgM).*$", "4", "单纯疱疹I型IGM阳性"),
    ("^(?=.*疱疹)(?=.*Ⅱ型)(?=.*IGG|.*IgG).*$", "4", "单纯疱疹Ⅱ型IGG阳性"),
    ("^(?=.*疱疹)(?=.*Ⅱ型)(?=.*IGM|.*IgM).*$", "4", "单纯疱疹Ⅱ型IGM阳性"),
    ("^(?=.*MA/CR|.*尿微量白蛋白|.*尿-微白蛋白|.*尿ACR|.*尿微量白蛋白/尿肌酐|.*尿微量白蛋白肌酐比值|.*尿微量蛋白/肌酐比值|.*尿白蛋白/尿肌酐|.*尿蛋白/尿肌酐比值|.*ALB/CRE).*$", "3|4", "尿ACR测定阳性"),
    ("^(?=.*亚硝酸盐).*$", "4", "尿亚硝酸盐阳性"),
    ("^(?=.*白细胞)(?=.*尿).*$", "3|4", "尿白细胞增高"),
    ("^(?=.*红细胞)(?=.*尿).*$", "3|4", "尿红细胞镜检增高"),
    ("^(?=.*尿胆原|.*尿胆元).*$", "3|4", "尿胆原阳性"),
    ("^(?=.*胆红素)(?=.*尿).*$", "4", "尿胆红素阳性"),
    ("^(?=.*尿酮体).*$", "4", "尿酮体阳性"),
    ("^(?=.*隐血)(?!.*粪|.*便).*$", "4", "尿隐血阳性"),
    ("^(?=.*弓).*$", "4", "弓形体抗体测定阳性"),
    ("^(?=.*胰岛素抗体).*$", "3|4", "抗胰岛素抗体阳性"),
    ("^(?=.*红细胞)(?=.*白带|.*阴道).*$", "4", "白带红细胞阳性"),
    ("^(?=.*淋巴细胞)(?=.*率|.*比).*$", "3", "淋巴细胞百分率偏高"),
    ("^(?=.*淋巴细胞)(?=.*率|.*比).*$", "2", "淋巴细胞百分率降低"),
    ("^(?=.*白细胞)(?=.*白带|.*阴道).*$", "4", "白带白细胞阳性"),
    ("^(?=.*结核抗体).*$", "4", "结核抗体阳性"),
    ("^(?=.*血沉).*$", "3", "血沉偏高"),
    ("^(?=.*衣原体)(?!.*眼|.*肺).*$", "4", "衣原体测定阳性"),
    ("^(?=.*视黄醇结合蛋白).*$", "2", "视黄醇结合蛋白偏低"),
    ("^(?=.*视黄醇结合蛋白).*$", "3", "视黄醇结合蛋白增高"),
    ("^(?=.*风[疹|诊]病毒)(?=.*抗体).*$", "4", "风疹病毒抗体阳性"),
    ("^(?=.*锌).*$", "3", "锌测定偏低"),
    ("^(?=.*锌).*$", "2", "锌测定增高"),
    ("^(?=.*C3).*$", "2", "C3降低"),
    ("^(?=.*C4).*$", "2", "C4降低"),
    ("^(?=.*载脂蛋白A)(?=.*载脂蛋白B).*$", "3", "载脂蛋白A1/载脂蛋白B比值偏低"),
    ("^(?=.*微球蛋白|.*MG)(?=.*β2).*$", "3", "β2-微球蛋白增高"),
    ("^(?=.*NSE|.*烯醇).*$", "3", "NSE增高"),
    ("^(?=.*C)(?=.*反应蛋白).*$", "3", "C反应蛋白增高"),
    ("^(?=.*绒毛膜促性腺激素|HCG).*$", "3", "人绒毛膜促性腺激素增高"),
    ("^(?=.*尿-微白蛋白|.*尿微量白蛋白)(?!.*肌酐).*$", "3", "尿微量白蛋白增高"),
    ("^(?=.*嗜碱性粒细胞比|.*嗜碱性粒细胞百分|.*嗜碱性粒细胞%).*$", "3", "嗜碱性粒细胞比率增高"),
    ("^(?=.*嗜碱性粒细胞#|.*嗜碱性粒细胞\\(BAS#\\)|.*嗜碱性粒细胞\\(BASO#\\)|.*嗜碱性粒细胞\\[BAS#\\]|.*嗜碱性粒细胞值|.*嗜碱性粒细胞数|.*嗜碱性粒细胞绝对|.*嗜碱性粒细胞计数).*$", "3", "嗜碱性粒细胞计数增高"),
    ("^(?=.*前列腺特异|.*PSA)(?!.*游离|.*F).*$", "3", "前列腺特异性抗原增高"),
    ("^(?=.*单核细胞#|.*单核细胞\\(MONO#\\)|.*单核细胞\\[Mon#\\]|.*单核细胞值|.*单核细胞数|.*单核细胞绝对|.*单核细胞计数|.*镜检单核细胞).*$", "3", "单核细胞值增高"),
    ("^(?=.*叶酸).*$", "2", "叶酸降低"),
    ("^(?=.*抗)(?=.*O).*$", "3", "抗O增高"),
    ("^(?=.*羟丁酸)(?=.*酶).*$", "3", "羟丁酸脱氢酶增高"),
    ("^(?=.*半胱氨酸)(?!.*基因).*$", "3", "同型半胱氨酸增高"),
    ("^(?=.*脂蛋白)(?=.*a).*$", "3", "脂蛋白aLP(a)增高"),
    ("^(?=.*岩藻糖苷酶).*$", "3", "血清岩藻糖苷酶测定增高"),
    ("^(?=.*血小板分布宽度).*$", "3", "血小板分布宽度增高"),
    ("^(?=.*类风湿因子).*$", "3", "血清类风湿因子增高"),
    ("^(?=.*鳞状细胞癌).*$", "3", "鳞状细胞癌抗原增高"),
    ("^(?=.*中值细胞\\(MON#\\)|.*中值细胞数|.*中值细胞绝对值|.*中值细胞计数|.*中间值粒细胞绝对值|.*中间白细胞数|.*中间细胞#|.*中间细胞数|.*中间细胞绝对值|.*中间细胞计数).*$", "3", "中值细胞绝对值增高"),
    ("^(?=.*载脂蛋白A)(?=.*载脂蛋白B).*$", "2", "载脂蛋白A1/载脂蛋白B比值偏高")
  )
  val checkIndexRsFlagIdCommonRules: Array[(String, String)] = Array(
    //("^(?=.*C3).*$", "C3"),
    //("^(?=.*C4).*$", "C4"),


    ("^(?=.*CA50|.*抗原50).*$", "CA50"),
    ("^(?=.*C肽|.*C-肽).*$", "C肽"),
    ("^(?=.*二聚体).*$", "D-二聚体"),
    ("^(?=.*三碘甲状腺原氨酸|.*T3)(?!.*游离).*$", "三碘甲状腺原氨酸"),
    ("^(?=.*中性粒细胞#|.*中性粒细胞\\(NEU#\\)|.*中性粒细胞计数|.*中性粒细胞值|.*中性粒细胞数|.*中性粒细胞绝对|.*中性细胞值|.*中性细胞数|.*中性细胞绝对值|.*中性细胞计数|.*镜检中性分叶核细胞).*$", "中性粒细胞值"),
    ("^(?=.*中性粒细胞比|.*中性粒细胞百分|.*中性粒细胞%).*$", "中性粒细胞百分率"),
    ("^(?=.*促卵泡).*$", "促卵泡生成素(FSH)"),
    ("^(?=.*促黄体).*$", "促黄体生成素"),
    ("^(?=.*微量元素铁|.*微量铁|.*血清Fe|.*血清铁|.*血铁|.*铁元素|.*铁测定).*$", "全血铁"),
    ("^(?=.*嗜酸性粒细胞%|.*嗜酸性粒细胞百分|.*嗜酸性粒细胞比).*$", "嗜酸性粒细胞比率"),
    ("^(?=.*嗜酸性白细胞计数|.*嗜酸性粒细胞绝对|.*嗜酸性粒细胞\\(EO#\\)|.*嗜酸性粒细胞\\(EOS#\\)|.*嗜酸性粒细胞\\[Eos#\\]|.*嗜酸性粒细胞值\\(EOS#\\)|.*嗜酸性粒细胞数|.*嗜酸性粒细胞绝对|.*嗜酸细胞值|.*嗜酸细胞提示|.*嗜酸细胞数量|.*嗜酸细胞数量（EOS#）|.*嗜酸细胞绝对值|.*嗜酸细胞计数).*$", "嗜酸性粒细胞计数"),
    ("^(?=.*泌乳素|.*催乳素).*$", "垂体泌乳素"),
    ("^(?=.*孕酮).*$", "孕酮"),
    ("^(?=.*比重).*$", "尿比重"),
    ("^(?=.*尿酸碱度).*$", "尿酸碱度"),
    ("^(?=.*红细胞体积)(?!.*分布|.*宽度).*$", "平均红细胞体积"),
    ("^(?=.*平均红细胞血红蛋白)(?=.*浓度).*$", "平均红细胞血红蛋白浓度"),
    ("^(?=.*平均红细胞血红蛋白)(?=.*量).*$", "平均红细胞血红蛋白量"),
    ("^(?=.*血小板)(?=.*体积|.*容积).*$", "平均血小板体积"),
    ("^(?=.*平均血红蛋白)(?=.*量).*$", "平均血红蛋白含量"),
    ("^(?=.*总铁结合).*$", "总铁结合力"),
    ("^(红细胞分布宽度|红细胞体积分布宽度){1}$", "红细胞体积分布宽度"),
    ("^(?=.*红细胞)(?=.*分布宽度)(?=.*标准差|.*SD).*$", "红细胞分布宽度标准偏差"),
    ("^(?=.*红细胞)(?=.*压积).*$", "红细胞压积"),
    ("^(?=.*红细胞数|.*红细胞计数|.*RBC)(?!.*尿).*$", "红细胞计数"),
    ("^(?=.*CD3).*$", "CD3"),
    ("^(?=.*淋巴细胞)(?=.*值|.*数).*$", "淋巴细胞值"),
    ("^(?=.*三碘甲状腺)(?=.*游离).*|(?=.*游离T3|.*FT3).*$", "游离三碘甲状腺原氨酸"), //（三碘甲状腺 and 游离) |游离T3 | FT3
    ("^(?=.*甲状腺素|.*T4)(?!.*游离|.*促).*$", "甲状腺素"),
    ("^(?=.*甲状腺结合).*$", "甲状腺结合蛋白"),
    ("^(?=.*白细胞)(?=.*数|.*管)(?!.*尿).*$", "白细胞计数"),
    ("^(?=.*睾酮).*$", "睾酮"),
    ("^(?=.*肿瘤特异性生长因子|.*TSGF).*$", "肿瘤特异性生长因子"),
    ("^(?=.*胃泌素)(?!.*释放肽|.*G17).*$", "胃泌素"),
    ("^(?=.*胃蛋白酶)(?=.*Ⅰ)(?!.*/|.*\\|.*比).*$", "胃蛋白酶原Ⅰ"),
    ("^(?=.*胃蛋白酶)(?=.*Ⅱ)(?!.*/|.*\\|.*比).*$", "胃蛋白酶原Ⅱ"),
    ("^(?=.*胃蛋白酶)(?=.*/|.*\\|.*比).*$", "胃蛋白酶原比值"),
    ("^(?=.*血小板压积|.*血小板比容|.*PCT).*$", "血小板压积"),
    ("^(?=.*低密度)(?!.*比|.*\\|.*/).*$", "血清低密度脂蛋白"),
    ("^(?=.*促甲状腺|.*TSH).*$", "血清促甲状腺激素"),
    ("^(?=.*免疫球蛋白)(?=.*A).*$", "血清免疫球蛋白IgA"),
    ("^(?=.*免疫球蛋白)(?=.*G).*$", "血清免疫球蛋白IgG"),
    ("^(?=.*免疫球蛋白)(?=.*M).*$", "血清免疫球蛋白IgM"),
    ("^(?=.*总IgE).*$", "血清总IgE"),
    ("^(?=.*总胆固醇)(?!.*高密度).*$", "血清总胆固醇"),
    ("^(?=.*氯)(?!.*尿|.*胺).*$", "血清氯"),
    ("^(?=.*游离甲状腺|.*游离T4|.*FT4).*$", "血清游离甲状腺素"),
    ("^(?=.*甘油三).*$", "血清甘油三酯"),
    ("^(?=.*无机磷|.*血清磷).*|(磷){1}$", "血清磷"),
    ("^(?=.*血清钙|.*总钙|.*元素钙|.*钙元素|.*血钙|.*离子钙|.*钙离子|.*钙测定).*$", "血清钙"),
    ("^(?=.*血清钠|.*血钠).*$", "血清钠"),
    ("^(?=.*钾).*$", "血清钾"),
    ("^(?=.*血清铁)(?!.*蛋白).*|(?=.*血铁).*|(?=.*铁元素).*|(?=.*元素铁).*|(?=.*铁测定).*|(?=.*微量铁).*$", "血清铁"),
    ("^(?=.*铜).*$", "血清铜"),
    ("^(?=.*镁).*$", "血清镁"),
    ("^(?=.*高密度)(?=.*胆固醇).*$", "血清高密度脂蛋白胆固醇"),
    ("^(?=.*高密度脂蛋白)(?!.*胆固醇).*$", "血清高密度脂蛋白"),
    ("^(?=.*载脂蛋白A)(?!.*载脂蛋白B).*$", "载脂蛋白A1"),
    ("^(?=.*载脂蛋白B)(?!.*载脂蛋白A).*$", "载脂蛋白B"),
    ("^(?=.*铁蛋白)(?!.*转).*$", "铁蛋白"),
    ("^(?=.*雌二醇).*$", "雌二醇"),
    ("^(?=.*髓过氧化物酶|.*MPO).*$", "髓过氧化物酶MPO")
  )
  val summaryRuleArray = Array(
    ("(.*隐睾(?!术后).*)", "隐睾"),
    ("(.*脂肪瘤.*)", "脂肪瘤"),
    (".*(外耳道疖肿|耳道疖肿).*", "外耳道疖肿"),
    ("(.*玻璃体积血|眼底出血|视网膜出血|结膜.{1,5}血.*)", "眼底出血"),
    ("(.*腱鞘囊肿.*)", "腱鞘囊肿"),
    ("(.*飞蚊症.*)", "飞蚊症"),
    ("(.*精索静脉曲张(?!术).*)", "精索静脉曲张"),
    ("(.*神经性耳聋.*)", "神经性耳聋"),
    ("(.*鞘膜积液.*)", "睾丸鞘膜积液"),
    ("(.*鼓膜穿孔.*)", "鼓膜穿孔"),
    (".*(结膜水肿|结膜炎).*", "结膜炎"),
    ("(.*弱视.*)", "弱视"),
    ("(.*沙眼.*)", "沙眼"),
    (".*(宫颈纳氏囊肿|宫颈多发纳氏囊肿|宫颈囊肿|宫颈纳囊|纳氏腺囊肿).*", "宫颈纳氏囊肿"),
    ("(.*宫颈.*糜.*|.*宫颈柱状上皮异位.*)", "宫颈糜烂"),
    ("(.*耵聍栓塞.*)", "耵聍栓塞"),
    (".*(前庭大腺囊肿|前庭大腺.{0,3}囊性肿块).*", "前庭大腺囊肿"),
    (".*(包茎|包皮).*", "包茎"),
    ("(.*下肢静脉曲张.*)", "下肢静脉曲张"),
    ("(.*鼻中隔.*)", "鼻中隔偏曲"),
    ("(.*白癜风.*)", "白癜风"),
    ("(.*白内障.*)", "白内障"),
    ("(.*前列腺增.*|前列腺.*度.*增.*|.*前列腺稍大.*|.*前列腺肥大.*|.*前列腺肿大.*)", "前列腺增生"),
    (".*(阴道炎|阴道病|外阴阴道酵母菌病|分泌物.*豆渣样.*|豆渣样.*分泌物.*).*", "阴道炎"),
    (".*(体癣|头癣|[^牛]皮癣|股癣).*", "皮肤真菌感染"),
    (".*(外痔|混合痔|痔疮).*", "痔疮"),
    (".*(腹股沟疝气|腹股沟斜疝|腹壁切口疝|腹壁疝).*", "腹壁疝"),
    (".*(视力欠佳|视力下降|视力严重缺陷|眼科检查.*常见症状，视力减退.*).*", "视力欠佳"),
    (".*(乳腺小叶增生).*", "乳腺小叶增生"),
    (".*(牙结石).*", "牙结石"),
    (".*(慢性咽炎|咽黏膜|慢性咽喉炎|部滤泡组织增生).*", "慢性咽炎"),
    (".*(脉压增|脉压差增|脉压差大).*", "脉压增宽"),
    (".*(白带清洁度偏高|白带清洁度高).*", "白带清洁度偏高"),
    (".*(前列腺钙化).*", "前列腺钙化灶"),
    ("(.*心动过缓.*)", "窦性心动过缓"),
    (".*(窦性心律不齐).*", "窦性心律不齐"),
    (".*(骨量减少|骨量匮乏|骨量少).*", "骨量减少"),
    (".*(消化道出血).*", "消化道出血"),
    (".*宫颈肥(大|厚).*", "宫颈肥大"), //new
    ("(.*乳腺结节.*)", "乳腺结节"),
    ("(.*子宫.{0,3}肌瘤.*)", "子宫肌瘤"),
    ("(.*龋.*)", "龋齿"),
    (".*绝经(期|后)子宫.*", "绝经期子宫"),
    ("(.*胆.{0,3}(息肉|隆起).*)", "胆囊息肉"),
    ("(.*宫颈炎.*)", "宫颈炎症"),
    ("(.*宫颈.{0,3}息肉.*)", "宫颈息肉"),
    ("(.*宫颈刮片.*Ⅱ.*)", "宫颈刮片巴式Ⅱ级"),
    ("(.*宫颈刮片.*Ⅲ.*)", "宫颈刮片巴式Ⅲ级"),
    ("(.*宫颈刮片.*Ⅳ.*)", "宫颈刮片巴式Ⅳ级"),
    ("(.*宫颈刮片.*Ⅴ.*)", "宫颈刮片巴式Ⅴ级"),
    ("(.*宫颈.{0,3}肌瘤.*)", "宫颈肌瘤"),
    ("(.*宫颈.{0,3}裂.*)", "宫颈旧裂"),
    ("(.*牙面色素.*)", "牙面色素沉着"),
    ("(.*血黏度.?异常.*)", "血黏度异常"),
    (".*节育器.*(移|嵌顿|位置异常).*", "宫内节育器位置异常"),
    (".*肺纹理.*(多|强|粗).*", "肺纹理增多"),
    ("(.*盆腔.{0,3}积液.*)", "盆腔积液"),
    (".*颈椎.*(退|骨质增生|钙化|椎管狭窄|突出|病).*", "颈椎病"),
    ("^(?=.*乳腺.{0,3}增生)(?!.*小叶.*).*", "乳腺增生"),
    (".*腰椎.*(退|骨质增生|钙化|椎管狭窄|突出|病).*", "腰椎病"),
    (".*(残根|残冠|缺齿|牙缺失).*", "牙齿残缺"),
    ("(.*鼻炎.*)", "鼻炎"),
    (".*(鼻窦|额窦|蝶窦).*", "鼻窦炎"),
    ("(.*肾.{0,3}囊肿.*)", "肾囊肿"),
    (".*(牙龈|牙周炎|牙周袋|牙周脓肿|牙周病).*", "牙周病"),
    ("(.*胆囊.{0,3}结石.*)", "胆囊结石"),
    ("(.*智齿.*)", "智齿"),
    (".*扁桃体.{0,5}(充血|炎|脓肿).*", "扁桃体炎"),
    (".*左心室舒张功能.?(减低|减退).*", "左心室舒张功能减低"),
    ("(.*视网膜.{0,10}硬化.*)", "视网膜动脉硬化"),
    ("(.*心动过速.*)", "窦性心动过速"),
    ("(.*尿葡萄糖.*)", "尿葡萄糖"),
    ("(.*乳腺.{0,3}囊肿.*)", "乳腺囊肿"),
    ("(.*甲状腺.{0,3}囊肿.*)", "甲状腺囊肿"),
    (".*子宫.{0,3}(增大|肥大|略大|稍大).*", "子宫肥大"),
    (".*尖瓣.{0,3}(反流|返流|钙化|关闭不全|狭窄).*", "尖瓣关闭不全"),
    ("(.*前列腺.{0,5}囊肿.*)", "前列腺囊肿"),
    ("(.*颈.?动脉.{0,5}膜.*)", "颈动脉内膜增厚"),
    ("(.*哺乳期乳腺.*)", "哺乳期乳腺"),
    ("(.*乳腺导管扩张.*)", "乳腺导管扩张"),
    (".*卵巢.{0,3}(囊肿|血肿|肿块).*", "卵巢囊肿"),
    (".*(眼|结膜).{0,10}结石.*", "眼睑结膜结石"),
    ("(.*结膜.*翼状胬肉.*)", "结膜翼状胬肉"),
    ("((?!史).*早搏(?!史).*)", "心脏早搏"),
    (".*基底动脉.{0,5}(速度.?慢|弹性).*", "TCD检查:椎基底动脉血流速度减慢"),
    (".*(椎基底动脉血流速度.{1,3}快).*", "TCD检查:椎基底动脉血流速度增快"),
    (".*(上颌窦.{0,4}囊肿).*", "上颌窦囊肿"),

    ("(.*豹纹状.*)", "豹纹状眼底"),
    ("(.*牙齿.*松动.*)", "牙齿松动"),
    ("(.*肺.{0,3}硬结.*)", "肺内硬结灶"),
    ("(.*视神经乳头生理凹陷.*)", "视神经乳头生理凹陷扩大"),
    (".*(脾脏.?大).*", "脾脏增大"),
    ("(.*心脏杂音.*)", "心脏杂音"),
    ("(.*骨质增生.*)", "骨质增生"),
    ("(.*眼压偏高.*)", "眼压偏高"),
    ("(.*眼压偏低.*)", "眼压偏低"),
    ("(.*腹部.{0,3}压痛.*)", "腹部压痛"),
    ("(.*室.{0,2}高电压.*)", "心室高电压"),
    (".*胆囊壁.{0,3}(糙|增厚).*", "胆囊壁粗糙"),
    (".*胆囊.?胆固醇(沉|结).*", "胆囊胆固醇沉着"),
    ("(.*胆囊炎.*)", "胆囊炎"),
    ("(.*胆囊.?增大.*)", "胆囊增大"),
    ("(.*胆囊萎缩.*)", "胆囊萎缩"),
    ("(.*房室传导阻滞.*)", "房室传导阻滞"),
    ("(.*束支传导阻滞.*)", "束支传导阻滞"),
    ("(.*顺钟向转位.*)", "心电图顺钟向转位"),
    ("(.*逆钟向转位.*)", "心电图逆钟向转位"),
    ("(.*子宫内膜增厚.*)", "子宫内膜增厚"),
    ("(.*子宫内膜异位.*)", "子宫内膜异位"),
    ("(.*子宫内膜息肉.*)", "子宫内膜息肉"),
    ("(.*子宫内膜钙化.*)", "子宫内膜钙化"),
    ("(.*主动脉瓣.{0,5}[反返]流.*)", "主动脉瓣反流"),
    ("(.*P-R间期.*)", "短P-R间期综合征"),
    ("(.*肺动脉瓣.{0,5}[反返]流.*)", "肺动脉瓣反流"),
    (".*肺功能.{0,15}(限制|障碍).*", "肺功能限制"),
    ("(.*肾.{0,2}钙化.*)", "肾钙化灶"),
    ("(.*复极综合征.*)", "心电图早期复极综合征"),
    ("(.*子宫腺肌[症病].*)", "子宫腺肌症"),
    ("(.*ST-T.*)", "心电图ST-T改变"),
    ("(.*肺.*纤维.*)", "肺纤维灶"),
    (".*附件.{0,5}炎.*", "附件炎"),
    ("(.*附件.?囊.*)", "附件囊肿"),
    ("(.*主动脉迂曲.*)", "主动脉迂曲"),
    ("(.*副脾.*)", "副脾"),
    ("(.*牙.?[裂折].*)", "牙折"),
    ("(.*颌关节.*)", "颞下颌关节紊乱综合征"),
    ("(.*颌下淋巴结肿大.*)", "颌下淋巴结肿大"),
    ("(.*颈淋巴结肿大.*)", "颈淋巴结肿大"),
    ("(.*主动脉型心.*)", "主动脉型心脏"),
    ("(.*心影增大.*)", "心影增大"),
    ("(.*皮肤湿疹.*)", "皮肤湿疹"),
    ("(.*皮肤荨麻疹.*)", "皮肤荨麻疹"),
    ("(.*皮肤丘疹.*)", "皮肤丘疹"),
    ("(.*皮肤斑疹.*)", "皮肤斑疹"),
    ("(.*皮肤玫瑰疹.*)", "皮肤玫瑰疹"),
    ("(.*变色牙.*)", "变色牙"),
    ("(.*R波递增不良.*)", "R波递增不良"),
    ("(.*牙本质过敏.*)", "牙本质过敏"),
    ("(.*预激综合[征症].*)", "预激综合征"),
    (".*主动脉结(突出|钙化).*", "主动脉结钙化"),
    ("(.*膈抬高.*)", "横膈抬高"),
    ("(.*脾.{0,3}钙化.*)", "脾钙化灶"),
    ("(.*皮脂.?囊肿.*)", "皮脂腺囊肿"),
    ("(.*口腔黏膜.*)", "口腔黏膜疾病"),
    ("(.*肛[周门]湿疹.*)", "肛周湿疹"),
    ("(.*副耳.*)", "副耳"),
    (".*(多囊卵巢|卵巢多囊).*", "多囊卵巢综合征"),
    ("(.*左室假腱索.*)", "左室假腱索"),
    ("(.*板腺阻塞.*)", "眼睑板腺阻塞"),
    (".*(子宫纵隔|纵隔子宫).*", "子宫纵隔"),
    ("(.*乳腺实性占位.*)", "乳腺实性占位"),
    ("(.*EB病毒.*)", "抗EB病毒"),
    ("(.*右位心.*)", "右位心"),
    ("(.*主动脉.?增宽.*)", "主动脉增宽"),
    ("(.*回奶期乳腺.*)", "回奶期乳腺"),
    ("(.*透明黏性白带.*)", "透明黏性白带"),
    ("(.*肺.{0,5}钙化.*)", "肺内钙化灶"),
    ("(.*肺气肿.*)", "肺气肿"),
    ("(.*肺动脉高压.*)", "肺动脉高压"),
    ("(.*眼球突出.*)", "眼球突出"),
    ("(.*眼球震颤.*)", "眼球震颤"),
    ("(.*眼球萎缩.*)", "眼球萎缩"),
    (".*(骶椎.?裂|腰椎骶化|腰椎.?裂).*", "隐性骶椎裂"),
    ("(.*前列腺.{0,2}回声.*)", "前列腺回声异常"),
    ("(.*[^诉]声音嘶哑[^史].*|.*[^诉]?声音嘶哑$)", "声音嘶哑"),
    ("(.*畸胎瘤[^术].*|.*畸胎瘤$)", "卵巢畸胎瘤"),
    ("(.*麦粒肿.*)", "眼睑麦粒肿"),
    ("(.*输尿管扩张.*)", "输尿管扩张"),
    ("(.*胃下垂.*)", "胃下垂"),
    (".*视神经(萎缩|乳头色).*", "视神经萎缩"),
    (".*玻璃体.{0,3}([混浑]浊|索条|星状|异物|异常).*", "玻璃体异常"),
    ("(.*肛管.{0,3}息肉.*)", "肛管息肉"),
    ("(.*外阴白斑.*)", "外阴白斑"),
    ("(.*外阴湿疹.*)", "外阴湿疹"),
    ("(.*纵隔增宽.*)", "纵隔增宽"),
    ("(.*丙肝.{0,10}阳性.*)", "丙肝"),
    ("(.*横位心.*)", "横位心"),
    ("(.*肋骨.{0,3}畸形.*)", "肋骨畸形"),
    ("(.*输卵管积水.*)", "输卵管积水"),
    ("(.*腹部包块.*)", "腹部包块"),
    ("(.*[^咽]?喉炎.*)", "喉炎"),
    ("(.*耳鸣.*)", "耳鸣"),
    ("(.*扁平苔藓.*)", "扁平苔藓"),
    ("(.*膀胱结石.*)", "膀胱结石"),
    (".*心肌.?(缺血|供血不足).*", "心肌缺血"),
    ("(.*唇炎.*)", "唇炎"),
    ("(.*膝关节.*)", "膝关节病"),
    ("(.*脑萎缩.*)", "脑萎缩"),
    ("(.*支气管炎[^史].*|.*支气管炎$)", "支气管炎"), //胸部正位检查:慢性支气管炎
    ("(.*附睾结节.*)", "附睾结节"),
    //心电图检查结果:窦性心律陈旧性下壁心肌梗塞
    (".*(氟斑牙|牙菌斑).*", "氟斑牙"),
    (".*黄斑.{0,5}(变|紊乱|暗淡|消失|出血|裂孔).*", "黄斑病变"),
    ("(.*宫腔.{0,16}积液.*)", "宫腔积液"),
    ("(.*晶体后囊.{0,3}密度增高.*)", "晶体后囊密度增高"),
    ("(.*胃粘膜层毛糙.*)", "胃粘膜层毛糙"),
    (".*阴道.{1,6}(膨出|赘生物|囊肿|包块).*", "阴道壁膨出"),
    ("(.*心房.{0,2}大.*)", "心房增大"),
    ("(.*心房.{0,3}颤.*)", "心房纤颤"),
    (".*呼吸音(粗|减弱|消失|增强|异常).*", "呼吸音异常"),
    ("(.*肾.{0,2}积水.*)", "肾积水"),
    (".*肺门影(增大|增浓).*", "肺门影增大"),
    ("(.*中耳炎.*)", "中耳炎"),
    ("(.*耳廓.{0,3}软骨膜炎.*)", "耳廓软骨膜炎"),
    ("(.*颈总?动脉.{0,16}斑块?.*)", "颈动脉斑块"),
    ("(.*耳.{0,3}湿疹.*)", "耳道湿疹"),
    ("(.*牙.{0,3}磨损.*)", "牙齿磨损"),
    (".*角膜(云翳|斑翳).*", "角膜云翳"),
    ("(.*角膜白斑.*)", "角膜白斑"),
    ("(.*角膜炎.*)", "角膜炎"),
    ("(.*角膜老年环.*)", "角膜老年环"),
    (".*角膜后(色素沉着|KP).*", "角膜后色素沉着"),
    ("(.*角膜.{0,2}新生血管.*)", "角膜新生血管"),
    ("(.*角膜上皮脱落.*)", "角膜上皮脱落"),
    ("(.*结膜.{0,7}色素斑.*)", "结膜色素斑"),
    ("(.*结膜.{0,4}裂斑.*)", "结膜裂斑"),
    ("(.*结膜下局限性结缔组织增生.*)", "结膜下局限性结缔组织增生"),
    (".*椎间?(孔|隙|管)狭窄.*", "椎管狭窄"),
    ("(.*斜视.*)", "眼斜视"),
    (".*左.?[室房].?(厚|大).*", "左室增大"),
    (".*右.?[室房].?(厚|大).*", "右室增大"),
    (".*肾内?(低|高|异常低|异常高|异常)回声.*", "肾异常回声"),
    ("(.*结膜囊肿.*)", "结膜囊肿"),
    (".*(脾厚|脾脏增厚).*", "脾厚"),
    ("(.*腋下淋巴结?肿大.*)", "腋下淋巴肿大"),
    ("(.*脾脏?囊肿.*)", "脾囊肿"),
    ("(.*会阴旧裂.*)", "会阴旧裂"),
    (".*(子宫|宫颈)囊肿.*", "子宫囊肿"),
    ("(.*肩周炎.*)", "肩周炎"),
    (".?(胶囊.镜|内科检查|胃部?彩超).*胃炎.*", "胃炎"),
    (".*便[隐潜]血.{0,13}([1-9][0-9]{2,3}|阳性).*|.*便血.*|.*常规见红细胞.*", "大便潜血"),
    ("(.*脑.{0,3}梗塞?.*)", "脑梗塞"),
    ("(.*肺炎.*)", "肺炎"),
    ("(.*上皮角化过度.*)", "上皮角化过度"),
    (".*晶状体(.{0,6}混浊|.{0,6}浑浊|色素沉着).*", "晶状体浑浊"),
    (".*(血管弹性.{0,3}[弱低硬降]).*", "血管硬化"),
    ("(.*脉络膜(局限)?萎缩.*)", "脉络膜萎缩"),
    (".*虹膜(残膜|残留|.{0,6}粘连|局部缺损|血管增生|根部断裂|异常).*", "虹膜异常"),
    (".*(心?室).{0,1}(肥厚|肥大).*", "心室肥大"),
    ("(.*双子宫.*)", "双子宫"),
    ("(.*多囊肝.*)", "多囊肝"),
    (".*左心?室(舒张)?顺应性.[低退佳].*", "左室顺应性欠佳"),
    ("(.*电轴.{0,3}偏.*)", "心电轴偏移"),
    ("(.*霰粒肿.*)", "眼睑霰粒肿"),
    ("(.*肛乳头肥大.*)", "肛乳头肥大"),
    ("(.*肺结核.*)", "肺结核"),
    ("(.*肾钙乳症.*)", "肾钙乳症"),
    ("(.*乳腺.{0,9}纤维腺?瘤.*)", "乳腺纤维瘤"),
    (".*乳.{0,3}结构.{0,2}(紊乱|不良).*", "乳腺腺体结构紊乱"),
    ("(.*副乳.*)", "副乳"),
    ("(.*支原体抗体.*)", "支原体抗体"),
    (".*膈.{0,4}(膨出|膨隆|隆起).*", "膈膨出"),
    ("(.*室间隔.{0,5}增厚.*)", "室间隔增厚"),
    ("(.*交界性心[率|律].*)", "交界性心律"),
    (".*视网膜.{0,2}(渗出|出血).*", "视网膜渗出"),
    ("(.*脾血管瘤.*)", "脾血管瘤"),
    ("(.*睑水肿.*)", "眼睑水肿"),
    ("(.*睑内翻.*)", "眼睑内翻"),
    ("(.*睑缘炎.*)", "睑缘炎"),
    ("(.*睑闭合障碍.*)", "眼睑闭合障碍"),
    ("(.*前牙反牙合.*)", "前牙反牙合"),
    ("(.*虹睫炎.*)", "虹睫炎"),
    ("(.*颈.?动脉闭塞.*)", "颈动脉闭塞"),
    ("(.*子宫脱垂.*)", "子宫脱垂"),
    ("(.*耳屏.{0,3}皮赘.*)", "耳屏皮赘"),
    (".*(肾异位|异位肾).*", "肾异位"),
    ("(.*腹股沟淋巴结肿大.*)", "腹股沟淋巴结肿大"),
    ("(.*耳后淋巴结肿大.*)", "耳后淋巴结肿大"),
    ("(.*锁骨上窝淋巴结肿大.*)", "锁骨上窝淋巴结肿大"),
    ("(.*口疮.*)", "口疮"),
    ("(.*口角炎.*)", "口角炎"),
    ("(.*肾萎缩.*)", "肾萎缩"),
    ("(.*重复肾.*)", "重复肾"),
    ("(.*胰腺囊肿.*)", "胰腺囊肿"),
    (".*(动脉瘤).*", "主动脉瘤"),
    (".*(乳突区肿大).*", "乳突区肿大"),
    (".*(乳腺.{0,6}回声).*", "乳腺异常回声"),
    (".*(乳腺炎).*", "乳腺炎"),
    (".*(乳腺.{0,2}退化不全).*", "乳腺退化不全"),
    (".*(乳腺钙化).*", "乳腺钙化"),
    (".*(佝偻病).*", "佝偻病"),
    (".*(冠状窦性心律).*", "冠状窦性心律"),
    (".*(前列腺.{0,6}结).*", "前列腺结石"),
    (".*前列腺占位性病变.*", "前列腺占位性病变"),
    (".*(前列腺炎).*", "前列腺炎"),
    (".*(动脉导管未闭).*", "动脉导管未闭"),
    (".*(主动脉硬化).*", "动脉硬化"),
    (".*(化脓性皮肤病).*", "化脓性皮肤病"),
    (".*(肾缺如|肾代偿性增大).*", "单侧肾缺如"),
    ("^(?=.*十二指肠溃疡)(?!.*史|.*幽门).*$", "十二指肠溃疡"),
    (".*(卵巢病变).*", "卵巢占位性病变"),
    (".*卵巢[稍]{0,1}(增|偏)大.*", "卵巢增大"),
    (".*(口炎).*", "口炎"),
    (".*(口腔囊肿).*", "口腔囊肿"),
    (".*听力(减退|偏移).*", "听力减退"),
    (".*呼吸音(减弱|消失|低).*", "呼吸音减弱"),
    (".*呼吸音(粗|增强).*", "呼吸音增强"),
    (".*(唇裂).*", "唇裂"),
    (".*嗅觉(障碍|功能减退).*", "嗅觉障碍"),
    (".*基底动脉血流速度不对称.*", "基底动脉血流速度不对称"),
    (".*基底动脉血流速度(略|稍|中度)减慢.*", "基底动脉血流速度减慢"),
    (".*(声带息肉).*", "声带息肉"),
    (".*(外耳道真菌病).*", "外耳道真菌病"),
    (".*(多发性硬化).*", "多发性硬化"),
    (".*(多囊肾).*", "多囊肾"),
    (".*(多囊脾).*", "多囊脾"),
    (".*(头颅血肿).*", "头颅血肿"),
    (".*(始基子宫).*", "始基子宫"),
    (".*(子宫萎缩).*", "子宫萎缩"),
    (".*(室壁瘤).*", "室壁瘤"),
    (".*(室间隔缺损).*", "室间隔缺损"),
    (".*(屈光).*", "屈光不正"),
    (".*(左前分支传导阻滞).*", "左前分支传导阻滞"),
    (".*(巩膜黄染).*", "巩膜黄染"),
    (".*(巴彬斯基征).*", "巴彬斯基征"),
    (".*(幼稚子宫).*", "幼稚子宫"),
    (".*(低电压).*", "心电图低电压"),
    (".*(心肌病).*", "心肌病"),
    (".*(慢性胰腺炎).*", "慢性胰腺炎"),
    (".*(房室分离).*", "房室分离"),
    (".*(房间隔膨出瘤).*", "房间隔膨出瘤"),
    (".*(日照性皮炎).*", "日照性皮炎"),
    (".*(残端息肉).*", "术后阴道残端息肉"),
    (".*(椎动脉血流速度不对称).*", "椎动脉血流速度不对称"),
    (".*(椎基底动脉供血不足).*", "椎基底动脉供血不足"),
    (".*(楔状缺损).*", "楔状缺损"),
    (".*(永存骨骺).*", "永存骨骺"),
    (".*(海绵状血管瘤).*", "海绵状血管瘤"),
    (".*(海绵肾).*", "海绵肾"),
    (".*(淋巴结肿大).*", "淋巴结肿大"),
    (".*(游走肾).*", "游走肾"),
    (".*(牙龈瘤).*", "牙龈瘤"),
    (".*(病窦综合征).*", "病窦综合征"),
    (".*(痛风).*", "痛风"),
    (".*(皮疹).*", "皮疹"),
    (".*(直肠前突).*", "直肠前突"),
    (".*(直肠息肉).*", "直肠息肉"),
    (".*(直肠炎).*", "直肠炎"),
    (".*(直肠脱垂).*", "直肠脱垂"),
    (".*(眼球活动受限).*", "眼球活动受限"),
    (".*(睑外翻).*", "眼睑外翻"),
    (".*(睾丸占位性病变).*", "睾丸占位性病变"),
    (".*(睾丸囊肿).*", "睾丸囊肿"),
    (".*(睾丸微石征).*", "睾丸微石征"),
    (".*(瞳孔变形).*", "瞳孔变形"),
    (".*(窦房结内游走心律).*", "窦房结内游走心律"),
    (".*(筛窦炎).*", "筛窦炎"),
    (".*(类风湿性关节炎).*", "类风湿性关节炎"),
    (".*(糖尿病视网膜病变).*", "糖尿病视网膜病变"),
    (".*(缺血性视神经病变).*", "缺血性视神经病变"),
    (".*(耳前瘘管).*", "耳前瘘管"),
    (".*(肋膈角变钝).*", "肋膈角变钝"),
    (".*(肛瘘).*", "肛瘘"),
    (".*(肛窦炎).*", "肛窦炎"),
    (".*(肛裂).*", "肛裂"),
    (".*(肺大泡).*", "肺大泡"),
    (".*(肺肿块影).*", "肺肿块影"),
    (".*(肺转移瘤).*", "肺转移瘤"),
    (".*(肺间质性改变).*", "肺间质性改变"),
    (".*(肾弥漫性).*", "肾弥漫性病变"),
    (".*(肾柱肥大).*", "肾柱肥大"),
    (".*(肾结石).*", "肾结石"),
    (".*(肾错构瘤).*", "肾错构瘤"),
    (".*(胆囊腺肌症).*", "胆囊腺肌症"),
    (".*(胸壁软组织).*", "胸壁软组织损伤"),
    (".*(脑供血不足).*", "脑供血不足"),
    (".*(脑膜瘤).*", "脑膜瘤"),
    (".*(腭裂).*", "腭裂"),
    (".*(腹部有?反跳痛).*", "腹部反跳痛"),
    (".*(膀胱壁小梁小房形成).*", "膀胱壁小梁小房形成"),
    (".*(膀胱憩室).*", "膀胱憩室"),
    (".*(藏毛疾病).*", "藏毛疾病"),
    (".*(蛛网膜囊肿).*", "蛛网膜囊肿"),
    (".*(融合肾).*", "融合肾"),
    (".*(视神经乳头炎).*", "视神经乳头炎"),
    (".*(视网膜有髓神经纤维).*", "视网膜有髓神经纤维"),
    (".*(视网膜色素变性).*", "视网膜色素变性"),
    (".*(踝关节痛).*", "踝关节痛"),
    (".*(逸搏).*", "逸搏"),
    (".*(长Q-T间期综合征).*", "长Q-T间期综合征"),
    (".*(阴囊湿疹).*", "阴囊湿疹"),
    (".*(阴茎疣状物).*", "阴茎疣状物"),
    (".*(阴茎肿物).*", "阴茎肿物"),
    (".*(阴道横膈).*", "阴道横膈"),
    (".*(附件压痛).*", "附件压痛"),
    (".*(雷诺综合征).*", "雷诺综合征"),
    (".*(青光眼).*", "青光眼"),
    (".*(静脉血栓).*", "静脉血栓"),
    (".*(颈淋巴结结核).*", "颈淋巴结结核"),
    (".*(颈部肿块).*", "颈部肿块"),
    (".*(额骨).*", "额骨骨病"),
    (".*(风湿性心脏病).*", "风湿性心脏病"),
    (".*(骨瘤).*", "骨瘤"),
    (".*(骶椎腰化).*", "骶椎腰化"),
    (".*(髌骨软化症).*", "髌骨软化症"),
    (".*(高血压病视网膜病变).*", "高血压病视网膜病变"),
    (".*(鳞状上皮炎症反应性改变).*", "鳞状上皮炎症反应性改变"),
    (".*(鼻息肉).*", "鼻息肉"),
    (".*(义齿).*", "义齿"),
    (".*(宫内.{0,1}妊|宫内早孕|如孕|宫内早妊|宫内中妊|宫内晚妊|妊娠试验(HCG)阳性|妊娠期).*", "宫内妊娠"),
    (".*(宫.{1,3}回声).*", "宫内异常回声"),
    (".*宫颈(黏膜|内口)?充血.*", "宫颈充血"),
    (".*(子宫|宫颈)占位性病变.*", "子宫占位性病变"),
    (".*(宫颈接触性出血|宫颈触血).*", "宫颈接触性出血"),
    (".*(脊柱侧|脊柱.{0,5}凸).*", "脊柱侧弯"),
    ("^(?=.*近视|.*零视力)(?!.*矫正).*$", "近视"),
    (".*膀胱(实性)?占位.*", "膀胱占位性病变"),
    (".*宫颈(缺如|畸形).*", "宫颈畸形"),
    (".*(阴道壁|宫颈)(口见)?赘生物.*", "宫颈赘生物"),
    (".*小气道(功能受损|病变).*", "小气道功能受损"),
    ("(.*左心?室收缩功能减低.*)", "左室收缩功能减低"),
    (".*巨细胞病毒抗体IgG阳性.*", "巨细胞病毒感染中"),
    (".*(葡萄膜炎|巩膜炎).*", "巩膜炎"),
    (".*(幽门|HP阳性).*", "幽门螺旋杆菌感染"),
    (".*心包腔?积液.*", "心包积液"),
    (".*(心电图异常|奔马律|房异常|异位心律|心电图低电压|频发室早部分呈三联律).*", "心电图异常"),
    (".*(急性咽喉?炎).*", "急性咽炎"),
    (".*房间(隔|膈)缺损.*", "房间隔缺损"),
    (".*(扁桃体.{1,3}大).*", "扁桃体肿大"),
    (".*(枕.{1,3}淋巴结肿大).*", "枕部淋巴结肿大"),
    (".*(椎动脉流速减低|椎动脉血流速度略?减?慢).*", "椎动脉血流速度减慢"),
    (".*(椎动脉血流速度略?增?快).*", "椎动脉血流速度增快"),
    (".*(气管.{0,5}受压).*", "气管受压"),
    (".*(甲状腺机能减退|桥本病及甲减).*", "甲状腺机能减退"),
    (".*(盆腔(包|肿)块).*", "盆腔肿块"),
    (".*直肠肿(物|块).*", "直肠肿物"),
    (".*(眼底不能窥视|眼底窥视不清|眼底细节不辨).*", "眼底窥视不清"),
    (".*(肋骨(良性)?肿瘤).*", "肋骨肿瘤"),
    (".*(肘关节痛|肱骨外上髁炎|肘关节疼痛).*", "肘关节痛"),
    (".*肛(周|旁)脓肿.*", "肛周脓肿"),
    (".*(脂肪过多型|肥胖|体脂肪率偏高).*", "肥胖"),
    (".*(肺动脉(内径)?增宽).*", "肺动脉增宽"),
    (".*肺内?结节(影|性质待定).*", "肺结节影"),
    (".*(通气(功能)?(障碍|异常)|肺活量不及格|肺功能非常严重的限制|峰流速下降|阻塞性睡眠呼吸暂停低通气综合征可能).*", "肺通气功能异常"),
    (".*(肺陈旧性病灶|肺片状模糊影).*", "肺陈旧性病灶"),
    (".*(肾实性占位|肾占位性病变).*", "肾占位性病变"),
    (".*(肾实质呈损害改变|肾脏损害).*", "肾脏损害"),
    (".*(胆囊实性占位|胆囊占位性病变).*", "胆囊占位性病变"),
    (".*(胰.{1,4}占位).*", "胰腺占位性病变"),
    (".*(胰腺.{0,3}回声).*", "胰腺异常回声"),
    (".*(胸膜.{0,4}钙化).*", "胸膜钙化"),
    (".*(脑.{0,1}动脉血流速度不对称).*", "脑动脉血流速度不对称"),
    (".*(脑动脉血流速度.{1,3}慢).*", "脑动脉血流速度减慢"),
    (".*(脑动脉血流速度.{1,3}快).*", "脑动脉血流速度增快"),
    (".*(脾.{1,3}回声).*", "脾内回声异常"),
    (".*腰腿(痛|疼).*", "腰腿痛"),
    (".*(腱鞘炎).*", "腱鞘炎"),
    (".*(腹股沟.{0,1}疝).*", "腹股沟疝"),
    (".*膀胱壁(局限性)?增厚.*", "膀胱壁增厚"),
    (".*(膀胱异常回声|膀胱强回声|膀胱壁后回声).*", "膀胱异常回声"),
    (".*(色觉异常|色弱).*", "色觉异常"),
    (".*(低血压|血压偏低).*", "血压偏低"),
    (".*(血液?流变异常).*", "血流变异常"),
    (".*(视网膜微?血管瘤).*", "视网膜血管瘤"),
    (".*(许莫氏|schmorl)结节.*", "许莫氏结节"),
    (".*(超重|偏重|体重指数偏高).*", "超重"),
    (".*(输尿管.{0,2}囊肿).*", "输尿管囊肿"),
    (".*(输尿管(下段|上段)?结石).*", "输尿管结石"),
    (".*(阴囊(触及)?肿物).*", "阴囊肿物"),
    (".*附件(包块|区肿块).*", "附件包块"),
    (".*附件区.{0,3}占位.*", "附件区占位性病变"),
    (".*(附睾肿大|睾丸肿大|附睾炎).*", "附睾炎"),
    (".*韧带.{0,2}钙化.*", "韧带钙化"),
    (".*(血管狭窄|颈动脉狭窄).*", "颈动脉狭窄"),
    (".*颈内?动脉血流速度不对称.*", "颈动脉血流速度不对称"),
    (".*颈内?动脉血流速度(略|均|轻度)?减慢.*", "颈动脉血流速度减慢"),
    (".*(肩关节疼?痛|颈肩痛).*", "颈肩痛"),
    (".*(骨质疏松|骨质减少|骨质轻度流失|骨密度降?低).*", "骨质疏松"),
    (".*(高血脂|高脂血|血脂异常).*", "高血脂"),
    (".*鼻前庭(疖肿|炎).*", "鼻前庭炎"),
    (".*鼻甲(肿|肥).*", "鼻甲肥大"),
    (".*鼻(中隔)?黏膜出血.*", "鼻黏膜出血"),
    (".*(体脂肪率偏低|体重不足|体重偏低|体重指数偏低|体重过低|偏瘦|消瘦).*", "偏瘦")
  )
  val ResultLabel = Array(
    ("乙肝大三阳"),
    ("乙肝小三阳"),
    ("嗜酸性粒细胞计数增高"),
    ("嗜酸性粒细胞计数降低"),
    ("左室舒张功能减低"),
    ("心横位"),
    ("抗甲状腺球蛋白抗体增高"),
    ("红细胞体积分布宽度增高"),
    ("红细胞体积分布宽度降低"),
    ("红细胞分布宽度标准偏差增高"),
    ("红细胞分布宽度标准偏差降低"),
    ("耳后淋巴结"),
    ("胃蛋白酶原比值增高"),
    ("胃蛋白酶原比值降低"),
    ("腰臀比增高"),
    ("腹股沟淋巴结"),
    ("锁骨上窝淋巴结"),
    ("颌下淋巴结")
  )


  val MatchGXYLabel = Array(
    ("^(?=.*125|.*12-5).*$"),
    ("^(?=.*153|.*15-3).*$"),
    ("^(?=.*199|.*19-9).*$"),
    ("^(?=.*242|.*24-2).*$"),
    ("^(?=.*724|.*724).*$"),
    ("^(?=.*21-1).*$"),
    ("^(?=.*B27).*$"),
    ("^(?=.*果糖胺).*$"),
    ("^(?=.*癌胚抗原).*$"),
    (".*(空腹血糖(调节)?受损).*"),
    ("^(?=.*血糖)(?=.*腹).*$"),
    ("^(?=.*血红蛋白)(?=.*糖).*$"),
    ("^(?=.*糖尿病)(?!.*视网膜).*$"),
    ("^(?=.*肌酸激酶)(?!.*同|.*磷).*$"),
    ("^(?=.*胰岛素)(?!.*抗体|.*生长).*$"),
    ("^(?=.*谷氨酰).*$"),
    ("^(?=.*尿素氮)(?!.*肌酐).*$"),
    (".*(Q波异常|异常Q波).*"),
    ("^(?=.*戊)(?=.*肝).*$"),
    ("^(?=.*甲)(?=.*肝)(?=.*IgM|.*IGM).*$"),
    ("^(?=.*甲胎蛋白).*$"),
    ("^(?=.*胆红素)(?=.*直接).*$"),
    ("^(?=.*肌酐)(?!.*尿).*$"),
    ("^(?=.*乳酸脱氢酶)(?!.*同).*$"),
    ("^(?=.*胆红素)(?=.*总).*$"),
    ("^(?=.*碱性磷酸酶).*$"),
    ("^(?=.*丙转氨酶|.*丙氨酸氨基转移酶)(?!.*谷草).*$"),
    ("^(?=.*草转氨酶|.*冬氨酸氨基转移酶)(?!.*谷丙|.*同工).*$"),
    ("^(?=.*谷氨酰)(?=.*转).*$"),
    ("^(?=.*胆红素)(?=.*间接).*$"),
    ("^(?=.*血糖)(?=.*餐后).*$"),
    ("^(?=.*高血压|.*血压偏高)(?!.*视网膜).*$")

  )

  val MatchTNBLabel = Array(
    ("^(?=.*125|.*12-5).*$"),
    ("^(?=.*153|.*15-3).*$"),
    ("^(?=.*199|.*19-9).*$"),
    ("^(?=.*242|.*24-2).*$"),
    ("^(?=.*724|.*724).*$"),
    ("^(?=.*21-1).*$"),
    ("^(?=.*B27).*$"),
    ("^(?=.*果糖胺).*$"),
    ("^(?=.*癌胚抗原).*$"),
    (".*(空腹血糖(调节)?受损).*"),
    ("^(?=.*血糖)(?=.*腹).*$"),
    ("^(?=.*血红蛋白)(?=.*糖).*$"),
    ("^(?=.*糖尿病)(?!.*视网膜).*$"),
    ("^(?=.*肌酸激酶)(?!.*同|.*磷).*$"),
    ("^(?=.*胰岛素)(?!.*抗体|.*生长).*$"),
    ("^(?=.*谷氨酰).*$"),
    ("^(?=.*尿素氮)(?!.*肌酐).*$"),
    (".*(Q波异常|异常Q波).*"),
    ("^(?=.*戊)(?=.*肝).*$"),
    ("^(?=.*甲)(?=.*肝)(?=.*IgM|.*IGM).*$"),
    ("^(?=.*甲胎蛋白).*$"),
    ("^(?=.*胆红素)(?=.*直接).*$"),
    ("^(?=.*肌酐)(?!.*尿).*$"),
    ("^(?=.*乳酸脱氢酶)(?!.*同).*$"),
    ("^(?=.*胆红素)(?=.*总).*$"),
    ("^(?=.*碱性磷酸酶).*$"),
    ("^(?=.*丙转氨酶|.*丙氨酸氨基转移酶)(?!.*谷草).*$"),
    ("^(?=.*草转氨酶|.*冬氨酸氨基转移酶)(?!.*谷丙|.*同工).*$"),
    ("^(?=.*谷氨酰)(?=.*转).*$"),
    ("^(?=.*胆红素)(?=.*间接).*$"),
    ("^(?=.*血糖)(?=.*餐后).*$"),
    ("^(?=.*高血压|.*血压偏高)(?!.*视网膜).*$")
  )

  val MatchGanLabel = Array(
    ("^(?=.*谷氨酰).*$"),
    ("^(?=.*丙氨酸氨基转移酶).*$"),
    (".*乙肝.*"),
    (".*(冠心病).*"),
    ("^(?=.*天门冬氨酸氨基转移酶|.*天冬氨酸氨基转移酶|.*门冬氨酸氨基转移酶).*$"),
    ("^(?=.*尿素氮)(?!.*肌酐).*$"),
    ("^(?=.*尿蛋白).*$"),
    (".*(Q波异常|异常Q波).*"),
    (".*(ST段).*"),
    ("(.*T波.*)"),
    ("^.*心肌梗塞.*"),
    ("^(?=.*胆汁酸).*$"),
    ("^(?=.*戊)(?=.*肝).*$"),
    ("^(球蛋白){1}|(?=.*血清球蛋白|.*球蛋白测定).*|(?=.*球蛋白)(?=.*GL).*$"),
    ("^(?=.*胆红素)(?=.*直接).*$"),
    ("^(?=.*肌酐)(?!.*尿).*$"),
    ("(.*肝.{0,2}占位.*)"),
    ("(.*肝.{0,3}钙化.*)"),
    (".*(肝功能异常).*"),
    (".*(肝包虫).*"),
    ("(.*肝.{0,3}囊肿.*)"),
    (".*(肝.{0,4}回声).*"),
    ("(.*肝脏?弥漫性病变.*)"),
    (".*(肝硬化).*"),
    ("(.*肝脏增大.*)"),
    ("(.*肝.{0,3}血管瘤.*)"),
    (".*肺.{0,1}(罗|干啰|湿啰|哮鸣)音.*"),
    ("^(?=.*胆碱)(?=.*酶).*$"),
    ("(.*胆管扩张.*)"),
    ("(.*胆管结石.*)"),
    (".*胸腔.{0,3}积液.*"),
    ("^(?=.*甲)(?=.*肝)(?=.*IgM|.*IGM).*$"),
    ("^(?=.*甲胎蛋白).*$"),
    (".*脂肪肝.*"),
    (".*血吸虫病肝.*"),
    ("^(?=.*血小板)(?=.*数).*$"),
    ("^(?=.*尿酸)(?!.*碱|.*结晶).*$"),
    ("^(?=.*胆红素)(?=.*总).*$"),
    ("^(?=.*总蛋白).*$"),
    ("^(?=.*白-球|.*白/球|.*白球比|.*白蛋白/球蛋白).*$"),
    ("^(?=.*白蛋白)(?!.*前|.*尿|.*微|.*球|.*糖|.*缺).*$"),
    ("^(?=.*碱性磷酸酶).*$"),
    ("^(?=.*胆红素)(?=.*结合).*$"),
    ("^(?=.*胱抑素).*$"),
    ("^(?=.*丙转氨酶|.*丙氨酸氨基转移酶)(?!.*谷草).*$"),
    ("^(?=.*草转氨酶|.*冬氨酸氨基转移酶)(?!.*谷丙|.*同工).*$"),
    ("(.*血管瘤.*)"),
    ("^(?=.*血红蛋白)(?!.*平均|.*糖).*$"),
    ("^(?=.*谷氨酰)(?=.*转).*$"),
    ("^(?=.*胆红素)(?=.*间接).*$")


  )

  val MatchJZXLabel = Array(
    (".*甲.{0,4}亢.*"),
    (".*(甲状旁腺结节).*"),
    (".*(甲状腺.{0,3}占位).*"),
    (".*(甲状腺.{0,5}回声(欠|不|稍欠)均|甲状腺腺体回声增粗|甲状腺片状低回声区|甲状腺回声弥漫性改变|甲状腺囊性回声).*"),
    (".*(甲状腺.{0,3}弥漫性病变).*"),
    (".*(甲状腺炎).*"),
    ("(.*甲状腺.{0,10}结节.*)"),
    ("(.*甲状腺.{0,5}大.*)"),
    ("(.*甲状腺.{0,3}腺瘤.*)"),
    (".*(甲状腺.{0,5}钙化).*")


  )

  //单一label分类
  def classFication(label: String) = {
    //返回的结果字符串
    //肝返回1，甲状腺返回2，高血压返回3，糖尿病返回4,其它异常返回5,不存在此异常返回0
    var rs_builder = new StringBuilder


    //肝判断
    MatchGanLabel.foreach(regex => {

      if (label.matches(regex.toString)) {
        rs_builder.append(1 + "_")


        println(label)
      }
    })
    //高血压
    MatchGXYLabel.foreach(regex => {

      if (label.matches(regex.toString)) {
        rs_builder.append(3 + "_")
        println(label)
      }
    })
    //甲状腺
    MatchJZXLabel.foreach(regex => {

      if (label.matches(regex.toString)) {
        rs_builder.append(2 + "_")
        println(label)
      }
    })
    //糖尿病
    MatchTNBLabel.foreach(regex => {

      if (label.matches(regex.toString)) {
        rs_builder.append(4 + "_")
        println(label)
      }
    })
    //其它异常
    checkIndexRsFlagIdCommonRules.foreach(line1 => {
      if (label.matches(line1._1)) {
        rs_builder.append("5")
      }
    })
    checkIndexRsFlagIdSpecialRules.foreach(line2 => {
      if (label.matches(line2._1)) {
        rs_builder.append("5")
      }
    })
    summaryRuleArray.foreach(line3 => {
      if (label.matches(line3._1)) {
        rs_builder.append("5")
      }
    })
    summaryRuleArray.foreach(line4 => {
      if (label.matches(line4._2)) {
        rs_builder.append("5")
      }
    })
    //如果到这里还没有匹配到label,那么追加0
    if (rs_builder.size <= 0) {
      rs_builder.append("0")
    }


    //返回结果
    rs_builder
  }

  def main(args: Array[String]): Unit = {
    println(classFication("丙氨酸氨基转移酶"))
    println(classFication("糖尿病"))
    println(classFication("我是神经病"))

    val flag: String = classFication("丙氨酸氨基转移酶").toString()

    //    insuranceMap: InsuranceMap
    /*val gan = new PushGan(InsuranceMap)
        val rs: String = gan.PushGan(insuranceMap)*/
  }

  var gan_rs: String=""
  var jzx_rs: String=""
  var gaoxueya_rs: String=""
  var xuetang_rs: String=""
  def getInsurancePush(label: String, insuranceMap: InsuranceMap) = {
    import scala.collection.mutable._
    /*
      肝n,肝p,甲状腺n,甲状腺p,高血压n,高血压p,糖n,糖p,其它异常,不存在此异常
     */

    val labelResult: String = classFication(label).toString()
    val rs_Push = new StringBuilder
    val gan = new PushGan()
    gan_rs= gan.PushGan(insuranceMap)
    jzx_rs= MatchJzx.panduan(label)
    if(jzx_rs.contains("0")){
      jzx_rs="0"
    }
    val gaoxueya = new PushGaoxueya()
    gaoxueya_rs= gaoxueya.pushGaoxueya(insuranceMap)
    val gaoxuetang = new PushTangniaobing()
    xuetang_rs= gaoxuetang.Pushtangniaobing(insuranceMap)
   /* val reds = new RedisUtil()
    reds.set("sd","das")*/
    //肝判断
    if (labelResult.contains("1")) {
      if (gan_rs.equals("0")) {
        //肝不推
        rs_Push.append("肝n" + "_")
        println("肝不：" + rs_Push)
      }
      if (gan_rs.equals("1")) {
        //肝推
        rs_Push.append("肝p" + "_")
        println("肝：" + rs_Push)
      }
    }

    //甲状腺判断
    if (labelResult.contains("2")) {

      if (jzx_rs.contains("0")) {
        //甲状腺不推
        rs_Push.append("甲状腺n" + "_")
      }
      if (jzx_rs.equals("1")) {
        //甲状腺推
        rs_Push.append("甲状腺p" + "_")
      }
    }

    //高血压
    if (labelResult.contains("3")) {

      if (gaoxueya.equals("0")) {
        //高血压不推
        rs_Push.append("高血压n" + "_")
      }
      if (gaoxueya.equals("1")) {
        //高血压推
        rs_Push.append("高血压p" + "_")
      }
    }

    //高血糖
    if (labelResult.contains("4")) {

      if (xuetang_rs.equals("0")) {
        //糖尿病
        rs_Push.append("糖n" + "_")
      }
      if (xuetang_rs.equals("1")) {
        //糖尿病
        rs_Push.append("糖p" + "_")
      }
    }


    //redisUtil.set("111","111")
    //其它异常
    if (labelResult.contains("5")) {
      rs_Push.append("其它异常")

    }

    //不存在此label
    if (labelResult.contains("0")) {
      rs_Push.append("不存在此异常")


    }
    rs_Push.toString()
  }

  //判断后,进行优先级
  //  推送优先级：关爱肝  甲状腺  高血压  高血糖

  /*
  肝n,肝p,甲状腺n,甲状腺p,高血压n,高血压p,糖n,糖p,其它异常,不存在此异常
 */
  def result(label: String, insuranceMap: InsuranceMap) = {
    val rsString: String = getInsurancePush(label, insuranceMap)
    println(rsString)
    var rsValue: String = "-1"
    //肝优先级最高，肝如果在推送，那么忽视所有情况
    if (rsString.contains("肝p")) {
      rsValue = "g1"
    } else if (rsString.contains("甲状腺p")) {
      rsValue = "j1"
    } else if (rsString.contains("高血压p")) {
      rsValue = "x1"
    } else if (rsString.contains("糖p")) {
      rsValue = "t1"
    } else {
      rsValue = "0"
    }
    rsValue
  }

  //返回结果gan_rs _ rs _ rs_status _ xutang_rs
  def fourRs(): String ={
    var builder = new StringBuilder
    builder.append(gan_rs+"_"+jzx_rs+"_"+gaoxueya_rs+"_"+xuetang_rs).toString()
  }
}
