package com.haozhuo.datag.com.service.Insurance

import java.text.SimpleDateFormat
import java.util
import java.util.Date



object AgeFication {


  //推送
  //1.查询用户年龄
  //2.根据年龄段来分析
  //3.返回此用户推送的结果
//  8da2c8f9bc6da2aed91b81fd156d71b2
 /* def tuisong(reportId:String): String ={
      val strings: util.ArrayList[String] = connect("select bd from hive.dataetl.rpt_b where rpt_id="+"'"+reportId+"'","bd");
    val str: String = strings.get(0)
    println(str)
    str
  }*/

  //获取年龄
  def getAge(birthday:String):String={
    val objYear: String = birthday.split("-")(0)

    val now: String = new SimpleDateFormat("yyyy-MM-dd").format(new Date)
    println("当你年月日"+now)

    val age:String=(now.split("-")(0).toInt - objYear.toInt).toString

    age
  }

  val h20String:String="[\n{\"checkName\":\"一般检查\",\"checkList\":\"身高、体重、体重指数（BMI） 血压（BP）、脉搏（P）\"},\n{\"checkName\":\"内科\",\"checkList\":\"心、肺听诊，腹部触诊\"},\n{\"checkName\":\"外科\",\"checkList\":\"浅表淋巴结，甲状腺、乳房、脊柱、四肢、外生殖器、前列腺、肛肠指检、皮肤等\"},\n{\"checkName\":\"妇科（已婚项目）\",\"checkList\":\"常规检查\"},\n{\"checkName\":\"\",\"checkList\":\"白带常规\"},\n{\"checkName\":\"\",\"checkList\":\"TCT（液基博片细胞学检查）\"},\n{\"checkName\":\"\",\"checkList\":\"宫颈HPV-DNA检查\"},\n{\"checkName\":\"眼科\",\"checkList\":\"外眼\"},\n{\"checkName\":\"\",\"checkList\":\"视力\"},\n{\"checkName\":\"\",\"checkList\":\"眼底\"},\n{\"checkName\":\"\",\"checkList\":\"裂隙灯\"},\n{\"checkName\":\"耳鼻喉检查1\",\"checkList\":\"外耳道、鼓膜、鼻腔、鼻中隔、扁桃体、咽部\"},\n{\"checkName\":\"口腔科\",\"checkList\":\"口腔检查\"},\n{\"checkName\":\"静态心电图（ECG）\",\"checkList\":\"十二导心电图\"},\n{\"checkName\":\"血常规18项\",\"checkList\":\"检查白细胞、红细胞、血小板等\"},\n{\"checkName\":\"尿常规12项\",\"checkList\":\"颜色、比重、酸碱度、尿糖、隐血、尿胆素、尿胆原、胆红素、尿蛋白、亚硝酸盐、尿沉渣检查\"},\n{\"checkName\":\"肝功能11项\",\"checkList\":\"ALT，AST，GGT，ALP，总蛋白，白蛋白，球蛋白，白/球比值,总胆红素，直接胆红素，间接胆红素\"},\n{\"checkName\":\"肾功能3项\",\"checkList\":\"尿素氮（BUN）、肌酐（Cr）、尿酸（UA）\"},\n{\"checkName\":\"血脂\",\"checkList\":\"1.总胆固醇(TC)\"},\n{\"checkName\":\"\",\"checkList\":\"2.甘油三脂（TG）\"},\n{\"checkName\":\"\",\"checkList\":\"3.高密度脂蛋白(HDL)\"},\n{\"checkName\":\"\",\"checkList\":\"4.低密度脂蛋白(LDL)\"},\n{\"checkName\":\"\",\"checkList\":\"6.载脂蛋白A1\"},\n{\"checkName\":\"血糖\",\"checkList\":\"空腹血糖\"},\n{\"checkName\":\"蛋白芯片检测（C6）\",\"checkList\":\"CA19-9、CA-153、CA125、PSA、CEA、AFP\"},\n{\"checkName\":\"ＤＲ不出片\",\"checkList\":\"胸部正位检查\"},\n{\"checkName\":\"\",\"checkList\":\"颈椎侧位检查\"},\n{\"checkName\":\"高清彩色多普勒B超\",\"checkList\":\"肝胆脾胰肾\"},\n{\"checkName\":\"\",\"checkList\":\"甲状腺\"},\n{\"checkName\":\"\",\"checkList\":\"前列腺\"},\n{\"checkName\":\"\",\"checkList\":\"乳房（双侧）\"},\n{\"checkName\":\"\",\"checkList\":\"子宫及附件\"},\n{\"checkName\":\"\",\"checkList\":\"阴道（已婚项目）\"},\n{\"checkName\":\"甲状腺功能3项\",\"checkList\":\"T3、T4、TSH\"},\n{\"checkName\":\"幽门螺旋杆菌检测\",\"checkList\":\"（C14哈气）\"},\n{\"checkName\":\"营养早餐\",\"checkList\":\"\\\"1、各地免费早餐标准请见《异地订单各城市情况表》\n2、额外加早餐10元一份，只支持前台付现\"}\n]"
  val s20String:String="[\n{\"checkName\":\"一般检查\",\"checkList\":\"身高、体重、体重指数（BMI） 血压（BP）、脉搏（P）\"},\n{\"checkName\":\"内科\",\"checkList\":\"心、肺听诊，腹部触诊\"},\n{\"checkName\":\"外科\",\"checkList\":\"浅表淋巴结，甲状腺、乳房、脊柱、四肢、外生殖器、前列腺、肛肠指检、皮肤等\"},\n{\"checkName\":\"妇科（已婚项目）\",\"checkList\":\"常规检查\"},\n{\"checkName\":\"\",\"checkList\":\"白带常规\"},\n{\"checkName\":\"\",\"checkList\":\"TCT（液基博片细胞学检查）\"},\n{\"checkName\":\"\",\"checkList\":\"宫颈HPV-DNA检查\"},\n{\"checkName\":\"眼科\",\"checkList\":\"外眼\"},\n{\"checkName\":\"\",\"checkList\":\"视力\"},\n{\"checkName\":\"\",\"checkList\":\"眼底\"},\n{\"checkName\":\"\",\"checkList\":\"裂隙灯\"},\n{\"checkName\":\"耳鼻喉检查1\",\"checkList\":\"外耳道、鼓膜、鼻腔、鼻中隔、扁桃体、咽部\"},\n{\"checkName\":\"口腔科\",\"checkList\":\"口腔检查\"},\n{\"checkName\":\"静态心电图（ECG）\",\"checkList\":\"十二导心电图\"},\n{\"checkName\":\"血常规18项\",\"checkList\":\"检查白细胞、红细胞、血小板等\"},\n{\"checkName\":\"尿常规12项\",\"checkList\":\"颜色、比重、酸碱度、尿糖、隐血、尿胆素、尿胆原、胆红素、尿蛋白、亚硝酸盐、尿沉渣检查\"},\n{\"checkName\":\"肝功能11项\",\"checkList\":\"ALT，AST，GGT，ALP，总蛋白，白蛋白，球蛋白，白/球比值,总胆红素，直接胆红素，间接胆红素\"},\n{\"checkName\":\"肾功能3项\",\"checkList\":\"尿素氮（BUN）、肌酐（Cr）、尿酸（UA）\"},\n{\"checkName\":\"血脂\",\"checkList\":\"1.总胆固醇(TC)\"},\n{\"checkName\":\"\",\"checkList\":\"2.甘油三脂（TG）\"},\n{\"checkName\":\"\",\"checkList\":\"3.高密度脂蛋白(HDL)\"},\n{\"checkName\":\"\",\"checkList\":\"4.低密度脂蛋白(LDL)\"},\n{\"checkName\":\"\",\"checkList\":\"6.载脂蛋白A1\"},\n{\"checkName\":\"血糖\",\"checkList\":\"空腹血糖\"},\n{\"checkName\":\"蛋白芯片检测\",\"checkList\":\"（C12）（男）\"},\n{\"checkName\":\"蛋白芯片检测\",\"checkList\":\"（C12）（女）\"},\n{\"checkName\":\"ＤＲ不出片\",\"checkList\":\"胸部正位检查\"},\n{\"checkName\":\"\",\"checkList\":\"颈椎侧位检查\"},\n{\"checkName\":\"高清彩色多普勒B超\",\"checkList\":\"肝胆脾胰肾\"},\n{\"checkName\":\"\",\"checkList\":\"甲状腺\"},\n{\"checkName\":\"\",\"checkList\":\"前列腺\"},\n{\"checkName\":\"\",\"checkList\":\"心脏\"},\n{\"checkName\":\"\",\"checkList\":\"乳房（双侧）\"},\n{\"checkName\":\"\",\"checkList\":\"子宫及附件\"},\n{\"checkName\":\"\",\"checkList\":\"阴道（已婚项目）\"},\n{\"checkName\":\"甲状腺功能3项\",\"checkList\":\"T3、T4、TSH\"},\n{\"checkName\":\"优生优育筛查（套）\",\"checkList\":\"风疹病毒抗体定性IgG、IgM、弓形体抗体定性IgG、IgM、巨细胞病毒抗体定性IgG、IgM、单纯疱疹病毒Ⅰ型抗体定性IgG、IgM、单纯疱疹病毒Ⅱ型抗体定性IgG、IgM\"},\n{\"checkName\":\"男性性激素五项\",\"checkList\":\"雌二醇（E2）、促黄体生成素（LH）、促卵泡生成素（FSH）、垂体泌乳素（PRL）、睾酮（T）\"},\n{\"checkName\":\"女性性激素六项\",\"checkList\":\"雌二醇（E2）、促黄体生成素（LH）、促卵泡生成素（FSH）、孕酮(P)、垂体泌乳素（PRL）、睾酮（T）\"},\n{\"checkName\":\"幽门螺旋杆菌检测\",\"checkList\":\"（C14哈气）\"},\n{\"checkName\":\"营养早餐\",\"checkList\":\"\\\"1、各地免费早餐标准请见《异地订单各城市情况表》\n2、额外加早餐10元一份，只支持前台付现\"}\n]"
  val m20String:String="[\n{\"checkName\":\"一般检查\",\"checkList\":\"身高、体重、体重指数（BMI） 血压（BP）、脉搏（P）\"},\n{\"checkName\":\"内科\",\"checkList\":\"心、肺听诊，腹部触诊\"},\n{\"checkName\":\"外科\",\"checkList\":\"浅表淋巴结，甲状腺、乳房、脊柱、四肢、外生殖器、前列腺、肛肠指检、皮肤等\"},\n{\"checkName\":\"妇科（已婚项目）\",\"checkList\":\"常规检查\"},\n{\"checkName\":\"\",\"checkList\":\"白带常规\"},\n{\"checkName\":\"\",\"checkList\":\"TCT（液基博片细胞学检查）\"},\n{\"checkName\":\"眼科\",\"checkList\":\"外眼\"},\n{\"checkName\":\"\",\"checkList\":\"视力\"},\n{\"checkName\":\"耳鼻喉检查1\",\"checkList\":\"外耳道、鼓膜、鼻腔、鼻中隔、扁桃体、咽部\"},\n{\"checkName\":\"口腔科\",\"checkList\":\"口腔检查\"},\n{\"checkName\":\"静态心电图（ECG）\",\"checkList\":\"十二导心电图\"},\n{\"checkName\":\"血常规18项\",\"checkList\":\"检查白细胞、红细胞、血小板等\"},\n{\"checkName\":\"肝功能\",\"checkList\":\"丙氨酸氨基转氨酶（ALT）\"},\n{\"checkName\":\"\",\"checkList\":\"总胆红素(TBil)\"},\n{\"checkName\":\"\",\"checkList\":\"谷草转氨酶（AST）\"},\n{\"checkName\":\"\",\"checkList\":\"谷氨酰转肽酶(GGT)\"},\n{\"checkName\":\"肾功能3项\",\"checkList\":\"尿素氮（BUN）、肌酐（Cr）、尿酸（UA）\"},\n{\"checkName\":\"尿常规12项\",\"checkList\":\"颜色、比重、酸碱度、尿糖、隐血、尿胆素、尿胆原、胆红素、尿蛋白、亚硝酸盐、尿沉渣检查\"},\n{\"checkName\":\"血糖\",\"checkList\":\"空腹血糖\"},\n{\"checkName\":\"ＤＲ不出片\",\"checkList\":\"胸部正位检查\"},\n{\"checkName\":\"\",\"checkList\":\"颈椎侧位检查\"},\n{\"checkName\":\"血脂\",\"checkList\":\"1.总胆固醇(TC)\"},\n{\"checkName\":\"\",\"checkList\":\"2.甘油三脂（TG）\"},\n{\"checkName\":\"肿瘤标志检测\",\"checkList\":\"甲胎蛋白（AFP）定性\"},\n{\"checkName\":\"\",\"checkList\":\"癌胚抗原（CEA）定性\"},\n{\"checkName\":\"高清彩色多普勒B超\",\"checkList\":\"肝胆脾胰肾\"},\n{\"checkName\":\"\",\"checkList\":\"甲状腺\"},\n{\"checkName\":\"\",\"checkList\":\"前列腺\"},\n{\"checkName\":\"\",\"checkList\":\"乳房（双侧）\"},\n{\"checkName\":\"幽门螺旋杆菌检测\",\"checkList\":\"（血液）\"},\n{\"checkName\":\"营养早餐\",\"checkList\":\"1、各地免费早餐标准请见《异地订单各城市情况表》2、额外加早餐10元一份，只支持前台付现\"}\n]"

