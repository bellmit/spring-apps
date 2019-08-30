package com.haozhuo.datag.service.Insurance


import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

object GetDataJZX_test {
  val str1: String = "甲状腺形态大小正常，甲状腺双叶均可见一类圆形无回声结节，右叶大小约2x3mm，左叶大小约4mmx3mm，壁薄光滑，后壁回声增强，内部未见血流。"
  val str2: String = "双侧甲状腺大小形态正常，包膜光整，左侧叶实质内查见多个混合回声结节，较大者约11×5mm；右侧叶实质内查见一枚弱回声结节，大小约7×5mm，边界清，形态规则，CDFI:结节内及周边见点状血"
  val str3:String="甲状腺形态大小正常，甲状腺左叶内见多个低回声结节，最大约3.6mm×2.5mm。CDFI：血流显示未见异常。"
  val str4:String="双侧甲状腺大小形态正常，包膜光整，实质回声均匀，未见明显异常回声。CDFI：血流显示未见异常。（TI-TADS 4级）"
  val str5:String=" 双侧甲状腺大小形态正常，包膜尚完整，实质分布欠均匀，于右侧内见两个低回声，一大小约24mmx13mm,其内见多个强回声，内部回声不均匀，边界不清后伴声影，CDFI：内见血流信号；另一大小约5mmx4mm，边界清晰。左侧甲状腺未见明显异常回声。"
  val str6:String=" 双侧甲状腺大小形态正常，包膜尚完整，实质分布不均匀，于双侧腺体内均见一等回声结节，较大的位于左侧，大小约19.0x11.0mm,边界不清，内部回声不均，内见不规则液性暗区，内见细小强回声"
  val str7:String=" 双侧甲状腺大小形态正常，包膜光整，实质回声均匀，右侧叶中份扫查一实性低回声结节，大小约0.6x0.4cm，纵横比＞1，后方伴声衰减，边缘模糊，形态欠规则，边缘呈"
  val str8:String=" 双侧甲状腺大小正常，腺体组织光点分布均匀，右侧叶内见大小约38*9*7mm，边界清，纵横比大于1，周边见血流环绕。左侧叶内见多枚混合性结节回声，以囊性为主，大者约9*8*8mm"

  def main(args: Array[String]) {

    //返回一个过滤了数据的集合
    import scala.collection.mutable._





  }
  def jiazhuangxian(yourString:String) ={
    val strings: ArrayBuffer[String] = {

      //正则表达式
      val regex = new Regex(
        """[0-9]+mmx[0-9]+mm|
          |[0-9]+mm\*[0-9]+mm|
          |[0-9]+cmx[0-9]+cm|
          |[0-9]+cm\*[0-9]+cm|
          |[0-9]+x[0-9]+mm|
          |[0-9]+x[0-9]+cm|
          |[0-9]+\*[0-9]+mm|[0-9]+\*[0-9]+cm|
          |[0-9]+×[0-9]+mm|[0-9]+×[0-9]+cm|[0-9]+mm×[0-9]+mm|[0-9]+cm×[0-9]+cm|
          |[0-9]+\.+[0-9]+x[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+x[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+\*[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+×[0-9]+\.[0-9]+cm|
          |[0-9]+\.[0-9]+x[0-9]+\.[0-9]+mm|
          |[0-9]+\.+[0-9]+cm|[0-9]+\.[0-9]+mm|
          |[0-9].{3,10}mm|
          |甲状腺结节TADS：[0-9]+|TADS[\s][0-9]+|
          |结节.{0,40}边界不清|
          |结节.{0,40}纵横比.{0,5}1|
        """)

      val strings = new ArrayBuffer[String]

      val res = regex.findAllIn(yourString)
      while (res.hasNext) {
        val s = res.next()
        if(s.contains("边界不清")){
          strings.append("结节:边界不清")
        }else if (s.contains("纵横比>1")|
          s.contains("纵横比＞1")|s.contains("纵横比大于1")) {
          strings.append("结节:纵横比大于1")
        }else{
          strings.append(s)
        }
      }
      strings
    }
    strings
  }


  //对数据进行提取并转换单位
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
        //返回原始单位是毫秒的结果
        row = result_Max_Num



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


  //结果方法
  //结果数据的打印
  def result(yourString2:ArrayBuffer[String])={
    //创建结果集
    val result_arr=new ArrayBuffer[String]
    var status_leave="good"
    //第一遍判断疾病等级
    yourString2.foreach(x=>{
      if(x.contains("TADS")) {

        status_leave="分析结果：TADS>3级"
        result_arr.append(status_leave)
      }
      if(x.contains("边界不清")){
        status_leave="分析结果：乳腺结节，模糊、边界不清"
        result_arr.append(status_leave)
      }
      if(x.contains("纵横比")){
        status_leave="分析结果：乳腺结节：纵横比大于1"
        result_arr.append(status_leave)
      }
    })


    var status:String = "good"
    //第二遍正则表达式数值过滤
    yourString2.foreach(x=>{
      val regexGetNum = new Regex(
        """
          |[0-9]+\.[0-9]+|
          |[0-9]+|
        """)
      val reg=regexGetNum.findAllIn(x)
      while (reg.hasNext){
        if(reg.next().toDouble>=30){
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





  //封装3个方法.
  def getStatus(yourString:String)= {

    val arr: ArrayBuffer[String] = jiazhuangxian(yourString)

    //          arr.foreach(x=>println(x))
    val strings: ArrayBuffer[String] = panduanRXUntil(arr)

    //          strings.foreach(x=>println(x))
    val result_status: String = result(strings)
    result_status
  }

}
