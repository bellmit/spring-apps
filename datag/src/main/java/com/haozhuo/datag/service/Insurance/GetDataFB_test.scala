package com.haozhuo.datag.service.Insurance

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

object GetDataFB_test {

  def main(args: Array[String]): Unit = {
    val str1:String="见高密度钙化结节。诊断：1.慢性支气管炎，肺气肿；2.双肺多发间质纤维化；3.右肺上叶及下叶亚实性病变，建议穿刺活检。4.主动脉及冠状动脉钙化。"
    val str2:String="影像表现：双肺支气管血管束走行、分布紊乱。左肺上叶尖后段见多发条索、斑片、树芽影及不规则钙化影，边界均清"

    //方法结束

    println(feibu(str2))
  }
  def feibu(yourString: String) ={
    val regex = new Regex(
      """
        |肺部罗音|
        |肺动脉增宽|肺间质性改变|
        |肺结核|
        |肺结节影|
        |肺门影增大|
        |肺内结节影|
        |肺内硬结灶|
        |肺气肿|
        |肺炎|
        |肺野内密度增高|影性质待查|
        |肺转移瘤|慢性支气管炎|纵隔增宽|
      """)

    val reg = regex.findAllIn(yourString)

    val strings = new ArrayBuffer[String]
    while (reg.hasNext){
      val str: String = reg.next()
      strings.append(str)
    }
    //返回结果集合
    var status_result=" "
    if(strings.isEmpty){
      status_result="1"
    }else{
      status_result="0"
    }
    status_result
  }
}
