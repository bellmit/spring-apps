package com.haozhuo.datag.service.Insurance

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

object GetDataXDT_test {
  def main(args: Array[String]): Unit = {
    val str1:String = "窦性心动过缓并不齐  平均心率59次/分T波改变（  V3 V4 V5 V6 低平 ）完全性右束支传导阻滞 "
    val str2:String ="窦性心动过缓并不齐  平均心率59次/分过早复极 "
    val str3:String="窦性心动过缓并不齐  平均心率43次/分左心室高电压III度房室传导阻滞T波形态高尖 "
    val str4:String="窦性心动过缓   HR59次/分  II III avF V1-V9 ST-T改变(广泛性冠状动脉供血不足)  心电轴左偏  左心室肥大伴劳损 "
    val str5:String="窦性心律左心室肥大伴劳损频发房性早搏大部分呈二联律  "


  def xindiantu(yourString:String)= {


    val regex = new Regex(
      """
        |[0-9]+mm×[0-9]+mm×[0-9]+mm|[0-9]+cm×[0-9]+cm×[0-9]+cm|
        |[0-9]+mmx[0-9]+mmx[0-9]+mm|[0-9]+cmx[0-9]+cmx[0-9]+cm|
        |[0-9]+mmX[0-9]+mmX[0-9]+mm|[0-9]+cmX[0-9]+cmX[0-9]+cm|
        |[0-9]+mmx[0-9]+mm|[0-9]+mmX[0-9]+mm|
        |[0-9]+mm\*[0-9]+mm|
        |[0-9]+cmx[0-9]+cm|[0-9]+cmX[0-9]+cm|
        |[0-9]+cm\*[0-9]+cm|
        |[0-9]+x[0-9]+mm|[0-9]+X[0-9]+mm|
        |[0-9]+x[0-9]+cm|[0-9]+X[0-9]+cm|
        |[0-9]+\*[0-9]+mm|[0-9]+\*[0-9]+cm|
        |[0-9]+×[0-9]+mm|[0-9]+×[0-9]+cm|[0-9]+mm×[0-9]+mm|[0-9]+cm×[0-9]+cm|
        |[0-9]+\.+[0-9]+x[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+x[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+\*[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+×[0-9]+\.+[0-9]+cm|
        |[0-9]+\.+[0-9]mm+×[0-9]+\.+[0-9]mm|
        |[0-9]+\.+[0-9]+cm|[0-9]+\.[0-9]+mm|
        |[0-9]+cm|[0-9]+mm|
        |心率.{0,10}[0-9]+|
        |III.{0,5}房室传导阻滞|
        |完全性.{0,10}束支传导阻滞|
        |室性早搏二联律|左心室肥大伴劳损|室性早搏三联律|
        |心房纤颤|心肌梗塞|心肌缺血|
      """)
    //心率小于45，大于120
    val reg = regex.findAllIn(yourString)

    val strings = new ArrayBuffer[String]

    while (reg.hasNext) {
      strings.append(reg.next())

    }
    strings
  }//方法结束


  //返回结果方法
  def result(yourString2:ArrayBuffer[String])={

  //拉取数字正则表达式
    val regexGetNum = new Regex(
      """
        |[0-9]+\.[0-9]+|
        |[0-9]+|
      """)

    val regexText = new Regex(
      """
        |III.{0,5}房室传导阻滞|
        |完全性.{0,10}束支传导阻滞|
        |室性早搏二联律|左心室肥大伴劳损|室性早搏三联律|
        |心房纤颤|心肌梗塞|心肌缺血|
      """)
    //创建结果集
    val result_arr=new ArrayBuffer[String]
    var status_leave="good"
    //第一遍判断疾病等级
    yourString2.foreach(x=>{
      if(x.contains("房室传导阻滞")) {
        status_leave="拒保：房室传导阻滞III级"
        result_arr.append(status_leave)
      }
      if(x.contains("束支传导阻滞")){
        status_leave="拒保：束支传导阻滞"
        result_arr.append(status_leave)
      }
      //判断所有包含在内的
      val regex=regexText.findAllIn(x)
      while (regex.hasNext){
//        println(x)
        result_arr.append(regex.next().toString)
      }

    })


    var status:String = "ok"
    //第二遍正则表达式数值过滤
    yourString2.foreach(x=>{

      //判断心率
      if(x.contains("心率")){

      val reg=regexGetNum.findAllIn(x)
      while (reg.hasNext){
        val x=reg.next()
        if((x.toDouble>=120)||x.toDouble<=45){
          status = "拒保:心率过缓或过快"
          result_arr.append(status)
        }

      }
      }




    })
    //返回结果集合
    var status_result=" "
    if(result_arr.isEmpty){
      status_result="1"
    }else{
      status_result="0"
    }
    status_result

  }






    def getStatus(yourString:String)= {
      val arr: ArrayBuffer[String] = xindiantu(yourString)
      //          arr.foreach(x=>println(x))
      val result_status: String = result(arr)
      result_status
    }

    println(getStatus(str5))

  }





}
