/*
* Copyright (C) 2014 SzuHsien Lee (misgod.tw@gmail.com)
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.a30corner.twculture.util

import java.io.{IOException, BufferedInputStream, File}
import scala.concurrent.{ExecutionContext, Promise, Future}
import com.a30corner.twculture.util.LogUtil._
import java.net.{HttpURLConnection, URL}
import java.lang.RuntimeException
import ExecutionContext.Implicits.global

object DownloadHelper {
  def httpGet(url: String, file: File): Future[File] = {
    D("httpGet", url)
    val p = Promise[File]()
    Future {
      val conn = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
      try {
        if (conn.getResponseCode == 200 || conn.getResponseCode == 201) {
          p.success(FileUtil.write(file, conn.getInputStream))
        } else {
          p.failure(new RuntimeException(conn.getResponseMessage))
        }
      } catch {
        case e: Exception =>
          W(TAG, e, "get data fail...")
          p.failure(e)
      } finally {
        conn.disconnect()
      }
    }

    p.future
  }


  def getHashContent(url: String): Future[String] = {
    D("getHttpContent", url)
    val p = Promise[String]()
    Future {
      val conn = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
      try {
        if (conn.getResponseCode == 200 || conn.getResponseCode == 201) {
          val bin = new BufferedInputStream(conn.getInputStream,32)
          val buf = new Array[Byte](32)
//          Stream.continually(bin.read(buf)).takeWhile(_ != -1)
          bin.read(buf)
          bin.close()
          p.success(new String(buf))
        } else {
          p.failure(new RuntimeException(conn.getResponseMessage))
        }
      } catch {
        case e: Exception =>
          W(TAG, e, "get data fail...")
          p.failure(e)
      } finally {
        D("getHttpContent", url)
        conn.disconnect()
      }
    }

    p.future
  }

}
