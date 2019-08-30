package com.haozhuo.datag.service.Insurance

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

object GetDataNK_test {

  val regexGetNum = new Regex(
    """
      |[0-9]+\.[0-9]+|
      |[0-9]+|
    """)

  def fubu(yourString: String) = {


    //正则表达式

    /*
      思路
      1.全部楼过来
      2.楼完判断包含拒保的直接返回拒保
      3.楼完不包含拒保的进行判断
      4.判断转义后如果在拒保范围内，返回拒保
       */


    val regex = new Regex(
      """
        |胆管结石|胆管扩张|
        |胆囊萎缩|多囊肝|多囊脾|多囊肾|
        |肝异常回声|肝脏增大|
        |卵巢畸胎瘤|卵巢占位性病变|
        |慢性胰腺炎|盆腔肿块|脾厚|脾内回声异常|
        |脾脏增大|脾脏增厚|脾肿大|胰腺异常回声|
        |胰腺占位性病变|脂肪肝（重度）|
        |肝.{0,15}囊状.{0,15}mm|肝.{0,15}囊状.{0,15}cm|
        |卵巢.{0,15}囊肿.{0,15}mm|卵巢.{0,15}囊肿.{0,15}cm|
        |附件.{0,30}mm|
        |盆腔积液.{0,20}mm|盆腔积液.{0,20}cm|
        |子宫肌瘤.{0,15}mm|子宫肌瘤.{0,15}cm|
      """)

    //直接拒保
    val regex_jubao = new Regex(
      """
        |胆管结石|胆管扩张|
        |胆囊萎缩|多囊肝|多囊脾|多囊肾|
        |肝异常回声|肝脏增大|
        |卵巢畸胎瘤|卵巢占位性病变|
        |慢性胰腺炎|盆腔肿块|脾厚|脾内回声异常|
        |脾脏增大|脾脏增厚|脾肿大|胰腺异常回声|
        |胰腺占位性病变|脂肪肝（重度）|
      """)
    var jubao_array = new ArrayBuffer[String]

    //集合装匹配的字段
    var jihe = new ArrayBuffer[String]

    val res = regex.findAllIn(yourString)
    while (res.hasNext) {
      val s = res.next()
      jihe.append(s.toString)

      val panduan = regex_jubao.findAllIn(s.toString)
      //丢正则表达式判断是否拒保,匹配每行拉取的数据


      while (panduan.hasNext) {
        jubao_array.append(panduan.next().toString)
      }
    }


    //判断单位，规整化数据
    def panduanRXUntil(yourArray:ArrayBuffer[String])= {






      val result_arr: ArrayBuffer[String] = yourArray.map(x => {

        //初始化
        var row: String = " "





        //如果x包含mm
        if (x.contains("mm")) {


          var mmNum =regexGetNum.findAllIn(x)

          //row_ints.append(5)
          //mm如果是整数就不会进入循环
          val row_ints = new ArrayBuffer[Double]

          //获取每一行的数据
          while (mmNum.hasNext){
            var mm=" "
            //每一行的数据装入一个数组
            mm = mmNum.next()


            row_ints.append(mm.toDouble)


          }
          //Int形式

          val row_ints1: Double = row_ints.max




          val result_Max_Num: String = row_ints1.toString

          //数据整理
          if((x.contains("肝"))&&(x.contains("囊状"))){
            row= "肝囊肿:"+result_Max_Num
          }
          if(x.contains("卵巢")){
            row="卵巢囊肿:"+result_Max_Num
          }
          if(x.contains("附件")){
            row="附件大小:"+result_Max_Num
          }
          if(x.contains("盆腔积液")){
            row="盆腔积液"+result_Max_Num
          }
          if(x.contains("子宫肌瘤")){
            row="子宫肌瘤"+result_Max_Num
          }
          //返回原始单位是毫秒的结果



        } else if (x.contains("cm")) {

          //过滤出数值,单位mm
          val mmNum = regexGetNum.findAllIn(x)
          var mm=" "

          val row_ints = new ArrayBuffer[Double]

          //获取每一行的数据
          while (mmNum.hasNext){
            //每一行的数据装入一个数组
            mm = mmNum.next()

            row_ints.append(mm.toDouble*10)

          }
          //Int形式
          val max: Double = row_ints.max
          val result_Max_Num: String = max.toString
          //返回原始单位是毫秒的结果

          if((x.contains("肝"))&&(x.contains("囊状"))){
            row= "肝囊肿:"+result_Max_Num
          }
          if(x.contains("卵巢")){
            row="卵巢囊肿:"+result_Max_Num
          }
          if(x.contains("附件")){
            row="附件大小:"+result_Max_Num
          }
          if(x.contains("盆腔积液")){
            row="盆腔积液"+result_Max_Num
          }
          if(x.contains("子宫肌瘤")){
            row="子宫肌瘤"+result_Max_Num
          }



        } else {
          row = x
        }


        //返回值
        row
      })
      //返回排序好的数值单位mm

      result_arr
    }

    //初始化结果
    var result="1"

    //      jihe.foreach(x=>println(x))



    if(!jubao_array.isEmpty){
      //直接拒保
      result="0"
    }else{
      //获取标准化的集合

      val Stand_array: ArrayBuffer[String] = panduanRXUntil(jihe)
      //        Stand_array.foreach(x=>println(x))
      //第二次判断数值




      Stand_array.foreach(row=>{

        /*肝囊肿
          卵巢囊肿
          附件大小
          盆腔积液
          子宫肌瘤*/
        if((row.contains("肝囊肿"))||(row.contains("卵巢囊肿"))||(row.contains("附件大小"))||(row.contains("子宫肌瘤"))) {
          var num = regexGetNum.findAllIn(row)
          while (num.hasNext) {
            val v = num.next().toDouble
            if (v > 50) {
              result = "0"
            }
          }
        }

        if(row.contains("盆腔积液")){
          var num =regexGetNum.findAllIn(row)
          while (num.hasNext){
            val v=num.next().toDouble
            if(v>30){
              result="0"
            }
          }
        }


      })


    }
    result
  }
  //方法结束


  def main(args: Array[String]): Unit = {
    val str1:String="肝脏形态、大小正常，肝左叶下段可见一囊状液性密度影，最大径约36mm"
    val str2:String="上中腹未见淋巴结肿大。未见腹水。腹膜后间隙未见积液。腹膜后大血管未见异常。结论：1、肝内低密度影，肝囊肿，肝血管瘤？建议增强CT进一步检查。2、左肾囊肿。腹主动脉壁钙化"
    val str3:String="子宫肌瘤大小约1.2*1.0cm，右侧卵巢囊肿大小约3.2*2.4cm，盆腔积液约3.2*2.7cm,节育环位置下移，环距宫底3.7cm  "
    val str4:String="宫内节育器位置正常盆腔积液，约5.3*1.9cm  "
    val str5:String=" 子宫肌瘤，大小约53*50mm，伴钙化。双侧附件区未见明显异常回声。"
    val str6:String="多囊肾"



  println(fubu(str1))
  }
}





