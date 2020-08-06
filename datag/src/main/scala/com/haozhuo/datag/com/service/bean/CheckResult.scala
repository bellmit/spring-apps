package com.haozhuo.datag.com.service.bean

import org.codehaus.jackson.annotate.JsonProperty

import scala.beans.BeanProperty

/**
  * Created by DELL on 2020/5/18 10:14
  */
class CheckResult extends Serializable{
  @BeanProperty
  @JsonProperty("appendInfo") var appendInfo: String = ""

  @BeanProperty
  @JsonProperty("canExplain") var canExplain: String = ""

  @BeanProperty
  @JsonProperty("result") var result: String=""

  @BeanProperty
  @JsonProperty("checkIndexName") var checkIndexName: String = ""

  @BeanProperty
  @JsonProperty("stdCheckIndexName") var stdCheckIndexName: String = ""

  @BeanProperty
  @JsonProperty("highValueRef") var highValueRef: String = ""

  @BeanProperty
  @JsonProperty("isAbandon") var isAbandon: String=""

  @BeanProperty
  @JsonProperty("isCalc") var isCalc: String = ""

  @BeanProperty
  @JsonProperty("lowValueRef") var lowValueRef: String = ""

  @BeanProperty
  @JsonProperty("resultFlagId") var resultFlagId: Integer=5

  @BeanProperty
  @JsonProperty("resultTypeId") var resultTypeId: Integer = 4

  @BeanProperty
  @JsonProperty("resultValue") var resultValue: String = ""

  @BeanProperty
  @JsonProperty("stdResultValue") var stdResultValue: String = ""

  @BeanProperty
  @JsonProperty("showIndex") var showIndex: String=""

  @BeanProperty
  @JsonProperty("textRef") var textRef: String = ""

  @BeanProperty
  @JsonProperty("stdTextRef") var stdTextRef: String = ""

  @BeanProperty
  @JsonProperty("unit") var unit: String=""

  @BeanProperty
  @JsonProperty("stdType") var stdType: String = ""

  @BeanProperty
  @JsonProperty("checkIndexType") var checkIndexType: String = ""


  override def toString = s"CheckResult($appendInfo, $canExplain, $result, $checkIndexName, $stdCheckIndexName, $highValueRef, $isAbandon, $isCalc, $lowValueRef, $resultFlagId, $resultTypeId, $resultValue, $stdResultValue, $showIndex, $textRef, $stdTextRef, $unit, $stdType, $checkIndexType)"
}

