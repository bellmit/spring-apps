package com.haozhuo.datag.com.service.Insurance

import scala.collection.mutable._
import scala.util.matching.Regex

/*
匹配的主函数入口
先通过非数值匹配，匹配到了返回0(含有重大异常)，否则返回1
返回1后执行数值匹配

数值匹配：匹配含有异常项,提取数据

*/


object matchMain {
  //先调用这个，匹配玩非数值的，在调用数值匹配,错误返回0并且+_vue值，正确返回1

  /**非数值匹配
    如果需要修改，在正则中添加需要匹配的内容，如果内容比较复杂，请勿在正则中添加
    请参照其他的方法，使用if(container)添加
    如果想更精准的匹配请调用修改paichu_string方法(添加后，对其进行排除 （患病状态排除) )
  **/
  def noNumMatch(rs_val:String) ={
    val regex = new Regex(
      """
        |心律失常|
        |呼吸(音|声)消失|呼吸(音|声)减弱|
        |重.*贫血貌|贫血貌.*重|
        |重度水肿|
        |急腹症|
        |急性.{0,10}青光眼|
        |(视网膜动脉硬化.{0,10}IV|IV.{0,10}视网膜动脉硬化|视网膜动脉硬化.{0,10}四.{0,2}期|视网膜动脉硬化.{0,10}4.{0,2}期)|
        |突发.{0,3}视力下降|
        |流行性.{0,10}出血性结膜炎|
        |喉头水肿|
        |活动性鼻出血|
        |大量气胸|
        |急性气胸|
        |张力性气胸|
        |液气胸|
        |脑疝|
        |脑.{0,10}出血|
        |硬膜下血肿.{0,10}急性|急性.{0,10}硬膜下血肿|
        |硬膜外血肿.{0,10}急性|急性.{0,10}硬膜外血肿|
        |严重.{0,5}颅脑挫裂伤|颅脑挫裂伤.{0,10}重|
        |蛛网膜下腔出血.{0,10}急性|蛛网膜下腔出血|
        |急性脑积水|
        |颅内.{0,10}急性.{0,10}大面积脑梗死|
        |严重.{0,5}脊柱损伤|脊柱损伤.{0,5}重|
        |(肺栓塞|肺梗死)|
        |主动脉夹层|
        |食道.{0,10}异物|
        |消化道.{0,20}穿孔|
        |绞窄性.{0,3}肠梗阻|
        |急性.{0,3}胆道梗阻|
        |急性.{0,10}坏死性胰腺炎|
        |多发复合伤.{0,40}破裂|.*破裂.*多发复合伤|
        |外伤.{0,15}出血.{0,20}腹腔积液|
        |急性.*胆囊炎.*胆囊化脓.*穿孔|
        |胆囊颈部结石.*嵌顿|
        |(肝硬化|腹膜炎|肿瘤).*腹腔积液|
        |卵巢(囊肿|肿瘤).*蒂扭转|卵.*蒂扭转|
        |卵巢(囊肿|肿瘤).*破裂|
        |黄体.{0,5}破裂|
        |大量心包积液.*心包填塞|心包积液.*大量.*心包填塞|
        |心.*(游离血栓|血栓游离)|
        |腹主动脉瘤|
        |下肢静脉.*(游离血栓|血栓游离)|
        |(急性|亚急性).*心肌梗|心肌梗.*(急性|亚急性)|
        |急性.*心肌缺血|心肌缺血.*急性|
        |心室扑动|心室颤动|
        |尖端扭转.{0,5}室性心动过速|室性心动过速.{0,5}尖端扭转|
        |多形性.{0,5}室性心动过速|室性心动过速.{0,5}多形性|
        |双向性.{0,5}室性心动过速|室性心动过速.{0,5}双向性|
        |严重.{0,5}低钾血症|低钾血症.{0,5}严重|
        |严重.{0,5}高钾血症|高钾血症.{0,5}严重|
        |急性.{0,5}肺栓塞|肺栓塞.{0,5}急性|
        |显性.*T波电交替|
        |多源性.{0,3}室性早搏|R.{0,3}T.{0,3}室性早搏|
        |心肌炎|
        |心脏起搏器.*(感知功能障碍|感知功能不良)|
        |消化道.*活动性出血|
        |肝脾表面.{0,3}不平.|肝脾.{0,10}结节|
        |肝脾.{0,10}重度肿大|肝脾.{0,10}中度肿大|肝脾.{0,10}中度|肝脾.{0,10}重度|
        |.{0,5}黄疸|
        |巨脾|
        |皮肤.*恶性肿瘤|恶性肿瘤.*皮肤|皮肤癌|
        |恶性淋巴|转移性淋巴结肿大|
        |甲状腺癌|甲状腺.*恶性肿瘤|
        |乳腺.{0,10}癌|乳腺.*恶性肿瘤|
        |肛管.{0,10}癌|肛管.*恶性肿瘤|
        |直肠.{0,10}癌|直肠.*恶性肿瘤|
        |前列腺.{0,10}癌|前列腺.*恶性肿瘤|
        |睾丸.*恶性肿瘤|睾丸癌|
        |阴茎.{0,10}癌|阴茎.*恶性肿瘤|
        |急性虹膜睫状体炎|
        |眶内肿瘤|眼眶.*肿瘤.{0,10}|
        |急性视神经炎|
        |急性.*缺血性视网膜病变|缺血性视网膜病变.*急性|
        |视网膜脱.*|
        |黄斑裂孔|
        |视网膜动脉硬化.{0,10}III.{0,3}期|视网膜动脉硬化.{0,10}3.{0,3}期|视网膜动脉硬化.{0,10}三.{0,3}期|
        |视乳头水肿|
        |角膜炎|
        |耳聋|
        |鼻咽.{0,10}癌|
        |急性.{0,3}会厌炎|声门上喉炎|会厌前咽峡炎|
        |喉.*癌|
        |回缩性涕血|
        |经久不愈.*慢性溃疡|慢性溃疡.*经久不愈|
        |天疱疮|
        |恶性肉芽肿|.{0,10}肉芽包.{0,10}恶性|
        |外周血.*幼稚细胞|
        |白细胞分类.*严重异常|白细胞分类.*异常.*重|
        |鳞状.{0,10}细胞癌|
        |不典型腺上皮细胞|非典型腺上皮细胞|
        |腺原位癌|
        |腺癌|
        |肿瘤标志物.*阳性|
        |肺.{0,3}结节.{0,20}恶性.{0,10}|恶性.{0,10}肺.{0,3}结节|肺.{0,20}恶性.*肺结节|
        |肺不张|
        |肺.{0,10}空洞|
        |心脏.*明显增大|心脏.*增大明显|
        |心包积液|
        |乳腺肿块|乳腺.{0,10}囊肿|
        |颅内占位性病变|
        |颅.*血管.{0,2}(狭窄|闭塞)|脑.*血管.{0,2}(狭窄|闭塞)|
        |颅内动脉瘤|
        |{0,5}.脑梗.{0,5}|
        |大片.{0,5}肺实变|肺.{0,30}渗出性改变|
        |肝.*恶性肿瘤|
        |胆管囊腺瘤|
        |肝脏囊肿合并.{0,5}(感染|出血)|
        |胆囊胆管.*高度恶性病变|
        |(无蒂性|广基).*病变胆囊息肉|
        |胆囊息肉病变.*(变宽|增大)|
        |胆囊息肉.*病灶.*周围黏膜.*(浸润|增厚)|
        |胆囊占位.*恶性病变|恶性病变.*胆囊占位|
        |恶性病变.*胰腺.*占位|胰腺.*占位.*恶性病变|
        |恶性病变.*脾脏.*占位|脾脏.*占位.*恶性病变|
        |(肝|脾).*(中度|重度)肿大|
        |肝外.{0,5}梗阻|
        |膀胱.*占位性病变.*恶.{0,3}性|膀胱.*恶.{0,3}性.*占位性病变|
        |前列腺.*占位性病变.*恶.{0,3}性|前列腺.*.恶.{0,3}性.*占位性病变|
        |子宫.*占位性病变.*恶.{0,3}性|子宫.*恶.{0,3}性.*占位性病变|
        |卵巢.*占位性病变.*恶.{0,3}性|卵巢.*恶.{0,3}性.*占位性病变|
        |肾.*占位性病变.*恶.{0,3}性|肾.*恶.{0,3}性.*占位性病变|
        |卵巢畸胎瘤|
        |动脉硬化.*斑块.*狭窄|
        |颈.{0,3}动脉.*闭塞|
        |动脉瘤|
        |甲状腺恶性肿瘤|
        |乳腺恶性肿瘤|
        |甲状腺实性结节.*沙砾样钙化|
        |乳腺实性结节.*沙砾样钙化|
        |严重.*瓣膜病|瓣膜病.*重|
        |左房粘液瘤|
        |严重.*先天性心脏病|先天性心脏病.*重|
        |心脏肿瘤|
        |(中度|重度).*(不|非)典型增生|
        |重度肠化|
        |(中|重)度.*黏膜.*萎缩|
        |腺瘤.{0,10}RADS.*(III|3)|
      """
    )




  val Regex=regex.findAllIn(rs_val)

    //遍历集合
    var vue="1"


    while (Regex.hasNext){
/*      //排除未见占位性病变
      val paichu_vue=Regex.next()
      val Regex_1 =new Regex(
        """
          |.{0,30}性病变|
        """)
      val bianli=Regex_1.findAllIn(paichu_vue)
      while (bianli.hasNext){
        if(bianli.next().contains("未见")){

        }else{
          vue=paichu_vue
        }

      }*/


      vue=Regex.next()


    }

    //二次判断特殊
    if(rs_val.contains("心肌病")&(!rs_val.contains("排除心肌病"))){
      vue="心肌病"
    }

    if(rs_val.contains("宫外孕")){
      val r = new Regex(
        """
          |.{0,10}宫外孕.{0,10}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()

        if(str.contains("术")){

        }else{
          vue=str
        }

      }
    }


    if(rs_val.contains("占位性病变")&rs_val.contains("腹膜后")&(!rs_val.contains("未见占位性病变"))){
      val r =new Regex(
        """
        |.{0,20}占位性病变.{0,20}|
        """)
      var str=" "
      val Regex1= r.findAllIn(rs_val)
      while (Regex1.hasNext){
        str=Regex1.next()
      }
      if(str.contains("未见")&str.contains("占位性病变")){

      }else {
        vue = "腹膜后占位性病变"
      }
    }

    if(rs_val.contains("性病")&(!rs_val.contains("占位性病变"))&(!rs_val.contains("恶性病变"))
    &(!rs_val.contains("良性病变"))&(!rs_val.contains("病变"))){
      vue="性病"
    }

    if(rs_val.contains("肝硬化")&(rs_val.contains("门脉高压"))){
      val r =new Regex(
        """
          |.{0,20}肝硬化.{0,20}|
        """)
      var str=" "
      val Regex1= r.findAllIn(rs_val)
      while (Regex1.hasNext){
        str=Regex1.next()

      }
      if(str.contains("未见")){

      }else{
        vue="肝硬化伴门脉高压"
      }
    }

    if(rs_val.contains("椎管")&rs_val.contains("占位")){
      val r =new Regex(
        """
          |椎管.{0,30}占位|
        """)
      var str=" "
      val Regex1= r.findAllIn(rs_val)
      while (Regex1.hasNext){
        str=Regex1.next()
        if(str.contains("占位")&(!str.contains("未"))&(!str.contains("不"))){
          vue=str
        }
      }
    }

//    颅内动静脉畸形|脑.*血管畸形|
    if(rs_val.contains("畸形")&&(rs_val.contains("动脉")||rs_val.contains("静脉"))&&rs_val.contains("颅内")){
      val r =new Regex(
        """
          |.{0,10}畸形.{0,3}|
        """)
      var str=" "

      val Regex1= r.findAllIn(rs_val)
      while (Regex1.hasNext){
        str=Regex1.next()
        if(str.contains("畸形")&(!str.contains("未见"))){
          vue=str
        }
      }
    }

    if(rs_val.contains("慢性硬膜下血肿")){
      val r =new Regex(
        """
          |.{0,10}慢性硬膜下血肿|
        """)
      var str=" "
      val Regex1= r.findAllIn(rs_val)
      while (Regex1.hasNext){

        str=Regex1.next()
        //调用除去考虑，未见等不确定因素的方法
          if(paichu_string(str)){
          }else{
            vue=str
          }


      }


    }

    if(rs_val.contains("骨")&&(rs_val.contains("转移")|rs_val.contains("破坏"))){
      val r =new Regex(
        """
          |.{0,15}骨.{0,10}转移|
          |.{0,10}骨.{0,10}破坏|
        """)
      var str=" "
      val Regex1= r.findAllIn(rs_val)
      while (Regex1.hasNext){

        str=Regex1.next()
        //调用除去考虑，未见等不确定因素的方法
        if(paichu_string(str)){
        }else{
          vue=str
        }


      }
    }

    if(rs_val.contains("膈下占位性病变")){
      val r =new Regex(
        """
          |.{0,10}膈下.{0,5}占位性病变.{0,10}|
        """)
      var str=" "
      val Regex1= r.findAllIn(rs_val)
      while (Regex1.hasNext){

        str=Regex1.next()
        //调用除去考虑，未见等不确定因素的方法
        if(paichu_string(str)){
        }else{
          vue=str
        }


      }
    }

    if(rs_val.contains("心")&&rs_val.contains("增大")&&rs_val.contains("明显")){
      val r =new Regex(
        """
          |{0,10}.心.{0,30}|
        """)
      var str=" "
      val Regex1= r.findAllIn(rs_val)
      while (Regex1.hasNext){

        str=Regex1.next()
        if(str.contains("明显")&str.contains("增大")) {
          //调用除去考虑，未见等不确定因素的方法
          if (paichu_string(str)) {
          } else {
            vue = str
          }
        }

      }
    }

    if(rs_val.contains("占位性病变")&&rs_val.contains("纵隔")) {
      val r = new Regex(
        """
          |.{0,10}纵隔.{0,20}占位性病变|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()

        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)) {
          println(str)
        } else {
          vue = str
        }
      }
    }

    if(rs_val.contains("纵隔")&&rs_val.contains("淋巴结肿大")){
      val r = new Regex(
        """
          |.{0,10}纵隔.{0,20}淋巴结肿大|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()

        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)) {
          println(str)
        } else {
          vue = str
        }
      }
    }

    if(rs_val.contains("活动性")&&rs_val.contains("肺结核")){
      val r = new Regex(
        """
          |.{0,10}活动性.{0,20}肺结核.{0,10}|.{0,10}肺结核.{0,20}活动性.{0,10}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()

        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)&(!str.contains("待排"))) {
        } else {
          vue = str
        }
      }
    }

    if(rs_val.contains("气胸")){
      val r = new Regex(
        """
          |.{0,10}气胸.{0,10}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()

        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)) {
        } else {
          vue = str
        }
      }

    }

    if(rs_val.contains("中等量")&rs_val.contains("胸腔积液")){
      val r = new Regex(
        """
          |.{0,10}胸腔.{0,10}积液.{0,10}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()

        if (str.contains("中等")) {
          //调用除去考虑，未见等不确定因素的方法
          if (paichu_string(str)) {
          } else {
            vue = str
          }
        }
      }

    }

