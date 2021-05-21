package com.haozhuo.datag.com.service.stdrpt


import collection.JavaConverters._
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import com.haozhuo.datag.com.service.bean.{CheckResult, ChkItem, ReportContent}
import com.haozhuo.datag.service.RptStdService
import com.haozhuo.datag.util.ParseTagRule
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control._

/**
 * 归一化
 * 1、规则匹配
 * 2、映射匹配
 */
object UnifMethod {
	private val logger: Logger = LoggerFactory.getLogger(classOf[UnifMethod])

  class UnifMethod{}


	/**
   * 报告标准化入口方法
   * @param jsonRpt
   * @return
   */
  def stdReport(jsonRpt :String):ReportContent={

	  val report = JSON.parseObject(jsonRpt,classOf[ReportContent])
	  report.checkItems.map(
        x => {
          x.checkResults.map(
            y => {
              val chkItemName = CleanMethod.index_name_replace(x.checkItemName)
              val chkIndexName = CleanMethod.index_name_replace(y.checkIndexName)
              if (RptStdService.indexMap.containsKey(chkItemName + chkIndexName)) {
                UnifMethod.stdItems(chkItemName, chkIndexName, x, y)
              }
              UnifMethod.stdCheckItems(chkItemName, chkIndexName, y)
              //清洗范围
              UnifMethod.stdTextRef(x, y)
              //清洗单位
              y.setStdUnit(stdUnit(y.unit))
			  val beginTime = System.currentTimeMillis
			  getTagRuleSD(y)
			  logger.info("nlpcost：{}ms", System.currentTimeMillis - beginTime)
			})
        }
      )

	  if(report!=null){
		  report.generalSummarys.foreach(x=>{
			  val sugName = x.summaryName
			  val sugKey = CleanMethod.clean_abn_name(sugName)
			  val sugMap = RptStdService.sugMap
			  if(sugMap.containsKey(sugKey)){
				  val sugArray = sugMap.get(sugKey)
				  val std_sug_name = sugArray(0);
				  val body = sugArray(1)
				  val check_mode = sugArray(2)
				  val abnormal_label = sugArray(3)
				  x.setStdSummaryName(std_sug_name)
				  x.setBody(body)
				  x.setCheckMode(check_mode)
				  x.setAbnormalLabel(abnormal_label)
			  }
			  if(CleanMethod.is_numberic_data(x.result)==1){
				  x.setStdResult(CleanMethod.result_value_replace(x.result.trim))
				  if(CleanMethod.textRefClean(x.fw)._4==0){
					  x.setStdFw(CleanMethod.textRefClean(x.fw)._1)
				  }
			  }
		  })
	  }

      report
    }

	def getTagRuleSD(checkResult: CheckResult){
		checkResult.resultTypeId.toInt match {
			case 0 =>
				List("(MAXRANGE,∞)&增高","(0,MINRANGE)&降低").map(execTagRuleSD(_,checkResult)).filter(_._2==1).foreach(x=>checkResult.setIndexTag(checkResult.stdCheckIndexName+x._1))
			case 2 =>
				List("强阳性,阳性&阳性","弱阳性&弱阳性").map(execTagRuleSD(_,checkResult)).filter(_._2==1).foreach(x=>checkResult.setIndexTag(checkResult.stdCheckIndexName+x._1))
			case 1 =>
				if(checkResult.stdCheckIndexName.equals("小结")||checkResult.stdCheckIndexName.equals("结果"))
					checkResult.setIndexTag(JSON.toJSONString(ContentProcessRule.content_process(checkResult.resultValue).asJava,SerializerFeature.BeanToArray).replace("\"","\'"))
		}
	}
	/**
	 *
	 * @param rule 规则
	 * @param checkResult 结果
	 * @return
	 */
	def execTagRuleSD(rule: String,checkResult: CheckResult): (String, Integer) = {
		val cols: Array[String] = rule.split("&")
		val rule_ref = cols(0)
		val tag = cols(1)
		val hit:Byte = checkResult.resultTypeId.toInt match {
			case 0  => ParseTagRule.isNum0(checkResult, "NA", rule_ref, 0, 0)
			case 10 => ParseTagRule.isAbnNum10(checkResult.resultValue,rule_ref,checkResult.lowValueRef,checkResult.highValueRef)
			case 2  => ParseTagRule.isAbnEnum(checkResult.stdResultValue,rule_ref)
			case _	=> 0.toByte
		}
		(tag,hit.toInt)
	}