  val h30String:String="[\n{\"checkName\":\"一般检查\",\"checkList\":\"身高、体重、体重指数（BMI） 血压（BP）、脉搏（P）\"},\n{\"checkName\":\"内科\",\"checkList\":\"心、肺听诊，腹部触诊\"},\n{\"checkName\":\"外科\",\"checkList\":\"浅表淋巴结，甲状腺、乳房、脊柱、四肢、外生殖器、前列腺、肛肠指检、皮肤等\"},\n{\"checkName\":\"妇科（已婚项目）\",\"checkList\":\"常规检查\"},\n{\"checkName\":\"\",\"checkList\":\"白带常规\"},\n{\"checkName\":\"\",\"checkList\":\"TCT（液基博片细胞学检查）\"},\n{\"checkName\":\"\",\"checkList\":\"宫颈HPV-DNA检查\"},\n{\"checkName\":\"眼科\",\"checkList\":\"外眼\"},\n{\"checkName\":\"\",\"checkList\":\"视力\"},\n{\"checkName\":\"\",\"checkList\":\"眼底\"},\n{\"checkName\":\"\",\"checkList\":\"裂隙灯\"},\n{\"checkName\":\"耳鼻喉检查1\",\"checkList\":\"外耳道、鼓膜、鼻腔、鼻中隔、扁桃体、咽部\"},\n{\"checkName\":\"音叉测听\",\"checkList\":\"\"},\n{\"checkName\":\"口腔科\",\"checkList\":\"口腔检查\"},\n{\"checkName\":\"静态心电图（ECG）\",\"checkList\":\"十二导心电图\"},\n{\"checkName\":\"血常规18项\",\"checkList\":\"检查白细胞、红细胞、血小板等\"},\n{\"checkName\":\"尿常规12项\",\"checkList\":\"颜色、比重、酸碱度、尿糖、隐血、尿胆素、尿胆原、胆红素、尿蛋白、亚硝酸盐、尿沉渣检查\"},\n{\"checkName\":\"肝功能11项\",\"checkList\":\"ALT，AST，GGT，ALP，总蛋白，白蛋白，球蛋白，白/球比值,总胆红素，直接胆红素，间接胆红素\"},\n{\"checkName\":\"肺功能检查\",\"checkList\":\"\"},\n{\"checkName\":\"肾功能3项\",\"checkList\":\"尿素氮（BUN）、肌酐（Cr）、尿酸（UA）\"},\n{\"checkName\":\"血脂\",\"checkList\":\"1.总胆固醇(TC)\"},\n{\"checkName\":\"\",\"checkList\":\"2.甘油三脂（TG）\"},\n{\"checkName\":\"\",\"checkList\":\"3.高密度脂蛋白(HDL)\"},\n{\"checkName\":\"\",\"checkList\":\"4.低密度脂蛋白(LDL)\"},\n{\"checkName\":\"\",\"checkList\":\"6.载脂蛋白A1\"},\n{\"checkName\":\"血糖\",\"checkList\":\"空腹血糖\"},\n{\"checkName\":\"心肌酶三项（CK、LDH、α-HBDH）\",\"checkList\":\"\"},\n{\"checkName\":\"蛋白芯片检测\",\"checkList\":\"（C12）（男）\"},\n{\"checkName\":\"蛋白芯片检测\",\"checkList\":\"（C12）（女）\"},\n{\"checkName\":\"风湿、类风湿疾病\",\"checkList\":\"血沉(ESR)\"},\n{\"checkName\":\"\",\"checkList\":\"类风湿因子（RF）\"},\n{\"checkName\":\"ＤＲ不出片\",\"checkList\":\"胸部正位检查\"},\n{\"checkName\":\"\",\"checkList\":\"颈椎侧位检查\"},\n{\"checkName\":\"高清彩色多普勒B超\",\"checkList\":\"肝胆脾胰肾\"},\n{\"checkName\":\"\",\"checkList\":\"甲状腺\"},\n{\"checkName\":\"\",\"checkList\":\"前列腺\"},\n{\"checkName\":\"\",\"checkList\":\"心脏\"},\n{\"checkName\":\"\",\"checkList\":\"乳房（双侧）\"},\n{\"checkName\":\"\",\"checkList\":\"子宫及附件\"},\n{\"checkName\":\"\",\"checkList\":\"阴道（已婚项目）\"},\n{\"checkName\":\"甲状腺功能3项\",\"checkList\":\"T3、T4、TSH\"},\n{\"checkName\":\"幽门螺旋杆菌检测\",\"checkList\":\"（C14哈气）\"},\n{\"checkName\":\"营养早餐\",\"checkList\":\"1、各地免费早餐标准请见《异地订单各城市情况表》2、额外加早餐10元一份，只支持前台付现\"}\n]"
  val m30String:String="[\n{\"checkName\":\"一般检查\",\"checkList\":\"身高、体重、体重指数（BMI） 血压（BP）、脉搏（P）\"},\n{\"checkName\":\"内科\",\"checkList\":\"心、肺听诊，腹部触诊\"},\n{\"checkName\":\"外科\",\"checkList\":\"浅表淋巴结，甲状腺、乳房、脊柱、四肢、外生殖器、前列腺、肛肠指检、皮肤等\"},\n{\"checkName\":\"妇科（已婚项目）\",\"checkList\":\"常规检查\"},\n{\"checkName\":\"\",\"checkList\":\"白带常规\"},\n{\"checkName\":\"\",\"checkList\":\"宫颈刮片\"},\n{\"checkName\":\"眼科\",\"checkList\":\"外眼\"},\n{\"checkName\":\"\",\"checkList\":\"视力\"},\n{\"checkName\":\"耳鼻喉检查1\",\"checkList\":\"外耳道、鼓膜、鼻腔、鼻中隔、扁桃体、咽部\"},\n{\"checkName\":\"口腔科\",\"checkList\":\"口腔检查\"},\n{\"checkName\":\"静态心电图（ECG）\",\"checkList\":\"十二导心电图\"},\n{\"checkName\":\"血常规18项\",\"checkList\":\"检查白细胞、红细胞、血小板等\"},\n{\"checkName\":\"肝功能\",\"checkList\":\"丙氨酸氨基转氨酶（ALT）\"},\n{\"checkName\":\"\",\"checkList\":\"总胆红素(TBil)\"},\n{\"checkName\":\"\",\"checkList\":\"谷草转氨酶（AST）\"},\n{\"checkName\":\"\",\"checkList\":\"谷氨酰转肽酶(GGT)\"},\n{\"checkName\":\"肾功能3项\",\"checkList\":\"尿素氮（BUN）、肌酐（Cr）、尿酸（UA）\"},\n{\"checkName\":\"尿常规12项\",\"checkList\":\"颜色、比重、酸碱度、尿糖、隐血、尿胆素、尿胆原、胆红素、尿蛋白、亚硝酸盐、尿沉渣检查\"},\n{\"checkName\":\"血糖\",\"checkList\":\"空腹血糖\"},\n{\"checkName\":\"血流变\",\"checkList\":\"血粘度检测\"},\n{\"checkName\":\"ＤＲ不出片\",\"checkList\":\"胸部正位检查\"},\n{\"checkName\":\"\",\"checkList\":\"颈椎侧位检查\"},\n{\"checkName\":\"血脂\",\"checkList\":\"1.总胆固醇(TC)\"},\n{\"checkName\":\"\",\"checkList\":\"2.甘油三脂（TG）\"},\n{\"checkName\":\"蛋白芯片检测（C6）\",\"checkList\":\"CA19-9、CA-153、CA125、PSA、CEA、AFP\"},\n{\"checkName\":\"\",\"checkList\":\"类风湿因子（RF）\"},\n{\"checkName\":\"高清彩色多普勒B超\",\"checkList\":\"肝胆脾胰肾\"},\n{\"checkName\":\"\",\"checkList\":\"甲状腺\"},\n{\"checkName\":\"\",\"checkList\":\"前列腺\"},\n{\"checkName\":\"\",\"checkList\":\"乳房（双侧）\"},\n{\"checkName\":\"\",\"checkList\":\"子宫及附件\"},\n{\"checkName\":\"\",\"checkList\":\"阴道（已婚项目）\"},\n{\"checkName\":\"幽门螺旋杆菌检测\",\"checkList\":\"（血液）\"},\n{\"checkName\":\"营养早餐\",\"checkList\":\"1、各地免费早餐标准请见《异地订单各城市情况表》2、额外加早餐10元一份，只支持前台付现\"}\n]"

