package org.knirxot.flume.agent.config

import com.google.gson.Gson
import org.knirxot.flume.security.GrantRun

import scala.beans.BeanProperty

/**
 * Created by xw on 2019/9/5.
 */
case class DataInfoItem(name: String, var value: String)

case class DataInfoNumberItem(name: String, var value: Long)

case class DataInfo() {

  @BeanProperty
  var isFinish: Boolean = true
  @BeanProperty
  var waitTime: Int = 30
  @BeanProperty
  var index: DataInfoItem = DataInfoItem("索引", "-")
  @BeanProperty
  var startTime: DataInfoItem = DataInfoItem("开始时间", "0000-00-00 00:00:00")
  @BeanProperty
  var expectTime: DataInfoItem = DataInfoItem("预计结束时间", "0000-00-00 00:00:00")
  @BeanProperty
  var total: DataInfoNumberItem = DataInfoNumberItem("数据总量", 0)
  @BeanProperty
  var sendTotal: DataInfoNumberItem = DataInfoNumberItem("已获取数据量", 0)
  @BeanProperty
  var percent: DataInfoItem = DataInfoItem("进度", "0 %")
  @BeanProperty
  var endTime: DataInfoItem = DataInfoItem("结束时间", "0000-00-00 00:00:00")

  override def toString: String = {
    GrantRun(() => new Gson().toJson(this))
  }
}
