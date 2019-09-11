package org.knirxot.flume.security

import java.security.{AccessController, Permission, PrivilegedAction}

/**
 * Created by xw on 2019/9/6.
 */
object GrantRun {
  def apply[T](f: () => T): T = {
    try {
      val perm = this.getClass.getClassLoader.loadClass("org.elasticsearch.SpecialPermission").newInstance()
      val sm = System.getSecurityManager()
      if (sm != null) {
        sm.checkPermission(perm.asInstanceOf[Permission])
      }
      AccessController.doPrivileged(new PrivilegedAction[T] {
        override def run(): T = {
          f()
        }
      })
    } catch {
      case _: ClassNotFoundException => {
        AccessController.doPrivileged(new PrivilegedAction[T] {
          override def run(): T = {
            f()
          }
        })
      }
      case e: Exception => throw e
    }
  }
}
