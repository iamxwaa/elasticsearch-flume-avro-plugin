package org.knirxot.flume.response

import org.elasticsearch.rest.{RestRequest, RestStatus}

/**
 * Created by xw on 2019/9/4.
 */
case class PageResponse(r: RestRequest, dir: String) extends ResourceResponse(r, dir) {

  def this(r: RestRequest) = {
    this(r, "")
  }

  override def contentType(): String = "text/html; charset=utf-8"

}
