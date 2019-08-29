package com.haozhuo.datag.service.Insurance

object getBeiShu {

  import scala.collection.mutable._
  import scala.util.matching.Regex


  def main(args: Array[String]): Unit = {


    val str1: String = "0.63 ↑"



     val d: Double =
    getBeiShu(78.9, "-<20.00")
    println(d)
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
    max
  }


  //参数描述:第一个你传递的值Double类型比如10,第二个是你传递的取值范围比如0.9-100
  //返回结果，如果是正常，则返回原始值，不正常，则返回倍数
  def getBeiShu(rs_val: Double, text_ref: String) = {

    var result = rs_val
    //判断<
    //正则表达式获取数值
    if(text_ref.contains("<")){
      val max2: String = filterChaobiao(text_ref)
      val d: Double = rs_val / max2.toDouble
      result = d
    }else {
      val strings: Array[String] = text_ref.split("-")
      val min: Double = strings(0).toDouble
      val max: Double = strings(1).toDouble

      if (min < rs_val && rs_val < max) {
        println("合理范围")
      } else if (rs_val >= max) {
        val d: Double = rs_val / max
        result = d
      } else if (rs_val < min) {
        val d: Double = rs_val / min
        result = d
      }


    }

    result
  }
}