  val h40String:String="[\n{\"checkName\":\"一般检查\",\"checkList\":\"身高、体重、体重指数（BMI） 血压（BP）、脉搏（P）\"},\n{\"checkName\":\"内科\",\"checkList\":\"心、肺听诊，腹部触诊\"},\n{\"checkName\":\"外科\",\"checkList\":\"浅表淋巴结，甲状腺、乳房、脊柱、四肢、外生殖器、前列腺、肛肠指检、皮肤等\"},\n{\"checkName\":\"妇科（已婚项目）\",\"checkList\":\"常规检查\"},\n{\"checkName\":\"\",\"checkList\":\"白带常规\"},\n{\"checkName\":\"\",\"checkList\":\"TCT\"},\n{\"checkName\":\"眼科\",\"checkList\":\"视力\"},\n{\"checkName\":\"\",\"checkList\":\"辨色力\"},\n{\"checkName\":\"\",\"checkList\":\"外眼、眼底、裂隙灯\"},\n{\"checkName\":\"耳鼻喉检查1\",\"checkList\":\"外耳道、鼓膜、鼻腔、鼻中隔、扁桃体、咽部\"},\n{\"checkName\":\"口腔科\",\"checkList\":\"口腔检查\"},\n{\"checkName\":\"音叉测听\",\"checkList\":\"\"},\n{\"checkName\":\"静态心电图（ECG）\",\"checkList\":\"十二导心电图\"},\n{\"checkName\":\"血常规18项\",\"checkList\":\"检查白细胞、红细胞、血小板等\"},\n{\"checkName\":\"肝功能11项\",\"checkList\":\"ALT，AST，GGT，ALP，总蛋白，白蛋白，球蛋白，白/球比值,总胆红素，直接胆红素，间接胆红素\"},\n{\"checkName\":\"肾功能3项\",\"checkList\":\"尿素氮（BUN）、肌酐（Cr）、尿酸（UA）\"},\n{\"checkName\":\"尿常规12项\",\"checkList\":\"颜色、比重、酸碱度、尿糖、隐血、尿胆素、尿胆原、胆红素、尿蛋白、亚硝酸盐、尿沉渣检查\"},\n{\"checkName\":\"血糖\",\"checkList\":\"空腹血糖\"},\n{\"checkName\":\"糖化血红蛋白（HbA1C）测定\",\"checkList\":\"\"},\n{\"checkName\":\"血流变\",\"checkList\":\"血粘度检测\"},\n{\"checkName\":\"ＤＲ不出片\",\"checkList\":\"胸部正位检查\"},\n{\"checkName\":\"\",\"checkList\":\"腰椎侧位检查\"},\n{\"checkName\":\"\",\"checkList\":\"颈椎侧位检查\"},\n{\"checkName\":\"血脂\",\"checkList\":\"1.总胆固醇(TC)\"},\n{\"checkName\":\"\",\"checkList\":\"2.甘油三脂（TG）\"},\n{\"checkName\":\"\",\"checkList\":\"3.高密度脂蛋白(HDL)\"},\n{\"checkName\":\"\",\"checkList\":\"4.低密度脂蛋白(LDL)\"},\n{\"checkName\":\"\",\"checkList\":\"6.载脂蛋白A1\"},\n{\"checkName\":\"经颅多谱勒(TCD)\",\"checkList\":\"\"},\n{\"checkName\":\"蛋白芯片检测\",\"checkList\":\"（C12）（男）\"},\n{\"checkName\":\"蛋白芯片检测\",\"checkList\":\"（C12）（女）\"},\n{\"checkName\":\"风湿、类风湿疾病\",\"checkList\":\"血沉(ESR)\"},\n{\"checkName\":\"\",\"checkList\":\"超敏C反应蛋白（hs-CRP）\"},\n{\"checkName\":\"\",\"checkList\":\"类风湿因子（RF）\"},\n{\"checkName\":\"高清彩色多普勒B超\",\"checkList\":\"肝胆脾胰肾\"},\n{\"checkName\":\"\",\"checkList\":\"甲状腺\"},\n{\"checkName\":\"\",\"checkList\":\"前列腺\"},\n{\"checkName\":\"\",\"checkList\":\"心脏\"},\n{\"checkName\":\"\",\"checkList\":\"乳房（双侧）\"},\n{\"checkName\":\"骨密度检测\",\"checkList\":\"\"},\n{\"checkName\":\"动脉硬化检测\",\"checkList\":\"\"},\n{\"checkName\":\"幽门螺旋杆菌检测\",\"checkList\":\"（血液）\"},\n{\"checkName\":\"营养早餐\",\"checkList\":\"1、各地免费早餐标准请见《异地订单各城市情况表》2、额外加早餐10元一份，只支持前台付现\"}\n]"
  val m40String:String="[\n{\"checkName\":\"一般检查\",\"checkList\":\"身高、体重、体重指数（BMI） 血压（BP）、脉搏（P）\"},\n{\"checkName\":\"内科\",\"checkList\":\"心、肺听诊，腹部触诊\"},\n{\"checkName\":\"外科\",\"checkList\":\"浅表淋巴结，甲状腺、乳房、脊柱、四肢、外生殖器、前列腺、肛肠指检、皮肤等\"},\n{\"checkName\":\"妇科（已婚项目）\",\"checkList\":\"常规检查\"},\n{\"checkName\":\"\",\"checkList\":\"白带常规\"},\n{\"checkName\":\"\",\"checkList\":\"TCT\"},\n{\"checkName\":\"眼科\",\"checkList\":\"视力\"},\n{\"checkName\":\"\",\"checkList\":\"辨色力\"},\n{\"checkName\":\"\",\"checkList\":\"外眼、眼底、裂隙灯\"},\n{\"checkName\":\"耳鼻喉检查1\",\"checkList\":\"外耳道、鼓膜、鼻腔、鼻中隔、扁桃体、咽部\"},\n{\"checkName\":\"口腔科\",\"checkList\":\"口腔检查\"},\n{\"checkName\":\"音叉测听\",\"checkList\":\"\"},\n{\"checkName\":\"静态心电图（ECG）\",\"checkList\":\"十二导心电图\"},\n{\"checkName\":\"血常规18项\",\"checkList\":\"检查白细胞、红细胞、血小板等\"},\n{\"checkName\":\"肝功能11项\",\"checkList\":\"ALT，AST，GGT，ALP，总蛋白，白蛋白，球蛋白，白/球比值,总胆红素，直接胆红素，间接胆红素\"},\n{\"checkName\":\"肾功能3项\",\"checkList\":\"尿素氮（BUN）、肌酐（Cr）、尿酸（UA）\"},\n{\"checkName\":\"尿常规12项\",\"checkList\":\"颜色、比重、酸碱度、尿糖、隐血、尿胆素、尿胆原、胆红素、尿蛋白、亚硝酸盐、尿沉渣检查\"},\n{\"checkName\":\"血糖\",\"checkList\":\"空腹血糖\"},\n{\"checkName\":\"糖化血红蛋白（HbA1C）测定\",\"checkList\":\"\"},\n{\"checkName\":\"血流变\",\"checkList\":\"血粘度检测\"},\n{\"checkName\":\"ＤＲ不出片\",\"checkList\":\"胸部正位检查\"},\n{\"checkName\":\"\",\"checkList\":\"腰椎侧位检查\"},\n{\"checkName\":\"\",\"checkList\":\"颈椎侧位检查\"},\n{\"checkName\":\"血脂\",\"checkList\":\"1.总胆固醇(TC)\"},\n{\"checkName\":\"\",\"checkList\":\"2.甘油三脂（TG）\"},\n{\"checkName\":\"\",\"checkList\":\"3.高密度脂蛋白(HDL)\"},\n{\"checkName\":\"\",\"checkList\":\"4.低密度脂蛋白(LDL)\"},\n{\"checkName\":\"\",\"checkList\":\"6.载脂蛋白A1\"},\n{\"checkName\":\"经颅多谱勒(TCD)\",\"checkList\":\"\"},\n{\"checkName\":\"蛋白芯片检测\",\"checkList\":\"（C12）（男）\"},\n{\"checkName\":\"蛋白芯片检测\",\"checkList\":\"（C12）（女）\"},\n{\"checkName\":\"风湿、类风湿疾病\",\"checkList\":\"血沉(ESR)\"},\n{\"checkName\":\"\",\"checkList\":\"超敏C反应蛋白（hs-CRP）\"},\n{\"checkName\":\"\",\"checkList\":\"类风湿因子（RF）\"},\n{\"checkName\":\"高清彩色多普勒B超\",\"checkList\":\"肝胆脾胰肾\"},\n{\"checkName\":\"\",\"checkList\":\"甲状腺\"},\n{\"checkName\":\"\",\"checkList\":\"前列腺\"},\n{\"checkName\":\"\",\"checkList\":\"心脏\"},\n{\"checkName\":\"\",\"checkList\":\"乳房（双侧）\"},\n{\"checkName\":\"骨密度检测\",\"checkList\":\"\"},\n{\"checkName\":\"动脉硬化检测\",\"checkList\":\"\"},\n{\"checkName\":\"幽门螺旋杆菌检测\",\"checkList\":\"（血液）\"},\n{\"checkName\":\"营养早餐\",\"checkList\":\"1、各地免费早餐标准请见《异地订单各城市情况表》2、额外加早餐10元一份，只支持前台付现\"}\n]"
  //20岁的超高保险推送
  def s20(labels:String)={
    //要知道别人要怀孕才可以，做了怀孕系列的检查?搜索，点击孕妇用品?
    //要知道别人，没力气，或者肌肉，生理特征反常，才知道是激素的问题
    //要知道别人，是否掉头发，姓生活是否满意，是否含乳
    val labelArray = Array(
      ("优生优育筛查", ".*(风疹|IGG|IgM|弓形体|巨细胞|疱疹).*"),
      ("男性性激素五项", ".*(雌二醇|生成素|泌乳素|睾酮).*"),
      ("女性性激素六项", ".*(雌二醇|生成素|泌乳素|睾酮|孕酮).*")
    )
    var flags=false;

    labels.split(",").foreach(label=>{
      labelArray.foreach(line=>{
        if(label.matches(line._2)){
          flags=true
        }
      })
    })

    flags
  }
  //20岁的高保险推送
  def h20(labels:String)={
    //要知道别人要怀孕才可以，做了怀孕系列的检查?搜索，点击孕妇用品?
    //要知道别人，没力气，或者肌肉，生理特征反常，才知道是激素的问题
    //要知道别人，是否掉头发，姓生活是否满意，是否含乳
    val labelArray = Array(
      ("妇科(已婚项目)",".*(HPV-DNA|TCT).*"),
      ("眼科",".*(眼底|裂隙灯).*"),
      ("血脂",".*(密度脂蛋白|载脂蛋白).*"),
      ("蛋白芯片检测",".*(CA153|CA199|CA125|PSA|CEA|AFP).*"),
      ("高清彩色多普勒B超",".*(附件|子宫|阴道).*"),
      ("甲状腺功能3项",".*(T3|T4|TSH).*"),
      ("幽门螺旋杆菌检测",".*(C14哈气).*")
    )
    var flagh=false;

    labels.split(",").foreach(label=>{
      labelArray.foreach(line=>{
        if(label.matches(line._2)){
          flagh=true
        }
      })
    })
    flagh
  }
  //如果都不在，那就推中的

