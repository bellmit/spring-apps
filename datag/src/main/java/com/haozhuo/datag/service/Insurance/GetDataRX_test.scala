package com.haozhuo.datag.service.Insurance

import java.lang.Exception

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

object GetDataRX_test {
  val str1:String = "左侧乳腺腺体层内约12点见一低回声区，大小约3x3mm, 边界清晰，内部回声欠均匀，CDFI：周边见彩色血流信号。左侧乳腺腺体层内约5点见一低回声区，大小约5x4mm, 边界清晰，内部回声欠均匀"
  val str2:String = "右侧内见一处、左侧内见多处管状无回声区，右侧内径约1.4mm，左侧最宽处内径约2.0mm。右侧乳腺腺体层内见两个低回声区，大者位于乳头内侧约5mm×4mm,边界清晰，内部回声较均匀，后方回声"
  val str3:String = "双侧乳腺腺体层轻度增厚,内部结构稍紊乱,回声欠均匀，右侧乳腺腺体层内见多个低回声结节，最大约9mm×3mm/8mm×5mm。左侧乳腺腺体层内见多个低回声肿物及结节，最大约14mm×8mm，有的结节不"
  val str4:String = " 双侧乳腺腺体层次结构紊乱，回声不均，双侧腋下淋巴结显示--建议3个月复查右乳9点处可见条状无回声，内径约0.19cm--建议定期复查左乳12点处可见大小约0.52x0.34cm的弱回声，边界较清晰"
  val str5_1:String ="左侧乳腺约9点钟方向腺体层内见低回声区，大小约5x3.5mm,"
  val str5_2:String="边界欠清晰，内部回声欠均匀，CDFI：未见明显彩色血流信号。右侧乳腺约12点钟方向腺体层内见管状无回声区，较宽处"

  val str5_3:String=  "内径约3mm，内清"
  val str6:String ="双侧乳腺腺体层增厚,内部结构紊乱,回声不均，未见异常血流。右侧乳腺外上象限内见多个管状无回声区，较宽内径约0.18cm，左侧乳腺外上象限内见多个管状无回声区，较宽内径约0.21cm，左侧乳头下方可见片状低回声，缺乏立体感，大小约0.69*0.38cm。"
  val str7:String=" 双侧乳腺腺体层次结构紊乱，回声不均。右乳头下方见两个低回声，大小分别为16.3x5.2mm、5.0x2.7mm形态尚规整，边界显示尚清晰，大小分别为16.3x5.2mm、5.0x2.7mm，形态尚规整，边界显示尚清晰，未见明显彩色血流信号。双腋下未见明显肿大淋巴结。小结：右侧乳腺结节（多发）（BI-RADS4类） "

  val str8:String="乳腺结节RADS IV 6*0.5cm 5x3.5mm"
  val str9:String="双侧乳腺腺体层增厚,内部结构紊乱,回声不均，于右乳2点钟处见囊性回声，大小约2.7x0.16cm，形态规整，边界清晰。双侧腋下淋巴结未见明显异常。小结 ：右侧乳腺囊"
  val str10:String=" 双侧乳腺增生，三维图峰值较低，二维图黄色.右侧乳腺结节，可见两个明显结节，11点钟方向有一大小约29*13MM，8点钟方向有一大小约13*13mm，三维图峰值较高，二维图红色."
  val str11:String="右侧乳腺纤维瘤术后。右侧乳腺外下象限和内上象限可见多个低回声结节，大者约4.7mm×2.7mm，边界不清，内部结构不均匀，纵/横＜1 ，CDFI;结节周边未见血流信"

  def main(args: Array[String]): Unit = {



    //导入可变集合
  import scala.collection.mutable._



  }


