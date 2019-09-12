package org.knirxot.flume.client

import java.text.SimpleDateFormat
import java.util
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, Executors}

import org.apache.commons.lang.StringUtils
import org.apache.flume.Event
import org.apache.flume.event.JSONEvent
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest
import org.elasticsearch.action.search.{ClearScrollRequest, SearchRequest, SearchResponse, SearchScrollRequest}
import org.elasticsearch.client.node.NodeClient
import org.elasticsearch.common.logging.Loggers
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.slice.SliceBuilder
import org.knirxot.flume.agent.AvroClient
import org.knirxot.flume.agent.config.{Config, DataInfo}

/**
 * Created by xw on 2019/9/4.
 */
class SearchClient(client: NodeClient, settings: Settings, config: Config) extends Runnable {
  private val logger = Loggers.getLogger(this.getClass, settings)

  private val avroClient = AvroClient.getClient(config.avroConfig)

  private val sendTotal = new AtomicInteger(0)

  private val startTime = System.currentTimeMillis()

  private val dataInfo: DataInfo = DataInfo()

  private val exec = Executors.newCachedThreadPool()

  private var stopFlag = false

  def stop() = {
    stopFlag = true
  }

  override def run(): Unit = {
    try {
      logger.info(s"Start flume avro client(${config.avroConfig.info})")
      dataInfo.isFinish = false
      val isStarted = avroClient.start
      //启动状态可能有延迟
      Thread.sleep(1000)
      if (!isStarted) {
        throw new Exception("Start flume avro client with error !!!")
      }
      val shardsCount = getShardsCount
      val countDown = new CountDownLatch(shardsCount)

      for (i <- 0 until shardsCount) {
        exec.execute(new Runnable {
          override def run(): Unit = {
            try {
              export(i, shardsCount)
            } finally {
              countDown.countDown()
            }
          }
        })
      }
      countDown.await()
      dataInfo.endTime.value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
      logger.info(s"Stop flume avro client(${config.avroConfig.info}) after ${dataInfo.waitTime}s")
      Thread.sleep(dataInfo.waitTime * 1000)
    } catch {
      case e: Exception => logger.error(s"Client(${config.avroConfig.info}) has error(${e.getMessage}). Stop !!!")
    } finally {
      dataInfo.isFinish = true
      logger.info(s"Stop flume avro client(${config.avroConfig.info})")
      avroClient.stop
    }
  }

  private def export(id: Int, max: Int) = {
    dataInfo.index.value = config.esConfig.index
    dataInfo.startTime.value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())

    val searchSourceBuilder = new SearchSourceBuilder()
    searchSourceBuilder.size(config.esConfig.size)
    searchSourceBuilder.slice(new SliceBuilder(id, max))
    val request = new SearchRequest()
    request.indices(config.esConfig.index)
    request.scroll(TimeValue.timeValueSeconds(config.esConfig.timeValue))
    request.source(searchSourceBuilder)
    var response = client.search(request).actionGet()
    var run = checkResponse(response)
    if (run) {
      dataInfo.total.value = dataInfo.total.value + response.getHits.totalHits
      send(response)
      while (run && !stopFlag) {
        logger.info(s"${config.esConfig.index}[$id][$max] ($sendTotal/${dataInfo.total.value})")
        val scrollRequest: SearchScrollRequest = new SearchScrollRequest(response.getScrollId)
        scrollRequest.scroll(TimeValue.timeValueSeconds(config.esConfig.timeValue))
        response = client.searchScroll(scrollRequest).actionGet()
        run = checkResponse(response)
        if (run) {
          send(response)
        }
      }
    }
    val clearScrollRequest = new ClearScrollRequest()
    clearScrollRequest.addScrollId(response.getScrollId)
    client.clearScroll(clearScrollRequest).actionGet()
  }

  private def getShardsCount: Int = {
    val request = new GetSettingsRequest()
    request.indices(config.esConfig.index)
    val response = client.admin().indices().getSettings(request).actionGet()
    val count = response.getSetting(config.esConfig.index, "index.number_of_shards")
    if (StringUtils.isNumeric(count)) count.toInt else 0
  }

  def getDataInfo: DataInfo = {
    if (dataInfo.isFinish) {
      dataInfo
    } else {
      dataInfo.sendTotal.value = sendTotal.get()
      dataInfo.percent.value =
        if (dataInfo.total.value == 0)
          "100 %"
        else {
          val sendTotal1 = if (0 == dataInfo.sendTotal.value) 1 else dataInfo.sendTotal.value
          val percent1 = sendTotal1.toDouble / dataInfo.total.value.toDouble

          if (dataInfo.total.value != dataInfo.sendTotal.value) {
            val now = System.currentTimeMillis()
            val expect1 = now - startTime
            dataInfo.expectTime.value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(now + (expect1 / percent1).toLong))
            (percent1 * 100).formatted("%.2f").toString + " %"
          } else {
            "100 %"
          }
        }
      dataInfo
    }
  }

  def getConfig: Config = config

  /**
   * 判断是否有数据
   *
   * @param response
   * @return
   */
  private def checkResponse(response: SearchResponse): Boolean = {
    response.getHits != null &&
      response.getHits.totalHits > 0 &&
      response.getHits.getHits != null &&
      response.getHits.getHits.length != 0
  }

  private def send(response: SearchResponse) = {
    val cacheEvents = new util.ArrayList[Event]()
    var count = 0
    response.getHits.getHits.foreach(hit => {
      val event = new JSONEvent
      event.setHeaders(new util.HashMap[String, String](1))
      event.setBody(hit.getSourceAsString.getBytes("UTF-8"))
      cacheEvents.add(event)
      count = count + 1
      if (cacheEvents.size() > config.avroConfig.batch) {
        avroClient.sendBath(cacheEvents)
        cacheEvents.clear()
      }
    })
    sendTotal.addAndGet(count)
    if (!cacheEvents.isEmpty) {
      avroClient.sendBath(cacheEvents)
      cacheEvents.clear()
    }
  }
}