package org.knirxot.flume.response

import org.elasticsearch.common.bytes.{BytesArray, BytesReference}
import org.elasticsearch.rest.{RestRequest, RestResponse, RestStatus}
import org.knirxot.flume.resource.{ResourceLoader, ResourceMapping}


/**
 * Created by xw on 2019/9/5.
 */
abstract class ResourceResponse(r: RestRequest, p: String) extends RestResponse {
  var responseStatus: RestStatus = _

  private var tmpContent: BytesReference = _

  private var cacheFile = Map[String, (RestStatus, BytesArray)]()

  type SBI = ((String, Boolean), Array[Byte])

  addHeader("Access-Control-Allow-Origin", "*")

  override def content(): BytesReference = {
    if (null != tmpContent) {
      val tc = tmpContent.copyBytesArray()
      tmpContent = null
      tc
    } else {
      if (cacheFile.contains(r.path())) {
        val a = cacheFile(r.path())
        responseStatus = a._1
        a._2
      } else if (cacheFile.contains(p)) {
        val a = cacheFile(p)
        responseStatus = a._1
        a._2
      } else {
        bytesArray(getResourceAsStream)
      }
    }
  }

  override def status(): RestStatus = {
    if (null == responseStatus) {
      tmpContent = content()
    }
    responseStatus
  }

  private def getResourceAsStream: SBI = {
    val path = ResourceMapping.getSource(r.path())
    var path2 = ("", false)
    var is: Array[Byte] = null
    if (path.isDefined) {
      path2 = path.get
      is = ResourceLoader.loadResource(path.get._1, path.get._1, p)
    } else {
      path2 = (p, true)
      is = ResourceLoader.loadResource(p)
    }
    (path2, is)
  }

  private def bytesArray(sbi: SBI): BytesArray = {
    if (null == sbi._2) {
      responseStatus = RestStatus.NOT_FOUND
      cache(sbi, BytesArray.EMPTY)
    } else {
      responseStatus = RestStatus.OK
      cache(sbi, new BytesArray(sbi._2))
    }
  }

  private def cache(key: SBI, v: BytesArray): BytesArray = {
    if (key._1._2) {
      cacheFile = cacheFile ++ Map(key._1._1 -> (responseStatus, v))
    }
    v
  }

}
