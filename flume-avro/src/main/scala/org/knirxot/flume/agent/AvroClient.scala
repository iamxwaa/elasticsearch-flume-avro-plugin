package org.knirxot.flume.agent

import java.util

import org.apache.flume.Event
import org.apache.flume.agent.embedded.EmbeddedAgent
import org.knirxot.flume.agent.config.AvroConfig
import org.knirxot.flume.security.GrantRun

/**
 * Created by xw on 2019/9/4.
 */
object AvroClient {

  case class Client(avroConfig: AvroConfig) {
    val id = System.currentTimeMillis()
    val properties = new util.HashMap[String, String]()
    if (avroConfig.channelType.equals("memory")) {
      properties.put("channel.type", "memory")
      properties.put("channel.capacity", "2000000")
      properties.put("channel.transactionCapacity", "2000000")
    } else {
      properties.put("channel.type", "file")
      properties.put("channel.checkpointDir", getCheckPointDir)
      properties.put("channel.dataDirs", getDataDir)
      properties.put("channel.capacity", "20000000")
    }
    properties.put("sinks", "sink1")
    properties.put("sink1.type", "avro")
    properties.put("sink1.hostname", avroConfig.host)
    properties.put("sink1.port", avroConfig.port.toString)
    properties.put("processor.type", "failover")
    properties.put("source.interceptors", "i1")
    properties.put("source.interceptors.i1.type", "static")
    properties.put("source.interceptors.i1.key", "_TYPE")
    properties.put("source.interceptors.i1.value", avroConfig.dataType)

    private def getCheckPointDir = {
      "/tmp/flume-ck-avro-" + id
    }

    private def getDataDir = {
      var list: List[String] = List()
      for (i <- 1 to 3) {
        list = list ++ List("/tmp/flume-data-avro" + i + "-" + id)
      }
      list.mkString(",")
    }

    private val agent = new EmbeddedAgent("elasticsearch-flume-avro-client")
    agent.configure(properties)

    def start: Boolean = {
      GrantRun(() => {
        var isStarted = false
        try {
          agent.start
          isStarted = true
        } catch {
          case e: Exception => isStarted = false
        }
        isStarted
      }: Boolean)
    }

    def sendBath(events: util.List[Event]): Unit = {
      if (null != events) {
        GrantRun(() => agent.putAll(events))
      }
    }

    def send(event: Event): Unit = {
      if (null != event) {
        GrantRun(() => agent.put(event))
      }
    }

    def stop: Unit = {
      GrantRun(agent.stop)
    }
  }

  def getClient(avroConfig: AvroConfig): Client = Client(avroConfig)
}