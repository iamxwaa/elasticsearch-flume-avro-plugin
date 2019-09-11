package org.knirxot.flume.agent.config

import java.util

import org.apache.commons.lang.StringUtils
import org.knirxot.flume.utils.RequestUtils._

import scala.beans.BeanProperty

/**
 * Created by xw on 2019/9/5.
 */
case class AvroConfig(params: util.Map[String, String]) {
  @BeanProperty
  var host: String = params.getString("host", "")
  @BeanProperty
  var port: Int = params.getInt("port", -1)
  @BeanProperty
  var dataType: String = params.getString("dataType", "flume-avro")
  @BeanProperty
  var batch: Int = params.getInt("batch", 2000)
  @BeanProperty
  var channelType: String = params.getString("channelType", "file")

  def info: String = {
    s"$host:$port#$dataType"
  }
}

case class EsConfig(params: util.Map[String, String]) {
  @BeanProperty
  var index: String = params.getString("index", "")
  @BeanProperty
  var timeValue: Int = params.getInt("timeValue", 600)
  @BeanProperty
  var size: Int = params.getInt("size", 5000)
}

case class Config(params: util.Map[String, String]) {

  @BeanProperty
  var avroConfig: AvroConfig = AvroConfig(params)
  @BeanProperty
  var esConfig: EsConfig = EsConfig(params)

  def check: Boolean = {
    if (StringUtils.isEmpty(esConfig.index)) {
      false
    } else if (StringUtils.isEmpty(avroConfig.host)) {
      false
    } else if (-1 == avroConfig.port) {
      false
    } else {
      true
    }
  }

  override def toString: String = {
    //GrantRun(() => {      new Gson().toJson(this)    })
    s"""{"avroConfig":{"host":"${avroConfig.host}","port":${avroConfig.port},"dataType":"${avroConfig.dataType}"},"esConfig":{"index":"${esConfig.index}","timeValue":${esConfig.timeValue},"size":${esConfig.size}}}"""
  }
}
