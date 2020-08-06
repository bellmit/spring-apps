package com.haozhuo.datag.com.service.bean

import org.codehaus.jackson.annotate.JsonProperty

import scala.beans.BeanProperty

/**
  * Created by DELL on 2020/5/18 9:57
  */
class ReportContent extends Serializable{

  @BeanProperty
  @JsonProperty("checkItems") var checkItems: Array[ChkItem] = null

  @BeanProperty
  @JsonProperty("generalSummarys") var generalSummarys: Array[GeneralSummary1] = null

  @BeanProperty
  @JsonProperty("generalSummarys2") var generalSummarys2: Array[String]=null

  override def toString = s"ReportContent($checkItems, $generalSummarys, $generalSummarys2)"
}
