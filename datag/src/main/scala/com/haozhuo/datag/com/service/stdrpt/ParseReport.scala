package com.haozhuo.datag.com.service.stdrpt

import com.alibaba.fastjson.JSON
import com.haozhuo.datag.com.service.bean.{Desc, Report, ReportContent, Result}

object ParseReport {

  def parseReport(reportContent:String): Desc ={
    val report = JSON.parseObject(reportContent,classOf[ReportContent])
    val desc = new Desc
    report.checkItems.filter(x=>
      {(x.checkItemName.contains("胸")||x.checkItemName.contains("肺"))&&x.checkItemName.contains("CT")}
    ).map(
      x=>{
        val result = new Result
        x.getCheckResults.map(y=> {
          if(y.stdCheckIndexName.equals("小结")){
            result.setConclusion(y.resultValue)
          }else if(y.stdCheckIndexName.equals("描述")){
            //println(y.stdCheckIndexName)
            result.setDescribe(y.resultValue)
          }
        }
        )

        desc.setResult(result)
        //println(x.checkItemName)
        if(result.getDescribe!=""||result.getConclusion!="") {
          desc.setFlag(1)
        }
        return desc
      }
    )

    if(desc.result!=null){
      if(desc.result.getDescribe!=""||desc.result.getConclusion!="") {
        desc.setFlag(1)
      }
    }
    desc
  }


}
