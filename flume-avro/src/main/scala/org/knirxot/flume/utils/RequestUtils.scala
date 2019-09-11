package org.knirxot.flume.utils

import java.util

/**
 * Created by xw on 2019/9/6.
 */
object RequestUtils {

  implicit def toRequestTools(params: util.Map[String, String]): RequestTools = RequestTools(params)

  case class RequestTools(params: util.Map[String, String]) {
    def getString(key: String, default: String): String = {
      if (null == params || !params.containsKey(key)) default else params.get(key)
    }

    def getInt(key: String, default: Int): Int = {
      getString(key, default.toString).toInt
    }
  }


}
