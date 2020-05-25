package com.haozhuo.datag.com.service.Insurance
import scala.util.matching.Regex

object getBeiShu {
  // 匹配汉字
  private val match_unicode = """[\u4e00-\u9fa5]"""
  private val text_ref_replace_pattern1 = """^(->|>=|〉|>|﹥|＞|≥)"""
  private val text_ref_replace_pattern2 = """^(-<=|-<|-＜|<=|<|＜|<)"""
  private val text_ref_replace_pattern3 = """^(-\d)"""
  private val text_ref_replace_pattern4 = """-<=|-＜=|-<|-≤|-＜|<=|＜=|≤|<|＜|〈|〈"""
  private val text_ref_replace_pattern5 = """-≤|->|~~|\-\-|~|～|－"""
  private val text_ref_replace_pattern6 = """(^[0-9]\d*$)|(^[0-9]\d*\.\d*|0\.\d*[1-9]\d*$)"""


  def main(args: Array[String]): Unit = {


    val str1: String = "0.63 ↑"



     val d: Double =
    getBeiShu(78.9, filter("≤26"))
    println(d)
  }
  def is_number(n:String)= {
    var flag:Integer = 2
    try{
      n.toDouble
      flag=1
    }catch {
      case ex: Exception =>
        flag = 0
    }
    flag
  }
  /**
    * 范围清洗
    * @param textRef1
    * @return
    */

  def textRefClean(textRef1:String)={
    var textRef:String = textRef1
    var flag:Integer = 2
    var stdTextRef:String = ""
    var lowTextRef:String = ""
    var higeTextRef:String = ""
    if(textRef.matches(match_unicode)){
      textRef = textRef.replace(" ","")
    }else if(textRef.matches(text_ref_replace_pattern1)){
      textRef = textRef.replaceAll(text_ref_replace_pattern1,"")+"-Inf"
    }else if(textRef.matches(text_ref_replace_pattern2)){
      textRef = textRef.replaceAll(text_ref_replace_pattern2,"0.00-")
    }else if(textRef.matches(text_ref_replace_pattern3)){
      textRef = "0.00"+ textRef
    }else{
      textRef = textRef.replaceAll(text_ref_replace_pattern4,"0.00-").replaceAll(" ","")
        .replaceAll(text_ref_replace_pattern5,"-")
    }

    if(textRef.contains("-")){
      lowTextRef = textRef.split("-")(0)
      higeTextRef = textRef.split("-")(1)
      if(lowTextRef.matches(text_ref_replace_pattern6)&&higeTextRef.matches(text_ref_replace_pattern6)){
        flag=0
      }else{}
    }else{
      flag = 1
    }
    stdTextRef = lowTextRef+"-"+higeTextRef
    (stdTextRef,lowTextRef,higeTextRef,flag)
  }


  def filterChaobiao(yourString: String) = {
    val regx = new Regex(
      """
        |[0-9]+.[0-9]+|
      """)

    val reg = regx.findAllIn(yourString)

    var max=" "
    while (reg.hasNext) {
      max=reg.next()
    }
    print("范围："+max)
    max
  }

  //目前支持替换~,-的比值(:)
  def filter(text_ref:String)={
    //替换数组
   val matchTh= Array(
      ("0-","-<"),
      ("-","~"),
     (":","：")
    )
    var text_ref_result=text_ref
    matchTh.foreach(r=>{
      if(text_ref.contains(r._2)) {
        text_ref_result=text_ref.replaceAll(r._2,r._1)
      }
    })
    //替换


    if(text_ref_result.contains(":")&&text_ref_result.contains("-")){
    //分开数据 去掉：
    val qieg: Array[String] = text_ref_result.split("-")
    val qiebi1: Array[String] = qieg(0).split(":")
    val qiebi2: Array[String] = qieg(1).split(":")
    val q1=qiebi1(0).toDouble/qiebi1(1).toDouble
    val q2=qiebi2(0).toDouble/qiebi2(1).toDouble
      q1+"-"+q2
    }else{
     text_ref_result
    }
  }

  def getBeiShu(rs_val: Double, text_ref: String) = {
    var result = rs_val
    if(text_ref.isEmpty){
      result=0
    }else {
      val min = textRefClean(text_ref)._2
      val max = textRefClean(text_ref)._3
      if (!min.equals("") && !max.equals("")) {
        if (min.toDouble < rs_val && rs_val < max.toDouble) {
          //println("合理范围")
        } else if (rs_val >= max.toDouble) {
          val d: Double = rs_val.toDouble / max.toDouble
          result = d
        } else if (rs_val < min.toDouble) {
          val d: Double = rs_val / min.toDouble
          result = d
        }
      }
    }
      result
    }


  //参数描述:第一个你传递的值Double类型比如10,第二个是你传递的取值范围比如0.9-100
  //返回结果，如果是正常，则返回原始值，不正常，则返回倍数
  def getBeiShu2(rs_val: Double, text_ref: String) = {
    var result = rs_val

    if(text_ref.isEmpty){
      result=0
    }

//1
    //判断<
    //正则表达式获取数值
    if(text_ref.contains("<")){
      val max2: String = filterChaobiao(text_ref)
      val d: Double = rs_val / max2.toDouble
      result = d
    }else if(text_ref.contains("-")){
      val strings: Array[String] = text_ref.split("-")
      val min: Double = strings(0).toDouble
      val max: Double = strings(1).toDouble
      if (min < rs_val && rs_val < max) {
        //println("合理范围")
      } else if (rs_val >= max) {
        val d: Double = rs_val / max
        result = d
      } else if (rs_val < min) {
        val d: Double = rs_val / min
        result = d
      }
    } //else结束
    result
  }
}