	/**
	 * 标准单位
	 * @param unit
	 * @return
	 */
	def stdUnit(unit:String):String={
		val lowUnit = unit.trim.toLowerCase
		var stdUnit = unit
		if(RptStdService.unitMap.containsKey(lowUnit)){
			stdUnit = RptStdService.unitMap.get(lowUnit)
		}
		stdUnit
	}

	/**
	 * ItemName,IndexName清洗
	 *
	 * @param item
	 * @param index
	 * @param x
	 * @param y
	 */
	def stdItems(item:String,index:String,x:ChkItem,y:CheckResult)={
		val itemMap = RptStdService.indexMap.get(item+index)
		val stdItemName = itemMap(0)
		val stdIndexName = itemMap(1)
		val indexType = itemMap(2)
		val stdType = itemMap(3)
		x.setStdCheckItemName(stdItemName)
		y.setStdCheckIndexName(stdIndexName)
		y.setCheckIndexType(indexType)
		y.setStdType(stdType)

	}

  /**
   * 范围清洗
   * @param x
   * @param y
   */
  def stdTextRef(x:ChkItem,y:CheckResult): Unit = {
	  //根据结果值判断指标类型
	  val flag = CleanMethod.get_chk_item_data_type(y)
	  if (flag.equals(0)) {
		  y.setResultTypeId(0)
		  val stdValue = CleanMethod.result_value_replace(y.resultValue.trim)
		  y.setStdResultValue(stdValue)
		  val textRef = CleanMethod.textRefClean(y.textRef)
		  if (textRef._4 == 0) {
			  y.setStdTextRef(textRef._1)
			  y.setLowValueRef(textRef._2)
			  y.setHighValueRef(textRef._3)
		  }
	  }else if(flag.equals(2)){
		  y.setResultTypeId(2)
		  val stdValue = CleanMethod.result_value_replace_tag(y.resultValue.trim)
		  y.setStdResultValue(stdValue)
	  }else {
		  y.setResultTypeId(1)
	  }
  }



  /**
   * 部位处理
   * 1.检查方式 匹配 item
   *      if index=描述
   *      {
   *        匹配item：规则如下
   *          拆分item 括号外内容|括号内内容
   *          优先匹配括号外内容：多个部位拼接
   *          如果括号外无匹配：匹配括号内内容：多个部位拼接
   *      }else{
   *        部位表 匹配 index （确保部位表全才行） 按字符长度优先
   *        这种情况一般只有一个部位
   *      }
   *
   * 拼接部位逻辑：
   * 按部位配置表的编码 排序组合成一个新编码，部位表没有就插入，有则不插入
   */



  /**
   * 组合方式 部位匹配，只匹配了indexname
   * @param stdItemId
   * @param itemName
   * @param indexName
   * @param indexRuleMap
   * @return
   */
  def matchCombItemIndexRule(stdItemId:String, itemName:String, indexName:String, indexRuleMap :mutable.HashMap[String,String],list :List[(String,Int)])={

    val loops = new Breaks
    var stdindexid : String = ""
    //一个标准index 一个规则；
//    println(indexRuleMap.toList)
    val t_list = filterListByStdId(stdItemId,list)

    val t_indexRuleMap = indexRuleMap.filter(x=>(x._1.split("\\|\\|")(2).equals("1") || x._1.split("\\|\\|")(2).equals("3")) && x._1.split("\\|\\|")(0).startsWith(stdItemId))
      .map(x=>(x._1,ScalaUtils.formatNumber(x._1.split("\\|\\|")(4).toInt)+"_"+ScalaUtils.formatNumber(x._1.split("\\|\\|")(5).toInt)))
      .toList.sortWith(_._2 > _._2)
//    println(t_indexRuleMap.toList)
    //部位 从indexName 匹配； 检查方式 全部 indexName 后 ItemName ；
    val indexnamemap = matchStdIdListByName(indexName,t_list)

//    println(indexnamemap.toList)

    stdindexid = matchSubCombItemIndexRule(stdItemId,t_indexRuleMap,indexnamemap)
    if (stdindexid.equals("")){
      val instr = getInBracketStr(itemName)
      if (! instr.contains("不含")){
        val itemnamemap = matchStdIdListByName(instr,t_list)
        stdindexid = matchSubCombItemIndexRule(stdItemId,t_indexRuleMap,itemnamemap)
      }

      if(stdindexid.equals("")){
        val outstr = getOutBracketStr(itemName)
        val itemnamemap = matchStdIdListByName(outstr,t_list)
        stdindexid = matchSubCombItemIndexRule(stdItemId,t_indexRuleMap,itemnamemap)
      }
    }


    stdindexid
  }