//    肺部.*占位性病变.*可疑肿块|肺部.*可疑肿块.*占位性病变|
    if(rs_val.contains("肺")&rs_val.contains("占位性病变")){
      val r = new Regex(
        """
          |.{0,5}肺.{0,20}占位性病变.{0,10}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()

          //调用除去，未见等不确定因素的方法
          if (paichu_string(str)) {
          } else {
            vue = str
          }
        }

    }

    //排除以上如果没有匹配到特殊癌症才走这一步
    if(!(vue.contains("癌")|vue.contains("恶性肿瘤"))){
      if(rs_val.contains("癌")|rs_val.contains("恶性肿瘤")){
        val r = new Regex(
          """
            |.{0,5}(癌|恶性肿瘤).{0,10}|
          """)
        var str = " "
        val Regex1 = r.findAllIn(rs_val)
        while (Regex1.hasNext) {

          str = Regex1.next()
          println(str)
          //调用除去考虑，未见等不确定因素的方法
          if (paichu_string(str)) {
          } else {
            vue = str
          }
        }
      }


    }


    if(rs_val.contains("胸腔积液")){
  val r = new Regex(
  """
    |.{0,10}大量.{0,10}胸腔积液|.{0,10}胸腔积液.{0,10}大量|
  """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()
        println(str)
        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)) {
        } else {
          vue = str
        }
      }
    }
//    鳞状上皮内病变
    if(rs_val.contains("鳞状")|rs_val.contains("鳞状上皮内病变")|(rs_val.contains("高级别")&rs_val.contains("病变"))
    |(rs_val.contains("ASC-H"))){
      val r = new Regex(
        """
          |.{0,10}鳞状上皮内病变.{0,10}|.{0,10}高级别.{0,10}内病变.{0,5}|
          |.{0,10}鳞状.{0,5}细胞.{0,10}|.{0,10}ASC-H.{0,15}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()
        println(str)
        //调用除去考虑，未见等不确定因素的方法

          vue = str

      }
    }

    if(rs_val.contains("痢疾")){
      val r = new Regex(
        """
        |.{0,10}痢疾.{0,10}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()
        println(str)
        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)) {
        } else {
          vue = str
        }
      }

    }

    if(rs_val.contains("口腔")&rs_val.contains("肿块")){
//      口腔.*肿块|口腔.*肿瘤|

      val r = new Regex(
        """
          |.{0,10}口腔.{0,10}肿块.{0,10}|.{0,10}肿块.{0,10}口腔.{0,10}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()
        println(str)
        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)) {
        } else {
          vue = str
        }
      }

    }
