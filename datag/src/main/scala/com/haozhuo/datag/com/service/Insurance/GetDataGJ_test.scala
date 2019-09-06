package com.haozhuo.datag.com.service.Insurance
import scala.collection.mutable._
import scala.util.matching.Regex

object GetDataGJ_test {
  val str1:String="（宫颈刮片）ASC-H（非典型鳞状细胞，不除外CIN II-III）；建议宫颈活检。"
  val str2:String="HPV-DNA检测：HPV-DNA-58型为阳性HPV-DNA检测：HPV-DNA-51型为阳性HPV-DNA检测：HPV-DNA-53型为弱阳性HPV-DNA检测：HPV-DNA-55型为弱阳"
  val str3:String="鳞状上皮内高度病变（HSIL）（CIN 3），不排除浸润癌  "
  val str4:String=" （宫颈刮片）HSIL（高级别鳞状上皮内病变，符合CIN II-III）；建议宫颈活检。"
  val str5:String="阴道残端刮片巴氏I级白带检查清洁度II级，正常范围。"
  val str6:String="阴道残端刮片巴氏II级白带检查清洁度II级，正常范围。"
  val str7:String="gongjingtct\\\": \\\"低级别鳞状上皮病变（LSIL），细胞形态改变提示HPV感染可能"
  val str8:String="HPV检测：HPV[11]低危型为阴性HPV检测：HPV[16]低危危型为阳性HPV检测：HPV[18]高危型为阴性HPV检测："

  def gongjing(yourString: String) ={
    val regex = new Regex(
      """
        |HSIL|HPV.{0,9}高危.{0,5}阳性|
        |CIN.{0,9}III|CIN.{0,6}3|
        |CIN.{0,9}II|CIN.{0,6}2|
        |刮片.{0,9}级|LSIL|ASU-US|
        |HPV.{0,9}低危.{0,5}阳性|
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
    val result_rg=new ArrayBuffer[String]
    var status_leave="good"
    //第一遍判断疾病等级
    yourString2.foreach(x=>{
      if(x.contains("HSIL")){
        result_arr.append("高度鳞状上皮内病变")
      }else if((x.contains("CIN"))&&(x.contains("III"))){
        result_arr.append("CIN癌变")
      }else if((x.contains("高危"))&&(x.contains("HPV"))){
        result_arr.append("HPV:高危阳性")
      }
      else if((x.contains("IV"))&&(x.contains("V"))){
        result_arr.append("刮片等级》III拒保")
      }else if((x.contains("II"))&&(x.contains("CIN"))){
        result_rg.append("转人工：CIN2级")
      }else if((x.contains("CIN"))&&(x.contains("2"))){
        result_rg.append("转人工：CIN2级")
      }else if((x.contains("刮片"))&&(x.contains("3"))){
        result_rg.append("转人工:刮片3级")
      }else if((x.contains("LSIL"))||(x.contains("ASU"))){
        result_rg.append("转人工:LSIL或者ASU")
      }else if((x.contains("HPV"))&&(x.contains("低危"))){
        result_rg.append("转人工:HPV低危阳性")
      }
    })

    var status:String = "good"
    //第二遍正则表达式数值过滤
    yourString2.foreach(x=>{

    })
    //返回结果集合

    var status_result=" "
    if(!result_arr.isEmpty){
      //有病
      status_result="0"
    }else if(!result_rg.isEmpty){
      //转人工
      status_result="2"
    }else{
      status_result="1"
    }
    status_result

  }


  def getStatus(yourString:String)={
    val strings: ArrayBuffer[String] = gongjing(yourString)

    val result_array: String = result(strings)
    result_array


  }
//GJ,HPV没有查到高
  def main(args: Array[String]): Unit = {



val str: String = getStatus(str8)
  println(str)
  }
  //封装方法

}