  //30岁的高保险推送
  def h30(labels:String)={
    val labelArray =Array(
      ("妇科（已婚项目）",".*(TCT|宫颈刮片巴式Ⅱ级|宫颈纳氏囊肿|宫颈刮片巴式Ⅲ级|宫颈刮片巴式Ⅳ级|宫颈刮片巴式Ⅴ级).*"),
      ("眼科",".*(眼底出血|豹纹状眼底|眼底窥视不清|黄斑病变|视网膜动脉硬化|角膜|晶状体|白内障).*"),
      ("音叉测听",".*(听力减退|神经性耳聋).*"),
      ("肝功能11项",".*(脂蛋白aLP(a)增高|总蛋白|白蛋白|球蛋白|白/球比值|直接胆红素|间接胆红素|戊型肝炎病毒抗体阳性|胆囊炎).*"),
      ("糖化血红蛋白（HbA1C）测定",".*血糖.*"),
      ("DR不出片",".*(腰椎).*"),
      ("血脂",".*(载脂蛋白A1|动脉硬化|冠心病|密度脂蛋白|血管硬化|心肌梗塞).*"),
      ("心肌酶三项（CK、LDH、α-HBDH）",".*(肌酸激酶|乳酸脱氢酶|羟丁酸脱氢酶).*"),
      ("高清彩色多普勒B超",".*(心).*"),
      ("蛋白芯片检测",".*(甲胎蛋白|癌胚|CA19|CA125|CA15).*"),
      ("风湿、类风湿疾病",".*(血沉|类风湿因子|风湿).*"),
      ("甲状腺功能3项",".*(T3|T4|TSH).*"),
      ("幽门螺旋杆菌检测",".*(C14哈气).*")
    )
    var flagh=false;

    labels.split(",").foreach(label=>{
      labelArray.foreach(line=>{
        if(label.matches(line._2)){
          println("我匹配上了"+label+"=>"+line._2)
          flagh=true
        }
      })
    })
    flagh

  }

