package com.haozhuo.datag.service.Insurance

import scala.util.matching.Regex
import scala.collection.mutable._

object GetDataGJ_test {
  val str1:String="（宫颈刮片）ASC-H（非典型鳞状细胞，不除外CIN II-III）；建议宫颈活检。"
  val str2:String="HPV-DNA检测：HPV-DNA-58型为阳性HPV-DNA检测：HPV-DNA-51型为阳性HPV-DNA检测：HPV-DNA-53型为弱阳性HPV-DNA检测：HPV-DNA-55型为弱阳"
  val str3:String="鳞状上皮内高度病变（HSIL）（CIN 3），不排除浸润癌  "
  val str4:String=" （宫颈刮片）HSIL（高级别鳞状上皮内病变，符合CIN II-III）；建议宫颈活检。"

//GJ,HPV没有查到高
  def main(args: Array[String]): Unit = {



     val str: String = getStatus(str3)
   println(str)
  }
  def gongjing(yourString: String) ={
    val regex = new Regex(
      """
        |HSIL|
        |CIN.{0,9}III|CIN.{0,6}3|
      """)

    val reg = regex.findAllIn(yourString)

    val strings = new ArrayBuffer[String]
    while (reg.hasNext){
      val str: String = reg.next()
      strings.append(str)
    }
    strings
  }
  //方法结束

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
    yourString2.foreach(x=>{
      if(x.contains("HSIL")){
        result_arr.append("高度鳞状上皮内病变")
      }else if(x.contains("CIN")){
        result_arr.append("CIN癌变")
      }
    })

    var status:String = "good"
    //第二遍正则表达式数值过滤
    yourString2.foreach(x=>{

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


  //封装方法
  def getStatus(yourString:String)={
    val strings: ArrayBuffer[String] = gongjing(yourString)

    val result_array: String = result(strings)
    result_array


  }
}
