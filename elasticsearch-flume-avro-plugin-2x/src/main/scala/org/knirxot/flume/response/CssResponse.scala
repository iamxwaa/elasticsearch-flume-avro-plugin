package org.knirxot.flume.response

import org.elasticsearch.rest.RestRequest

/**
 * Created by xw on 2019/9/5.
 */
case class CssResponse(r: RestRequest, dir: String) extends ResourceResponse(r, dir) {

  override def contentType(): String = "text/css"

}