  //45岁
  def h40(labels:String)={
    val labelArray =Array(
      ("妇科（已婚项目）",".*(TCT|宫颈刮片巴式Ⅱ级|宫颈纳氏囊肿|宫颈刮片巴式Ⅲ级|宫颈刮片巴式Ⅳ级|宫颈刮片巴式Ⅴ级).*"),
      ("眼科",".*(眼底出血|豹纹状眼底|眼底窥视不清|黄斑病变|视网膜动脉硬化|角膜|晶状体|白内障).*"),
      ("音叉测听",".*(听力减退|神经性耳聋).*"),
      ("肝功能11项",".*(脂蛋白aLP(a)增高|总蛋白|白蛋白|球蛋白|白/球比值|直接胆红素|间接胆红素|戊型肝炎病毒抗体阳性|胆囊炎).*"),
      ("糖化血红蛋白（HbA1C）测定",".*血糖.*"),
      ("DR不出片",".*(腰椎).*"),
      ("血脂",".*(载脂蛋白A1).*"),
      ("风湿、类风湿疾病",".*(血沉|类风湿因子|风湿).*")
    )
    var flagh=false;

    labels.split(",").foreach(label=>{
      labelArray.foreach(line=>{
        if(label.matches(line._2)){
//          logger("我匹配上了"+label+"=>"+line._2)
          flagh=true
        }
      })
    })
    flagh
  }


  def result(labels:String,brithday:String):String= {
    val age: Int = getAge(brithday).toInt
    print(age)
    if (20 <= age && age < 30) {
      println("对20-30岁的用户进行解析")
      //flagS super超级高
      val flagS: Boolean = s20(labels)
      val flagH: Boolean = h20(labels)
      if (flagS) {
        return s20String
      } else if (flagH) {
        return h20String
      } else {
        return m20String
      }


    } else if (30 <= age && age <= 45) {
      println("对30-45岁的用户进行解析")
      val flagH: Boolean = h30(labels)
      if (flagH) {
        return h30String
      } else {
        return m30String
      }
    } else if (45 < age) {
      println("对45岁以上的用户进行解析")
      val flagH: Boolean = h40(labels)
      if (flagH) {
        return h40String
      } else {
        return m40String
      }
    } else {
      return "小于20岁,没有保险推送,或者年龄不合法"
    }


  }
}