  //初步拉取数据
  def ruxian(yourString:String) = {

    val string_list = new ArrayBuffer[String]
    //正则表达式
    val regex = new Regex(
      """[0-9]+mmx[0-9]+mm|
        |[0-9]+mm\*[0-9]+mm|
        |[0-9]+cmx[0-9]+cm|
        |[0-9]+cm\*[0-9]+cm|
        |[0-9]+x[0-9]+mm|
        |[0-9]+x[0-9]+cm|[0-9]+.{0,7}MM|
        |[0-9]+\*[0-9]+mm|[0-9]+\*[0-9]+cm|
        |[0-9]+×[0-9]+mm|[0-9]+×[0-9]+cm|[0-9]+mm×[0-9]+mm|[0-9]+cm×[0-9]+cm|
        |[0-9]+\.[0-9]+x[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+x[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+\*[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+×[0-9]+\.[0-9]+cm|
        |[0-9]+\.[0-9]+x[0-9]+\.[0-9]+mm|[0-9]+\.+[0-9]+x[0-9]+\.[0-9]+mm|[0-9]+\.+[0-9]+\*[0-9]+\.[0-9]+mm|
        |[0-9]+\.+[0-9]mm+×[0-9]+\.+[0-9]mm|[0-9]+\*[0-9]+\.[0-9]+cm|
        |[0-9]+\.[0-9]+cm|[0-9]+\.[0-9]+mm|
        |[0-9]+cm|[0-9]+mm|
        |乳腺结节.{0,20}RADS：[4-9]+|乳腺结节.{0,20}RADS[\s][4-9]+|乳腺结节.{0,20}RADS:[4-9]+|乳腺结节.{0,20}RADS[4-9]+|
        |乳腺结节.{0,20}RADS：IV|乳腺结节.{0,20}RADS：V|乳腺结节.{0,20}RADS：VI|乳腺结节.{0,20}RADS:IV|乳腺结节.{0,20}RADS:V|乳腺结节.{0,20}RADS:VI|乳腺结节.{0,20}RADS[\s]IV|乳腺结节.{0,20}RADS[\s]V|乳腺结节.{0,20}RADS[\s]VI|乳腺结节.{0,20}RADSIV|乳腺结节.{0,20}RADSV|乳腺结节.{0,20}RADSVI|
        |结节.{0,20}边界不清|结节.{0,20}模糊|
      """)




    // RADS分为I,II,III这里只写3级以上的

    val res = regex.findAllIn(yourString)
    while (res.hasNext) {
      val str: String =res.next().replace("MM","mm")
      string_list.append(str)

    }

    string_list
  }//方法结束


  //单位化为统一(mm的形式）
  def panduanRXUntil(yourArray:ArrayBuffer[String])= {

    val regexGetNum = new Regex(
      """
        |[0-9]+\.[0-9]+|
        |[0-9]+|
      """)

    val result_arr: ArrayBuffer[String] = yourArray.map(x => {

      //初始化
      var row: String = " "



      //如果x包含mm
      if (x.contains("mm")) {
        //过滤出数值,单位mm
        val mmNum = regexGetNum.findAllIn(x)


        var mm=" "
        //row_ints.append(5)
        //mm如果是整数就不会进入循环
        val row_ints = new ArrayBuffer[Double]

        //获取每一行的数据
        while (mmNum.hasNext){

          //每一行的数据装入一个数组
          mm = mmNum.next()


          row_ints.append(mm.toDouble)


        }
        //Int形式

        val row_ints1: Double = row_ints.max


        val result_Max_Num: String = row_ints1.toString
        //返回原始单位是毫秒的结果
        row = result_Max_Num

        if(x.contains("边界不清")){
          row="边界不清"
        }

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

        row = result_Max_Num

        if(x.contains("边界不清")){
          row="边界不清"
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


  //获取最大规整话数据 方法结束


  //结果数据的打印
  def result(yourString2:ArrayBuffer[String])={
    //创建结果集
    val result_arr=new ArrayBuffer[String]
    var status_leave="good"
    //第一遍判断疾病等级
    yourString2.foreach(x=>{
      if(x.contains("乳腺结节")) {

        status_leave="分析结果：乳腺结节>3级"
        result_arr.append(status_leave)
      }
      if(x.contains("边界不清")|x.contains("模糊")){
        status_leave="分析结果：乳腺结节，模糊、边界不清"
        result_arr.append(status_leave)
      }
    })


    var status:String = "ok"
    //第二遍正则表达式数值过滤
    yourString2.foreach(x=>{
      val regexGetNum = new Regex(
        """
          |[0-9]+\.[0-9]+|
          |[0-9]+|
        """)
      val reg=regexGetNum.findAllIn(x)
      while (reg.hasNext){
        if(reg.next().toDouble>=20){
          status = "overGG"
          result_arr.append(status_leave)
        }

      }




    })
    //返回结果集合
    var status_result=" "
    if(result_arr.isEmpty){
      status_result="1"
    }else{
      status_result="0"
    }
    status_result

  }
  //最后判断结束
  //调用测试

  //封装3个方法.
  def getStatus(yourString:String)= {
    val arr: ArrayBuffer[String] = ruxian(yourString)
    //          arr.foreach(x=>println(x))
    val strings: ArrayBuffer[String] = panduanRXUntil(arr)

    //          strings.foreach(x=>println(x))
    val result_status: String = result(strings)
    result_status
  }
}