  def matchSubCombItemIndexRule(stdItemId:String, t_indexRuleMap :List[(String,String)],list :List[(String,String)])={
    val loops = new Breaks
    var stdindexid = ""
    var tmpstdindexcode = ""
    var bool:Boolean = false
    t_indexRuleMap.exists(x=>{
      val arr = x._1.split("\\|\\|")
      //        val regex = (if (StringUtils.isNotEmpty(indexrule) && StringUtils.isNotBlank(indexrule)) indexrule  else "sNulls").r

      tmpstdindexcode = arr(0)  //stdindexid
      val tmpisindexmain = arr(1) //是否可以独立判断
      val tmpindextype = arr(2)  //index_type 取部位
      val tmpindexid = arr(3)  //id
      var t_stdindexid = ""
      var tmpstdid = ""
      var bool:Boolean = false
      if(tmpisindexmain.equals("0")) {
        list.exists(y => {
          tmpstdid = y._1
          tmpstdindexcode.equals(tmpstdid)
        }) match {
          case true => {
            t_stdindexid = tmpstdid
          }
          case false =>
        }
        if(tmpstdindexcode.equals(t_stdindexid) && StringUtils.isNotEmpty(t_stdindexid) && StringUtils.isNotBlank(t_stdindexid)) bool =true
      }
      bool
      }) match {
        case true => stdindexid = tmpstdindexcode
        case false =>
      }

/*    loops.breakable{
      for ((tmpstdcheckid, indexrule) <- t_indexRuleMap) {
        val arr = tmpstdcheckid.split("\\|\\|")
        //        val regex = (if (StringUtils.isNotEmpty(indexrule) && StringUtils.isNotBlank(indexrule)) indexrule  else "sNulls").r

        val tmpstdindexcode = arr(0)  //stdindexid
        val tmpisindexmain = arr(1) //是否可以独立判断
        val tmpindextype = arr(2)  //index_type 取部位
        val tmpindexid = arr(3)  //id
        if(tmpindextype.equals("1") ||tmpindextype.equals("3")){
          //val stdidmap = if(tmpindextype.equals("1")) stdidcheckbodymap else stdidchecktypemap
          //          val name = if (tmpindextype.equals("1")) itemName  else indexName
          //          println(s"tmpstdindexcode=$tmpstdindexcode")
          if(tmpisindexmain.equals("0")){ //先匹配item再匹配index
            //编码前置能匹配到stdItemId
            if(tmpstdindexcode.startsWith(stdItemId)){
              var t_stdindexid = ""
              val loop2 = new Breaks
              loop2.breakable(
                for ((tmpstdid,value) <- list){

                  if (tmpstdindexcode.equals(tmpstdid)){
//                    println(s"tmpstdid=$tmpstdid tmpstdindexcode=$tmpstdindexcode value=$value ")
                    t_stdindexid = tmpstdid
                    loop2.break()

                  }
                })

              //val t_stdindexid = matchStdIdByNameV2(tmpstdindexcode,indexName,t_list)
              //println(s"tmpstdindexcode=$tmpstdindexcode stdItemId=$stdItemId indexName=$indexName t_stdindexid=$t_stdindexid")
              if (tmpstdindexcode.equals(t_stdindexid) && StringUtils.isNotEmpty(t_stdindexid) && StringUtils.isNotBlank(t_stdindexid)){
                stdindexid = tmpstdindexcode
                loops.break()
              }

            }
          }
        }


      }
    }*/
    stdindexid
  }




  /**
   * 部位，检查方式编码获取
   * @param stdid 需要检查字符串
   * @param list 部位关系/检查方式关系
   * @return
   */
  def filterListByStdId(stdid:String,list :List[(String,Int)])={
    //val checkbodyrule = dbClass.checkBodyRule
    val stime = System.currentTimeMillis()

    val map2: mutable.HashMap[String,Int] = mutable.HashMap()

    for ((key, value) <- list) {
      //      println(key)
      val tstr = key.split("\\|\\|")

      val tmpstdid = tstr(0)
      val namelen = tstr(3).toInt
      if (tmpstdid.startsWith(stdid)){
        map2.put(key, namelen)
      }

    }

    map2.toList.sortBy(-_._2)
//    val map = map2.map(x=>(x._1+"=="+x._1.split("\\|\\|")(0)+"_"+x._2.toString,x._2))
//    map.toList.sortWith(_._1.split("==")(1) > _._1.split("==")(1)).map(x=>(x._1.split("==")(0),x._2))
  }


