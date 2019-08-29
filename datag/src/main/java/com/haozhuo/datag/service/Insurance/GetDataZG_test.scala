package com.haozhuo.datag.service.Insurance

import scala.collection.mutable._
import scala.util.matching.Regex

object GetDataZG_test {

  val str1:String="子宫前位，大小58mm×50mm×45mm，轮廓光整，实质回声均匀，宫内节育器位置正常。子宫颈前后径约35mm。宫颈见多个类圆形无回声区，最大约为8mm×6mm。"
  val str2:String="规则，边界清。子宫后穹隆处探及液性暗区，深约19mm，透声可。双侧卵巢显示清晰，附件区未见明显异常。"
  val str3:String="子宫前位，形态大小正常，轮廓光整，肌层回声均匀，宫颈见多个类圆形无回声区，其中一个大小约29mm×19mm，形态规则，边边界清。子宫后穹窿探及液性暗区，范围约7mm×6mm。"
  val str4:String="子宫形态大小正常，轮廓光整，肌层回声均匀，宫颈见多个类圆形无回声区，最大约为8mmx5mm。宫颈增厚约35mm。可见节育环。双侧附件区未见明显异常回声"
  val str5:String="子宫形态大小正常，轮廓光整，实质回声均匀，内膜增厚约14mm。子宫壁可见一个低回声区，大小约27mm×13mm。右侧附件可见29mm×23mm 类圆形无回声区，壁薄而光滑。左侧附件未见明显异常。"
  val str6:String=" 子宫肌瘤，大小约53*50mm，伴钙化。双侧附件区未见明显异常回声。"
  val str7:String="后位子宫，大小形态正常，肌层回声不均匀，宫体前壁近粘膜处似可见一偏低回声结节，大小约1.2×1.1cm，边界尚清，回声欠均。内膜厚约0.7cm。右卵巢内可见一囊性暗区，大小约2.8×2.2c"
  val str8:String="右侧乳腺纤维瘤术后。右侧乳腺外下象限和内上象限可见多个低回声结节，大者约4.7mm×2.7mm，边界不清，内部结构不均匀，纵/横＜1 ，CDFI;结节周边未见血流信"
  val str9:String=" 大小4.2x3.5cm，边界清，宫腔内可见0.5x0.4cm偏强回声，边界清，内膜厚0.7cm。宫颈处可见多个无回声，较大直径0.6cm。左卵巢可见2.5x1.5cm囊肿，"
  val str10:String="子宫后位，形态大小正常，轮廓光整，实质回声均匀，内膜不厚，未见明显异常回声。右侧附件可见一无回声暗区，大小约17mm×29mm，形态规则，边界清。左侧附件60mm显示不清"
  def main(args: Array[String]): Unit = {

  def zigong(yourString:String)= {


      val regex = new Regex(
        """
          |[0-9]+mm×[0-9]+mm×[0-9]+mm|[0-9]+cm×[0-9]+cm×[0-9]+cm|
          |[0-9]+mmx[0-9]+mmx[0-9]+mm|[0-9]+cmx[0-9]+cmx[0-9]+cm|
          |[0-9]+mmX[0-9]+mmX[0-9]+mm|[0-9]+cmX[0-9]+cmX[0-9]+cm|
          |[0-9]+mmx[0-9]+mm|[0-9]+mmX[0-9]+mm|
          |[0-9]+mm\*[0-9]+mm|
          |[0-9]+cmx[0-9]+cm|[0-9]+cmX[0-9]+cm|
          |[0-9]+cm\*[0-9]+cm|
          |[0-9]+x[0-9]+mm|[0-9]+X[0-9]+mm|
          |[0-9]+x[0-9]+cm|[0-9]+X[0-9]+cm|
          |[0-9]+\*[0-9]+mm|[0-9]+\*[0-9]+cm|
          |[0-9]+×[0-9]+mm|[0-9]+×[0-9]+cm|[0-9]+mm×[0-9]+mm|[0-9]+cm×[0-9]+cm|
          |[0-9]+\.+[0-9]+x[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+x[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+\*[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+×[0-9]+\.+[0-9]+cm|
          |[0-9]+\.+[0-9]mm+×[0-9]+\.+[0-9]mm|
          |[0-9]+\.+[0-9]+cm|[0-9]+\.[0-9]+mm|
          |[0-9]+cm|[0-9]+mm|
          |内膜.{0,10}厚.{0,10}cm|内膜.{0,10}厚.{0,10}mm|
          |子宫肌瘤.{0,15}mm|
          |卵巢.{0,15}囊肿|
          |附件.{0,30}mm|
          """)
      val reg = regex.findAllIn(yourString)


      val strings = new ArrayBuffer[String]

      while (reg.hasNext) {
        strings.append(reg.next())

      }
      strings
    }//方法结束

    //判断单位，并且少量转换
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
          //取出最大值
          val row_ints1: Double = row_ints.max



          val result_Max_Num: String = row_ints1.toString
          //返回原始单位是毫秒的结果

          //获取，内膜，卵巢，子宫肌瘤
          if(x.contains("内膜")){
           row= "内膜:"+result_Max_Num
          }else if(x.contains("卵巢")){
            row="卵巢:"+result_Max_Num
          }else if(x.contains("子宫肌瘤")){
            row="子宫肌瘤:"+result_Max_Num
          }else if(x.contains("附件")) {
            row="附件:"+result_Max_Num
          }
           else {
              row = result_Max_Num
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

          //获取，内膜，卵巢，子宫肌瘤
          if(x.contains("内膜")){
            row= "内膜:"+result_Max_Num
          }else if(x.contains("卵巢")){
            row="卵巢囊肿:"+result_Max_Num
          }else if(x.contains("子宫肌瘤")){
            row="子宫肌瘤"+result_Max_Num
          }else {
            row = result_Max_Num
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

    //判断方法结束

    //结果方法
    //结果数据的打印
    def result(yourString2:ArrayBuffer[String])={

      val regexGetNum = new Regex(
        """
          |[0-9]+\.[0-9]+|
          |[0-9]+|
        """)
      //创建结果集
      val result_arr=new ArrayBuffer[String]
      var status_leave="good"
      //第一遍判断疾病等级

      var status:String = "good"
      //第二遍正则表达式数值过滤
      yourString2.foreach(x=>{

        //判断内膜是否大于12，否则判断囊肿是否大于50
        if(x.contains("内膜")) {
          val reg = regexGetNum.findAllIn(x)
          while (reg.hasNext) {
            if (reg.next().toDouble >= 12) {
              status = "overGG"
              result_arr.append(status_leave)
            }
          }
        }else if(x.contains("囊肿")|x.contains("子宫肌瘤")|x.contains("附件")){
          val reg = regexGetNum.findAllIn(x)
          while (reg.hasNext) {
            if (reg.next().toDouble >= 50) {
              status = "overGG"
              result_arr.append(status_leave)
            }
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
      val arr: ArrayBuffer[String] = zigong(yourString)
      //          arr.foreach(x=>println(x))
      val strings: ArrayBuffer[String] = panduanRXUntil(arr)

      //          strings.foreach(x=>println(x))
      val result_status: String = result(strings)
      result_status
    }

//    strings.foreach(x=>println(x))
//    panduan.foreach(x=>println(x))
//    strings.foreach(x=>println(x))

    println(getStatus(str6))
  }




}
