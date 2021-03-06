package com.haozhuo.datag.com.service.Insurance

object MatchJzx {
  //甲状腺:众安不推送关键词 正则集合
  val ZAjzx = Array(
    (".*甲状.{0,2}腺.{0,10}结节.*"),
    (".*甲状腺.{0,3}腺瘤.*"),
    (".*甲状腺.{0,10}钙化.*"),
    (".*毒性.*甲状腺肿.*"),
    (".*甲状腺.{0,5}囊肿.*"),
    (".*甲状腺.{0,5}炎.*"),
    (".*甲状腺.{0,5}病变.*"),
    (".*甲状腺.{0,10}未显示.*"),
    (".*甲状腺次全切除术后.*"),
    (".*甲状腺.{0,5}小.*"),
    (".*甲状腺.{0,5}亢进.*"),
    (".*甲状.{0,3}腺.{0,5}增生.*"),
    (".*甲状旁腺占位.*"),
    (".*甲状腺.{0,8}切除.*"),
    (".*甲状腺.{0,10}火海.*"),
    (".*颈.{0,5}淋巴.{0,10}大.*"),
    (".*甲状腺.{0,5}未探.*"),
    (".*甲状旁腺.{0,5}肿.*"),
    (".*甲状腺.{0,5}肿.*"),
    (".*甲状腺体积小.*"),
    (".*甲状腺.{0,8}包块.*"),
    (".*颈.{0,5}包块.*"),
    (".*甲状腺.{0,5}叶缺如.*"),
    (".*甲状腺.{0,5}异位.*"),
    (".*甲亢.*"),
    (".*碘放射治疗术后.{0,10}甲状腺体积缩小.{0,10}.*")


  )

  def panduan(label:String):String = {

    var result = "1"
    //定义bool默认没有匹配上
    var bool: Boolean = false
    ZAjzx.foreach(line => {

      bool = label.matches(line)

      //匹配上了则为true
      if (bool) {
        result = "0_" + label
        return result
      }
    })

    result

  }

  def main(args: Array[String])= {
    println(panduan("甲亢"))


  }


}