  /**
   * 通过名称获取编码列表
   * @param list
   * @return
   */
  def matchStdIdListByName(indexName:String,list :List[(String,Int)])= {
    val stime = System.currentTimeMillis()

    val stdidmap : mutable.HashMap[String,String] = mutable.HashMap()

    for ((key, value) <- list) {

      val tstr = key.split("\\|\\|")
      val tmpstdid = tstr(0)
      val tmprulestr = tstr(1)
      val rulelist = tmprulestr.split(";;")
      var hit_rule2_num = 0
      var tmpIndexName = indexName.toUpperCase
      //var tmpItemName = itemName.toUpperCase
      val t_value = ScalaUtils.formatNumber(value)+"_"+ScalaUtils.formatNumber(tstr(3).toInt)+"_"+ScalaUtils.formatNumber(tstr(4).toInt)

      for (rule <- rulelist) {

        val regex = (if (StringUtils.isNotEmpty(rule) && StringUtils.isNotBlank(rule)) rule else "sNulls").r
        regex.findFirstIn(tmpIndexName) match {
          case Some(data) => {
//            tmpIndexName = tmpIndexName.replace(data,"")
            hit_rule2_num += 1
          }
          case _ => /*{
            regex.findFirstIn(tmpItemName.toUpperCase) match {
              case Some(data) => {
//                tmpItemName = tmpItemName.replace(data,"")
                hit_rule2_num += 1
              }

              case _ =>
            }
          }*/
        }
      }
//      println(s"rulelist.length="+rulelist.length+ s" hit_rule2_num=$hit_rule2_num tmpstdid=$tmpstdid tmprulestr=$tmprulestr")
      if (rulelist.length == hit_rule2_num) {
        stdidmap.put(tmpstdid, t_value)
      }
    }

//    val t = System.currentTimeMillis - stime
    //    println(s"matchStdIdByNameV2 标准化cost：$t ms")
//    println(stdidmap.toList.sortWith(_._2 > _._2))
    stdidmap.toList.sortWith(_._2 > _._2)
  }





  /**
   * 通过部位，检查方式获取标准itemid，indexid
   * @param name
   * @param keyList
   * @param relList
   * @return
   */
  def matchStdIdByName(name :String, keyList:List[(String,Int)], relList:List[(String,Int)])={
    var stdid :String = ""
    val map: mutable.HashMap[String,Int] = mutable.HashMap()
    val list = ListBuffer[Int]()
    val loops = new Breaks
    loops.breakable{
      for ((key, value) <- keyList) {
        val code = key.split("\\|\\|")(0)
        val name_rule = key.split("\\|\\|")(1)
        val id = key.split("\\|\\|")(2).toInt
        val regex = (if (StringUtils.isNotEmpty(name_rule) && StringUtils.isNotBlank(name_rule)) name_rule  else "sNulls").r

//        println(s"regex=$regex, name=$name")
        regex.findFirstIn(name.toUpperCase) match {
          case Some(data) => {
//            println(s"========$data")
            list += id
          }
          case _ =>
        }
      }
    }
    //具体看部位表从存储结构；判断这个部位是否存在
    //存在获取 code
    //不存在 插入 并且获取code
    stdid = getStdIdRelCode(list, relList)

    stdid
  }

  /**
   * 获取检查方式编码=itemid
   * @param list
   * @return
   */
  def getStdIdRelCode(list :ListBuffer[Int], relList:List[(String,Int)])={

    var stdid :String = ""
    val loops = new Breaks
    loops.breakable{
      for ((key, value) <- relList) {
        val id = key.split("\\|\\|")(0)
        val partidstr = key.split("\\|\\|")(1)
        val code = key.split("\\|\\|")(2)
        val tlist = partidstr.split(",").map(x=>x.trim.toInt).toList
//        println(s"list:$list   ; tlist:$tlist")
        if(list.distinct.sorted == tlist.distinct.sorted){
          stdid = code
          loops.break()
        }
      }
    }
    //不存在，需要插入，并且返回 autocode
    if (!list.isEmpty && stdid.equals("")){

      //代码补全
    }
    stdid
  }





