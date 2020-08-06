package com.haozhuo.datag.com.service.bean

import org.codehaus.jackson.annotate.JsonProperty

import scala.beans.BeanProperty

/**
  * Created by DELL on 2020/5/18 10:03
  */
class GeneralSummary1 extends Serializable{
  @BeanProperty
  @JsonProperty("fw") var fw: String = ""

  @BeanProperty
  @JsonProperty("stdFw") var stdFw: String = ""

  @BeanProperty
  @JsonProperty("isPrivacy") var isPrivacy: String = ""

  @BeanProperty
  @JsonProperty("result") var result: String=""

  @BeanProperty
  @JsonProperty("stdResult") var stdResult: String=""

  @BeanProperty
  @JsonProperty("reviewAdvice") var reviewAdvice: String = ""

  @BeanProperty
  @JsonProperty("summaryAdvice") var summaryAdvice: String = ""

  @BeanProperty
  @JsonProperty("summaryDescription") var summaryDescription: String=""

  @BeanProperty
  @JsonProperty("summaryMedicalExplanation") var summaryMedicalExplanation: String = ""

  @BeanProperty
  @JsonProperty("summaryName") var summaryName: String = ""

  @BeanProperty
  @JsonProperty("summaryReasonResult") var summaryReasonResult: String=""

  @BeanProperty
  @JsonProperty("body") var body: String = ""

  @BeanProperty
  @JsonProperty("checkMode") var checkMode: String = ""

  @BeanProperty
  @JsonProperty("abnormalLabel") var abnormalLabel: String=""

  @BeanProperty
  @JsonProperty("stdSummaryName") var stdSummaryName: String=""


}
