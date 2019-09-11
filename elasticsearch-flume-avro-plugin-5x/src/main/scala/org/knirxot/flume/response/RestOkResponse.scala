package org.knirxot.flume.response

import org.elasticsearch.common.bytes.{BytesArray, BytesReference}
import org.elasticsearch.rest.{RestResponse, RestStatus}

/**
 * Created by xw on 2019/9/4.
 */
case class RestOkResponse(message: String) extends RestResponse {

  addHeader("Access-Control-Allow-Origin", "*")

  def this() = {
    this("{\"status\":\"OK\"}")
  }

  override def contentType(): String = "application/json; charset=utf-8"

  override def content(): BytesReference = {
    new BytesArray(message)
  }

  override def status(): RestStatus = RestStatus.OK
}