  /**
   * 单独item或者index匹配规则，目前只对非组合有效
   * @param itemName
   * @param indexName
   * @param indexRuleMap
   * @return
   */
  def matchSingleMainRule(itemName:String,indexName:String,indexRuleMap:List[(String,String)])={

    val loops = new Breaks
    var stdindexid : String = ""
	  var indexid :String =""

  	indexRuleMap.exists(x=>{
      val arr = x._1.split("\\|\\|")
      val regex = (if (StringUtils.isNotEmpty(x._2) && StringUtils.isNotBlank(x._2))  x._2 else "sNulls").r
      val tmpstdindexid = arr(0)  //stdindexid
      val tmpisindexmain = arr(1) //是否可以独立判断
      indexid = tmpstdindexid
      if (tmpisindexmain.equals("1") ){
        regex.findFirstIn(indexName) match {
          case Some(data) => true
          case _ => false
        }
      } else if(tmpisindexmain.equals("2")){
        regex.findFirstIn(itemName) match {
          case Some(data) => true
          case _ => false
        }
      }else{
        false
      }
	 // (tmpisindexmain.equals("1") &&indexName.matches(regex))||(tmpisindexmain.equals(2)&&itemName.matches(regex))
  	})match {
		case true =>
			stdindexid =indexid
		case false =>None
	}

    stdindexid
  }

  /**
   * 生成标准编码stdcheckid
   * @param stdIndexId
   * @param codeArr
   * @return
   */
  def generatePropStdCode(stdIndexId :String, codeArr :Array[String], itemType :String)={

    val stime = System.currentTimeMillis()
    var stdcheckid:String = ""
    var prop1:String = "00"
    var prop2:String = "00"
    var prop3:String = "000"
    if(stdIndexId != null && !stdIndexId.equals("")){
      if(itemType.equals("1")||itemType.equals("3")){
        prop1 = codeArr(6)
        prop2 = codeArr(7)
        prop3 = codeArr(9)
      }else{
        prop1 = codeArr(0)
        prop2 = codeArr(1)
        prop3 = codeArr(2)
      }

//      if(!codeArr(8).equals("000")){
//        prop3 = codeArr(8)
//      }else{
//        prop3 = codeArr(5)
//      }
      stdcheckid = stdIndexId + "." + prop1 + "." + prop2 + "." + prop3 + "." + codeArr(3) + "." + codeArr(4) + "." +codeArr(8)+"."+ codeArr(5)

    }
//    val t = System.currentTimeMillis - stime
//    println(s"generatePropStdCode 标准化cost：$t ms")

    stdcheckid
  }

  /**
   * 3 通过映射表获取标准编码(优先匹配）
   * @param itemName
   * @param indexName
   * @return
   */
  def matchItemIndexMap(itemName:String,indexName:String)={
    var stdcheckid:String = ""
    var state :Integer = null
    val loops = new Breaks
    val tmpStr :String = itemName + "_" + indexName
    val itemindexmap   = RptStdService.checkIdMap    //mysql 获取 编码要处理成标准编码
    if(itemindexmap.containsKey(tmpStr)) {
//      println(s"tmpStr=$tmpStr")
      stdcheckid =itemindexmap.get(tmpStr)(0)
      state = itemindexmap.get(tmpStr)(1).toInt
      if(state == 0 ||state == 3){ //0错误数据 or 3不确定
        stdcheckid = "Z.000.000.00.00.000.00.0.9.000"
      }
    }

	  stdcheckid
  }

  /**
   * 获取括号里面内容
   * @param str
   * @return
   */
  def getInBracketStr(str :String)={
    val regex = "(?<=\\(|（)(\\W*)(?=\\)|）)".r //"(?<=\\()(\\W*)(?=\\))".r
    val list = regex.findAllMatchIn(str).toList
    var reStr :String = ""
    for(ts <- list){
      reStr =  ts +  "," + reStr
    }
    reStr
  }

  /**
   * 获取括号外内容
   * @param str
   * @return
   */
  def getOutBracketStr(str :String)={
    str.replaceAll("(?<=\\(|（)(\\W*)(?=\\)|）)","")
  }


  /**
   * 检查项目处理
   * @param item
   * @param index
   * @param y
   * @return
   */
  def stdCheckItems( item:String,index:String,y:CheckResult)={
    var stdcheckid :String = ""
    //2.匹配已经审核的映射
    stdcheckid = UnifMethod.matchItemIndexMap(item,index)

    y.setStdCheckId(stdcheckid)

  }



  def main(args: Array[String]): Unit = {

  }

}

