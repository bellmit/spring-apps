package com.haozhuo.datag.service.Insurance

import scala.util.matching.Regex


/*
*提取数据不能够包含其他的数据类型，比如肝脏之类的。
*否则将会导致数据混乱。
*
*
* */
import scala.collection.mutable._
object GetDataS_test {
  val str1:String=" 双肾形态、大小、位置未见异常，双侧肾脏见类圆形低密度灶，大者位于右侧，直径约9mm，边界清楚，并见点状钙化，CT值约10Hu，余肾实质内未见异常密度影，左肾盂见边界清晰水样密度"
  val str2:String=" 双肾位置未见异常。左肾盏可见小点状高密度结石。右肾和双输尿管未见异常。腹膜后未见肿大的淋巴结。左侧肾上腺内见类圆形混杂密度影，直径约24mm，其内见多发点状高密度灶。"

  val str3:String=" 肝脏：肝脏大小、形态正常，肝包膜完整，血管走向清，肝内光点分布均匀。门静脉内径10mm。胆囊：大小、形态正常，胆囊壁光滑，胆汁透声好，其内未见明显强光团。胆总管内径4mm。胰腺：大小、形态正常，回声均匀。脾脏：大小、形态正常，回声均匀。双肾：大小、形态正常，双肾包膜完整，集合系统未见分离暗区及光团。"
  val str4:String="左肾皮质可见椭圆形不规则 低密度占位，大小约 1.7 x15mm密度 均匀/不均匀 。病变边界欠清楚/ 。肾周脂肪间隙 清晰  。所及肝脏、脾脏、胆囊、胰腺未见明显异常。腹膜后 未见肿大淋巴结。"

  val str5:String="双肾位置未见异常。左肾盂及下盏可见多发高密度结石，最大的约 13mm*25 mm大小，左侧肾盂肾盏见水样密度影，左侧上端输尿管扩张。右肾及输尿管未见异常。脾脏、"
  def main(args: Array[String]): Unit = {

    def shenzang(yourString:String)={

    val regex = new Regex(
      """[0-9]+mm×[0-9]+mm×[0-9]+mm|[0-9]+cm×[0-9]+cm×[0-9]+cm|
        |[0-9]+mmx[0-9]+mmx[0-9]+mm|[0-9]+cmx[0-9]+cmx[0-9]+cm|
        |[0-9]+mmX[0-9]+mmX[0-9]+mm|[0-9]+cmX[0-9]+cmX[0-9]+cm|
        |[0-9]+mmx[0-9]+mm|[0-9]+mmX[0-9]+mm|
        |[0-9]+mm\*[0-9]+mm|
        |[0-9]+cmx[0-9]+cm|[0-9]+cmX[0-9]+cm|
        |[0-9]+cm\*[0-9]+cm|
        |[0-9]+x[0-9]+mm|[0-9]+X[0-9]+mm|
        |[0-9]+x[0-9]+cm|[0-9]+X[0-9]+cm|
        |[0-9]+\*[0-9]+mm|[0-9]+\*[0-9]+cm|
        |[0-9]+×[0-9]+mm|[0-9]+×[0-9]+cm|[0-9]+mm×[0-9]mm|[0-9]+cm×[0-9]cm|
        |[0-9]+\.+[0-9]+x[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+x[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+\*[0-9]+\.[0-9]+cm|[0-9]+\.+[0-9]+×[0-9]+\.[0-9]+cm|
        |[0-9]+\.+[0-9]+cm|[0-9]+\.[0-9]+mm|
        |[0-9]+cm|[0-9]+mm|
        |肾.{2,40}钙化|肾.{2,40}分隔|
        |[0-9]+.{0,10}cm|[0-9]+.{0,10}mm|
        |多囊肾|肾.{0,15}结石.{0,15}mm|
        """)



      val strings = new ArrayBuffer[String]
    //分开匹配-判断是什么，使用不同的正则表达式。
    val reg =regex.findAllIn(yourString)
    while (reg.hasNext){
      strings.append(reg.next())
    }
    strings
    }//方法结束

    //对数据进行提取并转换单位
    def panduanRXUntil(yourArray:ArrayBuffer[String])= {

      val regexGetNum = new Regex(
        """
          |[0-9]+\.[0-9]+|
          |[0-9]+|
        """)




      val result_arr: ArrayBuffer[String] = yourArray.map(x => {

        //初始化
        var row: String = " "





        //如果x包含mm
        if (x.contains("mm")) {

          var mmNum =regexGetNum.findAllIn(x)

          //row_ints.append(5)
          //mm如果是整数就不会进入循环
          val row_ints = new ArrayBuffer[Double]

          //获取每一行的数据
          while (mmNum.hasNext){
            var mm=" "
            //每一行的数据装入一个数组
            mm = mmNum.next()


            row_ints.append(mm.toDouble)


          }
          //Int形式

          val row_ints1: Double = row_ints.max



          val result_Max_Num: String = row_ints1.toString
          //返回原始单位是毫秒的结果
          if(x.contains("结石")){
            row="肾结石:"+result_Max_Num
          }else if(x.contains("钙化")||x.contains("分隔")){
            row="肾脏钙化或者分隔"
          }
          else {
            row = result_Max_Num
          }


        } else if (x.contains("cm")) {

          //过滤出数值,单位mm
          val mmNum = regexGetNum.findAllIn(x)
          var mm=" "

          val row_ints = new ArrayBuffer[Double]

          //获取每一行的数据
          while (mmNum.hasNext){
            //每一行的数据装入一个数组
            mm = mmNum.next()

            row_ints.append(mm.toDouble*10)

          }
          //Int形式
          val max: Double = row_ints.max
          val result_Max_Num: String = max.toString
          //返回原始单位是毫秒的结果
          if(x.contains("结石")){
            row="肾结石:"+result_Max_Num
          }else if(x.contains("钙化")||x.contains("分隔")){
            row="肾脏钙化或者分隔"
          }
          else {
            row = result_Max_Num
          }






        } else {
          row = x
        }


        //返回值
        row
      })
      //返回排序好的数值单位mm

      result_arr


    }

    //结果方法

    def result(yourString2:ArrayBuffer[String])={

      val regexGetNum = new Regex(
        """
          |[0-9]+\.[0-9]+|
          |[0-9]+|
        """)
      //创建结果集
      val result_arr=new ArrayBuffer[String]
      var status_leave="good"
      //第一遍判断疾病等级
      yourString2.foreach(x=>{
        if(x.contains("钙化")||x.contains("分割")){
          result_arr.append("钙化，或者分割")
        }
      })


      var status:String = "good"
      //第二遍正则表达式数值过滤
      yourString2.foreach(x=>{
          //肾结石//钙化//分割
        if(x.contains("肾结石")) {
          val regexGetNum = new Regex(
            """
              |[0-9]+\.[0-9]+|
              |[0-9]+|
            """)
          val reg = regexGetNum.findAllIn(x)
          while (reg.hasNext) {
            if (reg.next().toDouble >= 30) {
              status = "overGG"
              result_arr.append(status_leave)
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

    //方法结束
    //封装3个方法.
    def getStatus(yourString:String)= {
      val arr: ArrayBuffer[String] = shenzang(yourString)
//                arr.foreach(x=>println(x))
      val strings: ArrayBuffer[String] = panduanRXUntil(arr)

//                strings.foreach(x=>println(x))
      val result_status: String = result(strings)
      result_status

    }



  }
}
