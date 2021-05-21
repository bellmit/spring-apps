package com.haozhuo.datag.com.service.stdrpt

import java.text.SimpleDateFormat
import java.util.Calendar

import com.haozhuo.datag.com.service.bean.CheckResult
import org.slf4j.{Logger, LoggerFactory}


/**
  * Created by DELL on 2020/5/19 15:36
  */
object CleanMethod {
  private val logger: Logger = LoggerFactory.getLogger(classOf[CleanMethod])
  //异常名称清洗
  private val not_needed_symbols_replace_pattern =  """[\s+`~!@#$%^&*()=|{}'\[\].。<>/?！￥…（）—\-【】 ‘;；:：”“’,，、？\\]"""
  //阴阳性结果匹配模式
  private val positive_or_negative_match_pattern1 = """^[+-]+(/(hp|HP))?$.*""".r.toString()
  private val positive_or_negative_match_pattern_2 = """^[\d-]+/(hp|HP)$.*""".r.toString()
  private val positive_or_negative_match_pattern_3 = """^(hp|HP|弱|强)?[阴阳]性.*""".r.toString()

  //数值型指标项检查结果
  private val numeric_chk_item_value_pattern = """^[0-9]+[0-9Ee.]+([↓↑]?(\([\u4e00-\u9fa5,*].*?\))?|次/分|(mmol|umol|CELL|g)/u?L)?$""".r
  // 非数字字符
  private val non_decimal = """[^\d.]+"""

  // 匹配汉字
  private val match_unicode = """[\u4e00-\u9fa5]"""
  //^(->|>=|〉|>|﹥|＞|≥)  ^(-<=|-<|-＜|<=|<|＜|<) ^(-\d) -<=|-＜=|-<|-≤|-＜|<=|＜=|≤|<|＜|〈|〈
 // -≤|->|~~|\-\-|~|～|－  (^[0-9]\d*$)|(^[0-9]\d*\.\d*|0\.\d*[1-9]\d*$)
  private val text_ref_replace_pattern1 = """^(->|>=|〉|>|﹥|＞|≥)"""
  private val text_ref_replace_pattern2 = """^(-<=|-<|-＜|<=|<|＜|<)"""
  private val text_ref_replace_pattern3 = """^(-\d)"""
  private val text_ref_replace_pattern4 = """-<=|-＜=|-<|-≤|-＜|<=|＜=|≤|<|＜|〈|〈"""
  private val text_ref_replace_pattern5 = """-≤|->|~~|\-\-|~|～|－"""
  private val text_ref_replace_pattern6 = """(^[0-9]\d*$)|(^[0-9]\d*\.\d*|0\.\d*[1-9]\d*$)"""

  def clean_abn_name(abn_name:String)= {
    abn_name.replaceAll(not_needed_symbols_replace_pattern,"")
    .replaceAll("Ⅰ", "1")
    .replaceAll("Ⅱ", "2")
    .replaceAll("Ⅲ", "3")
    .toLowerCase()
  }


  //checkItemName，checkIndexName 用以下函数对特殊字符处理
  def index_name_replace(index_name1:String) = {
    var index_name = index_name1
    if(index_name!=""){
    index_name = index_name.replace(" ", "").toUpperCase
    index_name = index_name.replace("*", "")
    index_name = index_name.replace("【", "[").replace("】", "]")
    index_name = index_name.replace("（", "(").replace("）", ")")
    index_name = index_name.replace("[", "(").replace("]", ")")
    index_name = index_name.replace("—", "-").replace("－", "-").replace("--", "-")
    index_name = index_name.replace("_", "-")
    index_name = index_name.replace("★", "")
    index_name = index_name.replace("Ⅰ", "I")
    index_name = index_name.replace("Ⅱ", "II")
    if (index_name.charAt(0).toString=="-") {
      index_name = index_name.substring(1, index_name.length)
    }
    if (index_name!=""){
    if (index_name.charAt(index_name.length-1).toString==(".")){
      index_name = index_name.substring(0, index_name.length - 1)
    }
   }
    }
    index_name
  }

  /**
    * 根据结果值判断指标类型
    * @param checkResult
    * @return 0-数值型;1-描述型;2-阴阳性
    */
  def get_chk_item_data_type(checkResult: CheckResult)={
    var flag:Integer = 0
    if(checkResult.resultValue.matches(positive_or_negative_match_pattern1)|| checkResult.resultValue.matches(positive_or_negative_match_pattern_2)
    || checkResult.resultValue.matches(positive_or_negative_match_pattern_3)){
      flag = 2
    }else if(checkResult.resultFlagId == 4){
      if (checkResult.resultValue.length> 20) {flag = 1}
      else {flag =2}
    }else if(checkResult.resultFlagId==2 || checkResult.resultFlagId==3 || is_numberic_data(checkResult.resultValue) ==1){
      flag =0
    }else{
      flag =1
    }
    flag
  }

  /**
    * 判断是否为数值型数据
    * @param result
    * @return
    */
  def is_numberic_data(result:String)= {
    var value = result.replace(" ","")
    var flag:Integer = 0
    if (numeric_chk_item_value_pattern.findAllIn(value).length!=0) {
      val vDouble = value.replaceAll(non_decimal,"")
      try{
        vDouble.toDouble
        flag=1
      }catch {
        case ex: Exception =>
          logger.info("value:{}",value)
          flag = 2
      }
    }else{
      flag = 3
    }
    flag
  }