//    眼.*静脉血栓|眼.*动脉栓塞|
    if(rs_val.contains("眼底")&(rs_val.contains("静脉血栓")|rs_val.contains("动脉栓塞"))){
      val r = new Regex(
        """
          |.{0,10}眼.{0,10}静脉血栓|.{0,10}眼.{0,10}动脉栓塞|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()
        println(str)
        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)) {
        } else {
          vue = str
        }
      }
    }

    if((rs_val.contains("宫颈")|| rs_val.contains("外阴"))&
      (rs_val.contains("囊肿")|rs_val.contains("肿物")|rs_val.contains("肿瘤"))){
      val r = new Regex(
        """
          |.{0,10}肿.{0,30}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()
        println(str)
        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)) {
        } else {
          vue = str
        }
      }
    }

    if(rs_val.contains("宫颈")&rs_val.contains("上皮")){
      val r = new Regex(
        """
          |.{0,10}病变.{0,10}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()
        println(str)
        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)) {
        } else {
          vue = str
        }
      }
    }

    if(rs_val.contains("扪及包块")&rs_val.contains("腹")){

      val r = new Regex(
        """
          |.{0,10}扪及包块.{0,10}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()
        println(str)
        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)) {
        } else {
          vue = str
        }
      }
    }

    if(rs_val.contains("腹水")){
      val r = new Regex(
        """
          |.{0,10}腹水.{0,10}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()
        println(str)
        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)) {
        } else {
          vue = str
        }
      }

    }

    if((rs_val.contains("食管")|rs_val.contains("胃底"))&rs_val.contains("静脉曲张")&rs_val.contains("重度")){
      val r = new Regex(
        """
          |.{0,10}静脉曲张.{0,10}|
        """)
      var str = " "
      val Regex1 = r.findAllIn(rs_val)
      while (Regex1.hasNext) {

        str = Regex1.next()
        println(str)
        //调用除去考虑，未见等不确定因素的方法
        if (paichu_string(str)) {
        } else {
          vue = str
        }
      }
    }

    if(vue.equals("1")){
      "1"
    }else{
      "0_"+vue
    }


  }

  /**数值判断
    *对数值进行判断，修改请参照if其他方法
    *修改首先添加正则，拉取数据后
    *使用if包含此数值
    * 修改大于号小于的值即可
    */

  def numMatch(rs_val:String)={
    //字符串的拉取
    val regex = new Regex(
      """
      |结肠溃疡.*[0-9]+.{0,10}.*息肉|结肠溃疡.*息肉.{0,10}[0-9]+.{0,10}|
      |十二指肠.{0,20}溃疡.{0,10}m.{7}|十二指肠.{0,30}溃疡|
      |胃.{0,20}溃疡.{0,30}cm|胃.*cm.{0,30}溃疡|
      |[0-9]+.*乳腺.{0,3}实.{0,3}性占位.{0,30}|
      |[0-9].{0,30}甲状腺.{0,5}实性占位.{0,35}|甲状腺.{0,5}实性占位.{0,35}|
      |甲状腺.{0,5}混合.{0,3}性占位.{0,35}|
      |[0-9].*卵巢.{0,5}囊肿.{0,20}|卵巢.{0,5}囊肿.{0,20}|
      |内膜.{0,10}厚.{0,15}|
      |子宫肌瘤.{0,20}|
      |泌尿.{0,20}实性占位性病变|生殖系统.{0,20}实性占位性病变|
      |腹.{0,20}m.*腹部.{0,20}占位性病变|
      |胰腺囊肿.{0,20}|回声区.{0,30}m.{0,20}胰腺囊肿|
      |肾.{0,30}m|
      |主胰管扩张.{0,30}胰腺囊肿|
      |胆囊.{0,20}息肉.{0,10}|
      |肝脏.{0,20}血管瘤.{0,20}|
      |颈.{0,3}动脉.{0,5}狭窄.{0,20}%|
      |血管瘤.{0,20}|
      |.*肝.*囊肿.*|
      |肝.{0,3}血管瘤.{0,20}|
      |血清肌酸激酶-MB同工酶(CK-MB)升高，肌钙蛋白:阳性，肌红蛋白:阳性|
      |宫颈刮片.{0,10}巴氏.{0,10}级|
      |子宫内膜.{0,20}m|
      |.{0,10}心室停搏.{0,10}|
      |室.{0,3}性心动过速.{0,20}心.{0,2}率.{0,20}|
      |QT间期延长.{0,20}|
      |长R.{0,3}R间期.{0,20}|
      |严重.{0,10}心动过缓.*三度.{0,10}房室阻滞|
      |心房颤动.{0,15}预激.*ms|
      |.*便潜血.*|.*粪便隐血.*|
      """
    )


    //数值拉取
    def getCm(str:String)= {
      val regex_num = new Regex(
        """
          |[0-9]+\.[0-9]+|
          |[0-9]+|
        """
      )

      //定义返回值
      var max=" "

      //转换为cm,获取最大值
      if (str.contains("mm")|str.contains("MM")|str.contains("M M")|str.contains("m m")){


        //创建数组获取最大值
        val row_ints = new ArrayBuffer[Double]

        val result_1=regex_num.findAllIn(str)
        while(result_1.hasNext){
          //获取最大值
          row_ints.append(result_1.next().toDouble/10.0)
        }
         max= row_ints.max.toString
      }

      //不转换，获取最大值
      else if(str.contains("cm")|str.contains("CM")|str.contains("c m")|str.contains("C M")){
        //创建数组获取最大值
        val row_ints = new ArrayBuffer[Double]

        val result_1=regex_num.findAllIn(str)
        while(result_1.hasNext){
          //获取最大值
          row_ints.append(result_1.next().toDouble)
        }
         max= row_ints.max.toString
      }
      else{
        val row_ints = new ArrayBuffer[Double]
        val result_1=regex_num.findAllIn(str)
        while(result_1.hasNext){
          //获取最大值
          row_ints.append(result_1.next().toDouble)
        }
       //没有匹配到数字，应该还是成功的
        if(row_ints.isEmpty){
          println("正则匹配到数据，但是没有匹配到数值")
          max="-1"
          println(str)
        }else{
          max=row_ints.max.toString
        }

      }



    max

    }





    //获取每条异常最大值与配置参数进行对比，返回结果为0（异常）
    var Regex=regex.findAllIn(rs_val)
    var result_status="1"

    var result_vue="正常"

    while (Regex.hasNext){

      val  string_next=Regex.next()



      /**这里不能用match(它没有container)
        * 判断大小，如果大小存在，不在进行循环直接跳出
        */
      if(string_next.contains("结肠")){
        //获取最大值
        val max: String = getCm(string_next)
        //判断
        result_status= if(max.toDouble>0.5) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>0.5) string_next else result_vue

      }

      if(string_next.contains("十二指肠")){
        //获取最大值
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>2.5) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>2.5) string_next else result_vue
      }

      if(string_next.contains("胃")&string_next.contains("溃疡")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>3.0) "0" else result_status
       println(string_next)
        result_vue=if(max.toDouble>3.0) string_next else result_vue
      }

      if(string_next.contains("乳腺")&string_next.contains("占位")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>2.0) "0" else result_status
//       println(string_next)
        result_vue=if(max.toDouble>2.0) string_next else result_vue
      }

      if(string_next.contains("甲状腺")&string_next.contains("实")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>2.0) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>2.0) string_next else result_vue
      }

      if(string_next.contains("甲状腺")&string_next.contains("混合性")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>2.0) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>2.0) string_next else result_vue
      }
      if(string_next.contains("心动过缓")&string_next.contains("阻滞")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble<35) "0" else result_status
        //        println(string_next)
        result_vue=if(max.toDouble<35) string_next else result_vue
      }

      if(string_next.contains("卵巢")&string_next.contains("肿")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>5.0) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>5.0) string_next else result_vue
      }

      if(string_next.contains("内膜")&(rs_val.contains("不规则阴道出血")|rs_val.contains("阴道不规则出血"))){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>0.5) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>0.5) string_next else result_vue
      }

      if(string_next.contains("子宫肌瘤")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>5.0) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>5.0) string_next else result_vue
      }

      if((string_next.contains("泌尿")||string_next.contains("生殖系统"))&string_next.contains("占位性病变")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>3.0) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>3.0) string_next else result_vue
      }

      if(string_next.contains("肾")&string_next.contains("囊肿")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>5.0) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>5.0) string_next else result_vue
      }

      if(string_next.contains("腹部")&string_next.contains("占位性病变")&(!string_next.contains("未见"))){

        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>3.0) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>3.0) string_next else result_vue
      }

      if(string_next.contains("胰腺")&string_next.contains("囊肿")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>3.0) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>3.0) string_next else result_vue
      }

      if(string_next.contains("主胰管")&string_next.contains("胰腺")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>0.5) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>0.5) string_next else result_vue
      }

      if(string_next.contains("胆囊")&string_next.contains("息肉")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>10) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>10) string_next else result_vue
      }

      if(string_next.contains("肝脏")&string_next.contains("血管瘤")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>10) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>10) string_next else result_vue
      }

      if(string_next.contains("肝")&string_next.contains("囊肿")&
        (!string_next.contains("考虑肝囊肿"))){
        val r =new Regex(
          """
            |肝.{0,30}m.*肝.{0,3}囊肿|肝.{0,3}囊肿.*m|
          """)
        var str=" "
        val Regex1= r.findAllIn(rs_val)
        while (Regex1.hasNext){
          str=Regex1.next()
          if(str.contains("肝")) {
            val max: String = getCm(string_next)
            //判断
            result_status = if (max.toDouble > 10) "0" else result_status
            //        println(string_next)
            result_vue = if (max.toDouble > 10) string_next else result_vue
          }
        }


      }

      if(string_next.contains("颈")&string_next.contains("狭窄")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>50) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>50) string_next else result_vue
      }
      if(string_next.contains("QT")&string_next.contains("间期延长")){
        println(string_next)
        val max: String = getCm(string_next)
        result_status = if(max.toDouble>550) "0" else result_status
        result_vue=if(max.toDouble>550) string_next else result_vue
      }

      if(string_next.contains("长")&string_next.contains("R")){
        println(string_next)
        val max: String = getCm(string_next)
        result_status = if(max.toDouble>3) "0" else result_status
        result_vue=if(max.toDouble>3) string_next else result_vue
      }

      if(string_next.contains("胆囊")&string_next.contains("息肉")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>0.8) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>0.8) string_next else result_vue
      }

      if(string_next.contains("血管瘤")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>5) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>5) string_next else result_vue
      }

      if(string_next.contains("血管瘤")&string_next.contains("肝")){
        val max: String = getCm(string_next)
        //判断
        result_status = if(max.toDouble>3) "0" else result_status
//        println(string_next)
        result_vue=if(max.toDouble>3) string_next else result_vue
      }

      if(string_next.contains("宫颈刮片")&(string_next.contains("III")|string_next.contains("IV")|string_next.contains("V")|string_next.contains("3")
        |string_next.contains("4")|string_next.contains("5")|string_next.contains("三")|string_next.contains("四")|string_next.contains("五"))){
        //判断
        result_status = "0"
//        println(string_next)
        result_vue="宫颈刮片>=3级"
      }

      if(string_next.contains("心动过速")){
        val max: String = getCm(string_next)
//        println(string_next)
        //判断
        result_status = if(max.toDouble>=150) "0" else result_status
        //        println(string_next)
        result_vue=if(max.toDouble>=150) string_next else result_vue
      }

    }

    if(result_status.equals("1")){
      "1"
    }else{
      "0_"+result_vue
    }


//    regex.findAllIn()
  }

  //未见，无，不
  //包含的话返回true,否则false
  //需要过滤其他条件加上就可以
  def paichu_string(zhengze:String)={
/*    if((!zhengze.contains("除外"))&(!zhengze.contains("可能"))&(!zhengze.contains("疑似"))
    &(!zhengze.contains("考虑"))&(!zhengze.contains("未见"))&(!zhengze.contains("无"))
    &(!zhengze.contains("不"))&(!zhengze.contains("是否"))&(!zhengze.contains("有否"))) */

    if(
     (!zhengze.contains("未见"))&(!zhengze.contains("无"))
      &(!zhengze.contains("不"))&(!zhengze.contains("未发现"))&(!zhengze.contains("未"))){

      false
  }else {

      true
    }

  }

  //可能疑似,//如果没有说可能，那么2给都要走，如果说了可能，那么只走第一个
  def keneng_string(zhengze:String)={
    /*    if((!zhengze.contains("除外"))&(!zhengze.contains("可能"))&(!zhengze.contains("疑似"))
        &(!zhengze.contains("考虑"))&(!zhengze.contains("未见"))&(!zhengze.contains("无"))
        &(!zhengze.contains("不"))&(!zhengze.contains("是否"))&(!zhengze.contains("有否"))) */

    if((!zhengze.contains("除外"))&(!zhengze.contains("可能"))&(!zhengze.contains("疑似"))
      &(!zhengze.contains("考虑"))
      &(!zhengze.contains("是否"))&(!zhengze.contains("有否"))){

      false
    }else {

      true
    }

  }

  def main(args: Array[String]): Unit = {
    val Match= noNumMatch("右肾大小约129x58mm，包膜欠光滑，肾实质厚度稍变薄、回声欠均匀，于实质中段见一个无回声区，大小约27x25mm，向外凸起，边界清晰，形态规则，集合系统未见分离\")")

    val str: String = numMatch(
                               """
                               """
    )
    println(Match)


    println(str)
  }


}
