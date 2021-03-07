package com.haozhuo.datag.com.service.bean

import scala.beans.BeanProperty


class Desc extends Serializable {
  @BeanProperty var result:Result = null
  @BeanProperty var flag:Int = 0

  override def toString = s"Desc()"
}
