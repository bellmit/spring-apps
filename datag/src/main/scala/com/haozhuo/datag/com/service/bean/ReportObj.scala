package com.haozhuo.datag.com.service.bean

import org.codehaus.jackson.annotate.JsonProperty

import scala.beans.BeanProperty

class ReportObj extends Serializable {
  @BeanProperty
  @JsonProperty("healthReportId") var healthReportId: String = ""
  @BeanProperty
  @JsonProperty("age") var age: Integer = 0
  @BeanProperty
  @JsonProperty("name") var name: String = ""
  @BeanProperty
  @JsonProperty("userId") var userId: String = ""
  @BeanProperty
  @JsonProperty("idCardNoMd5") var idCardNoMd5: String = ""
  @BeanProperty
  @JsonProperty("birthday") var birthday: String = ""
  @BeanProperty
  @JsonProperty("sex") var sex: String = ""
  @BeanProperty
  @JsonProperty("checkUnitCode") var checkUnitCode: String = ""
  @BeanProperty
  @JsonProperty("checkUnitName") var checkUnitName: String = ""
  @BeanProperty
  @JsonProperty("reportContent") var reportContent: ReportContent = null
  @BeanProperty
  @JsonProperty("checkDate") var checkDate: String = ""
  @BeanProperty
  @JsonProperty("dwdm") var dwdm: String = ""
  @BeanProperty
  @JsonProperty("dwmc") var dwmc: String = ""
  @BeanProperty
  @JsonProperty("userLoadTime") var userLoadTime: String = ""
  @BeanProperty
  @JsonProperty("reportCreateTime") var reportCreateTime: String = ""
  @BeanProperty
  @JsonProperty("lastUpdateTime") var lastUpdateTime: String = ""
  @BeanProperty
  @JsonProperty("addReportTime") var addReportTime: String = ""

  override def toString = s"Report($healthReportId, $name, $userId, $idCardNoMd5, $birthday, $sex, $checkUnitCode, $checkUnitName, $checkDate, $dwdm, $dwmc, $userLoadTime, $reportCreateTime, $lastUpdateTime,$reportContent)"
}
