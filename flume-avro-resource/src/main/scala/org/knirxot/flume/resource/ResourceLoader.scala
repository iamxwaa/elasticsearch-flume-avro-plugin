package org.knirxot.flume.resource

import java.io.{ByteArrayOutputStream, File, FileInputStream, InputStream}

import org.apache.commons.io.IOUtils
import org.knirxot.flume.security.GrantRun

/**
 * Created by xw on 2019/9/10.
 */
object ResourceLoader {

  def loadResource(localPath: String, jarPath: String, jarPath2: String): Array[Byte] = {
    GrantRun(() => {
      val file = new File(localPath)
      if (file.exists()) {
        readStream(new FileInputStream(file))
      } else {
        val stream = this.getClass.getClassLoader.getResourceAsStream("_site" + jarPath)
        if (null == stream) {
          readStream(this.getClass.getClassLoader.getResourceAsStream("_site" + jarPath2))
        } else {
          readStream(stream)
        }
      }
    })
  }

  def loadResource(sitePath: String): Array[Byte] = {
    readStream(this.getClass.getClassLoader.getResourceAsStream("_site" + sitePath))
  }

  private def readStream(input: InputStream): Array[Byte] = {
    if (null == input) {
      Array()
    } else {
      val output = new ByteArrayOutputStream()
      IOUtils.copy(input, output)
      output.toByteArray
    }
  }
}
