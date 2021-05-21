package com.haozhuo.datag.com.service.stdrpt

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import org.apache.http.HttpHost

object ReportEsSink extends Serializable{

  val host =""

  private val newindex = "report_std"
  private val indexType ="report"


  def getAddresses(): java.util.ArrayList[HttpHost]  ={
    val httpHosts: java.util.ArrayList[HttpHost] = new java.util.ArrayList()
    host.split(",").map(x=>{
      val parts = x.split(":")
      if(parts.length>1){
        httpHosts.add(new HttpHost(parts(0), Integer.parseInt(parts(1))));
      }
    })
    httpHosts
  }

}
