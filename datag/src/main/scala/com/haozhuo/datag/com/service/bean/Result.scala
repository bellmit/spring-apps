package com.haozhuo.datag.com.service.bean

import scala.beans.BeanProperty

class Result extends Serializable {
   @BeanProperty var describe:String="";
   @BeanProperty var conclusion:String="";


   override def toString = s"Result($describe, $conclusion)"
}
