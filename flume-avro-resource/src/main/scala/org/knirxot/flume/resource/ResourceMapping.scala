package org.knirxot.flume.resource

/**
 * Created by xw on 2019/9/4.
 */
object ResourceMapping {
  //key->(路径,是否缓存)
  private val sourceMap = Map(
    "/_flume" -> ("/index.html", true)
    //    "/_flume" -> ("/usr/share/elasticsearch/test/index.html", false),
    //    "/_flume/js/index.js" -> ("/usr/share/elasticsearch/test/index.js", false)
  )

  def getSource(path: String): Option[(String, Boolean)] = {
    sourceMap.get(path)
  }
}
