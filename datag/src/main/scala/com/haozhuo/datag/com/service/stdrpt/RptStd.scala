package com.haozhuo.datag.com.service.stdrpt

import com.alibaba.fastjson.serializer.SerializeFilter
import com.alibaba.fastjson.{JSON, TypeReference}
import com.haozhuo.datag.com.service.bean.{CheckResult, ChkItem, Report}
import com.haozhuo.datag.service.{DataEtlJdbcService, RptStdService}

import scala.io.Source

object RptStd {


  def rptStd(jsonRpt:String): Report={

    val report = JSON.parseObject(jsonRpt,classOf[Report])
    if(report.obj.reportContent!=null){
      report.obj.reportContent.checkItems.map(
        x => {
          x.checkResults.map(
            y => {
              val chkItemName = CleanMethod.index_name_replace(x.checkItemName)
              val chkIndexName = CleanMethod.index_name_replace(y.checkIndexName)
              if (RptStdService.indexMap.containsKey(chkItemName + chkIndexName)) {
                //println(chkItemName + chkIndexName)
                stdItems(chkItemName, chkIndexName, x, y)
              }
              //清洗范围
              stdTextRef(x, y)
              //清洗单位
              y.setUnit(y.unit.trim)
            })
        }
      )
    }
    if(report.obj.reportContent!=null){
      report.obj.reportContent.generalSummarys.map(x=>{
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

    if(report.obj.birthday!=null){
      try {
        val age = Integer.valueOf(report.obj.checkDate.substring(0,4))-Integer.valueOf(report.obj.birthday.substring(0,4))
        report.obj.setAge(age)
      }catch {
        case ex:Exception=>
          //logger.info("年龄计算错错误："+ex)
      }
    }
    if(!bmi.equals("")){
      report.obj.bmi=bmi
    }else if(!height.equals("") && !weight.equals("")){
      //int(float(weight) / (0.0001 * float(height) * float(height)) * 2) / 2
      bmi = ((weight.toFloat/(0.0001* height.toFloat*height.toFloat)*2).toInt/2).toString
      report.obj.bmi = bmi
    }

    //logger.info("标准化cost：{}ms", System.currentTimeMillis - beginTime)
    //val repo:String = JSON.toJSONString(report,new Array[SerializeFilter](0))
    report
  }

  var height = "";
  var bmi ="";
  var weight = "";


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
    //itemMap.foreach(print(_))
    val stdItemName = itemMap(0)
    val stdIndexName = itemMap(1)
    val indexType = itemMap(2)
    val stdType = itemMap(3)
    if(stdIndexName.equals("身高")){
      height = y.getResultValue
    }
    if(stdIndexName.equals("体重")){
      weight = y.getResultValue
    }
    if(stdIndexName.equals("体重指数")){
      bmi = y.getResultValue
    }
    x.setStdCheckItemName(stdItemName)
    y.setStdCheckIndexName(stdIndexName)
    y.setCheckIndexType(indexType)
    y.setStdType(stdType)

  }


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
    }else {
      y.setResultTypeId(1)
    }
  }


  def readFileLine(path:String)= {
    var rpt = "";
    val file = Source.fromFile(path)
    for (line <- file.getLines) {
      rpt = line
    }
    file.close
    rpt
  }


  def main(args: Array[String]): Unit = {
    val str ="E:\\spring-apps\\datag\\src\\main\\excel\\d.txt";
    val jsonrpt = readFileLine(str)
    print(jsonrpt)
    //CapHttpPost.postUrl(jsonrpt)
    //val rpt = rptStd(jsonrpt)
    //rpt.obj.reportContent.checkItems.foreach(print(_))
  }

}