  /**
    * 判断是否为纯数字 1 是
    * @param result
    * @return
    */
  def is_number(result:String)= {
    var flag:Integer = 2
    try{
      result.toDouble
      flag=1
    }catch {
      case ex: Exception =>
        logger.info("value:{}",result)
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
      }else  logger.info("范围上下限清洗错误")
    }else{
      flag = 1
      logger.info("text_ref不含有-")
    }
    stdTextRef = lowTextRef+"-"+higeTextRef
    (stdTextRef,lowTextRef,higeTextRef,flag)
  }



  /** 范围清洗,flag= 0 表示清洗成功
    *
    * @param text_ref1
    * @return (text_ref,text_ref_lower,text_ref_upper,flag)
    */
  @Deprecated
  def text_ref_replace(text_ref1:String)= {
    var flag:Integer = 0
    var text_ref_lower:String = ""
    var text_ref_upper:String = ""
    var text_ref = text_ref1.replace(" ","")
    text_ref = text_ref.replace("-<=", "0.00-")
    text_ref = text_ref.replace("-＜=", "0.00-")
    text_ref = text_ref.replace("-<", "0.00-")
    text_ref = text_ref.replace("-＜", "0.00-")

    text_ref = text_ref.replace("<=", "0.00-")
    text_ref = text_ref.replace("＜=", "0.00-")
    text_ref = text_ref.replace("<", "0.00-")
    text_ref = text_ref.replace("＜", "0.00-")
    text_ref = text_ref.replace("〈", "0.00-")
    text_ref = text_ref.replace("-≤", "0.00-")
    text_ref = text_ref.replace("〈", "0.00-")
    if(text_ref.startsWith("≤")){
      text_ref = text_ref.replace("≤","0.00-")
    }

    if ( text_ref.contains("〉") ){
      text_ref = text_ref.replace("〉", "")
      text_ref = text_ref + "-Inf"
    }
    if ( text_ref.contains("->") ){
      text_ref = text_ref.replace("〉", "")
      text_ref = text_ref + "-Inf"
    }
    if ( text_ref.contains(">")) {
      text_ref = text_ref.replace(">", "")
      text_ref = text_ref + "-Inf"
    }
    if ( text_ref.contains("﹥"))
      text_ref = text_ref.replace("﹥", "")
      text_ref = text_ref + "-Inf"
    if(text_ref.contains("＞")) {
      text_ref = text_ref.replace("＞", "")
      text_ref = text_ref + "-Inf"
    }
    if(text_ref.contains("》")) {
      text_ref = text_ref.replace("》", "")
      text_ref = text_ref + "-Inf"
    }
    if ( text_ref.contains("--")) text_ref = text_ref.replace("--", "-")
    if ( text_ref.contains("～")) text_ref = text_ref.replace("～", "-")
    if ( text_ref.contains("~")) text_ref = text_ref.replace("~", "-")
    if (text_ref.contains("-")){
    text_ref_lower = text_ref.split("-")(0)
    text_ref_upper = text_ref.split("-")(1)
      if (is_number(text_ref_lower)==1 && is_number(text_ref_upper)==1){
        text_ref_lower = text_ref_lower.toDouble.toString
        if(text_ref_upper.equals("Inf")){
          text_ref_upper = text_ref_upper
        }else{
          text_ref_upper = text_ref_upper.toString
        }
        text_ref = text_ref_lower + "-" + text_ref_upper
      }
      else
      {
        flag = 1
        text_ref = text_ref1 + "(数据错误)"
      }
    }
    else{
      flag =2
      text_ref = text_ref1 + "(数据错误)"
    }

    (text_ref,text_ref_lower,text_ref_upper,flag)
  }

  /**
    * 结果值清理
    * @param result_value
    * @return
    */
  def result_value_replace(result_value:String)= {
    var value:String = result_value
    value = value.replaceAll(non_decimal,"")
    if (value.endsWith(".")){
      value = value.dropRight(1)
    }
    try {
      value = value.toDouble.toString
    }catch {
      case ex:Exception=>
        logger.info("不能转成纯数字："+ex)
      value = value
    }
    value
  }
	def result_value_replace_tag(result_value:String)= {
		var value:String = result_value
		value = result_value.split("\\(")(0).split("（")(0).split("/")(0)
		val qylist = List("强阳","3+","4+","+3","+4","+++","++++")
		val ruolist = List("弱阳","+-")
		val ylist = List("阳性","++","+","+2","+1","1+","2+")
		if(result_value.contains("阳性")&&result_value.contains("阴性")) value="数据错误"
		else if(qylist.filter(value.contains(_)).nonEmpty) value="强阳性"
		else if(ruolist.filter(value.contains(_)).nonEmpty) value="弱阳性"
		else if(ylist.filter(value.contains(_)).nonEmpty) value="阳性"
		else value = "阴性"
		value
	}

class CleanMethod{}

  def getTimeSlot(slot:Int)={
    val cal = Calendar.getInstance()
    cal.add(Calendar.MINUTE,slot)
    val time =new SimpleDateFormat("yyyyMMddHHmmss")
    time.parse(time.format(cal.getTime))
    time
  }

  def main(args: Array[String]): Unit = {
    val name = clean_abn_name(" -糖尿病Ⅱ‘")
    println("异常："+name)
    val checkResult = new CheckResult
    checkResult.setResultValue("2.82")
    checkResult.setResultFlagId(1)
    val result = get_chk_item_data_type(checkResult)
    //范围清洗
    val ref = text_ref_replace("100-300 10^9/L ")
    println("范围："+ref)
    val value = result_value_replace("10.67 ↑")
    println(value)
    val cleanName = clean_abn_name("血小板计数（PLT） 增高")
    println
    ("异常名称："+cleanName)
    val numdate = is_numberic_data("0.63")
    println("数值型数据："+numdate)
    val textref = textRefClean("0-3")
    println(textref)

	val typeID = is_numberic_data("2")
	  println(typeID)

  }
}
