package com.haozhuo.datag.com.service.stdrpt

import java.io.{FileInputStream, InputStream}
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import org.slf4j.LoggerFactory

object ScalaUtils {
  val logger = LoggerFactory.getLogger(getClass())

  implicit class Ternary[T](bool:Boolean){
    def ?(a: => T, b: => T): T = if (bool) a else b
  }

  def readFile(path: String): InputStream = {
    var in: InputStream = null
    val hdfsPartten = "hdfs://[0-9a-zA_Z.:]+/".r
    val reg = hdfsPartten.findFirstIn(path)
    if (reg.isDefined) {
      logger.info("HDFS命名空间:{}", reg.get)
      logger.info("加载HDFS中的文件:{}", path)
//      val hadoopConf = new org.apache.hadoop.conf.Configuration()
//      val hdfs = org.apache.hadoop.fs.FileSystem.get(new java.net.URI(reg.get), hadoopConf)
//      in = hdfs.open(new org.apache.hadoop.fs.Path(path)).asInstanceOf[java.io.InputStream]
    } else {
      logger.info("加载本地目录下的文件:{}", path)
      in = new FileInputStream(path)
    }
    in
  }

  def toString(any: Any): String = {
    if (any == null)
      ""
    else
      any.toString
  }

  def toInt(any: Any, default: Int = 0): Int = {
    if (any == null)
      default
    else
      any.toString.toInt
  }

  def toDouble(any: Any, default: Double = 0D): Double = {
    if (any == null)
      default
    else
      any.toString.toDouble
  }

  def firstItrAsDouble(iter: Iterable[Double], defaultValue: Double = 0D): Double = {
    if (iter.isEmpty) {
      defaultValue
    } else {
      retainDecimal(iter.head, 2)
    }
  }

  def firstItrAsString(iter: Iterable[String], defaultValue: String = ""): String = {
    if (iter.isEmpty) {
      defaultValue
    } else {
      iter.head
    }
  }

  def firstItrAsInt(iter: Iterable[Int], defaultValue: Int = 0): Int = {
    if (iter.isEmpty) {
      defaultValue
    } else {
      iter.head
    }
  }

  def firstItr[T](iter: Iterable[T], defaultValue: T = None): T = {
    if (iter.isEmpty) {
      defaultValue
    } else {
      iter.head
    }
  }


  /**
   * 保留小数位数
   */
  def retainDecimal(number: Double, bits: Int = 2): Double = {
    BigDecimal(number).setScale(bits, BigDecimal.RoundingMode.HALF_UP).doubleValue()
  }

  def retainDecimalDown(number: Double, bits: Int = 2): Double = {
    BigDecimal(number).setScale(bits, BigDecimal.RoundingMode.DOWN).doubleValue()
  }

  def getStrDate(format: String): String = {
    return new SimpleDateFormat(format).format(new Date)
  }

  def getStrDate: String = {
    return getStrDate("yyyy-MM-dd HH:mm:ss")
  }

  def strToDate(str: String, format: String = "yyyy-MM-dd HH:mm:ss"): Date = {
    val sdf: SimpleDateFormat = new SimpleDateFormat(format)
    var date: Date = null
    try {
      date = sdf.parse(str)
    } catch {
      case e: Exception => {
        logger.info("解析日期出错:{}", e)
        date = new Date
      }
    }
    return date
  }

  def daysFromNow(date: Date): Int = {
    ((new Date().getTime - date.getTime) / (1000 * 3600 * 24)).asInstanceOf[Int]
  }

  def getNdaysAgo(n: Int, format: String = "yyyy-MM-dd HH:mm:ss"): String = {
    var dNow = new Date() //当前时间
    var dBefore = new Date()
    val calendar = Calendar.getInstance() //得到日历
    calendar.setTime(dNow)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    dNow = calendar.getTime()
    calendar.add(Calendar.DAY_OF_MONTH, -n)
    dBefore = calendar.getTime()
    val sdf = new SimpleDateFormat(format) //设置时间格式
    sdf.format(dBefore)
  }

  def addByDay(strDate: String, n: Int, format: String = "yyyy-MM-dd"): String = {
    val calendarDate = Calendar.getInstance()
    val df = new SimpleDateFormat(format)
    val date = df.parse(strDate)
    calendarDate.setTime(date)
    calendarDate.add(Calendar.DAY_OF_MONTH, n)
    df.format(calendarDate.getTime)
  }

  /**
   * 时间格式只能是：
   * 2017-05-10,2017-06-11
   * 或
   * 200,0
   * 这样的
   * @param dateRange
   * @return
   */
  def readDateRange(dateRange: String): (String, String) = {
    val start = dateRange.split(",")(0)
    val end = dateRange.split(",")(1)
    //2017-05-10,2017-06-11
    if (dateRange.contains("-")) {
      (start, end)
    }
    //200,0
    else {
      (getNdaysAgo(start.toInt), getNdaysAgo(end.toInt))
    }
  }

  def isEmpty(str:String) = {
    str == null || str == ""
  }

  def isNotEmpty(str:String) = {
    !isEmpty(str)
  }

  def hashMD5(content: String): String = {
    val md5 = MessageDigest.getInstance("MD5")
    val encoded = md5.digest((content).getBytes)
    encoded.map("%02x".format(_)).mkString
  }

  def formatNumber(num :Int)={
    f"$num%5d".replaceAll(" ", "0")
  }

}
