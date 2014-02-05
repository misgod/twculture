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


package com.a30corner.twculture

import java.io._
import scala.concurrent.Future
import scala.Predef._

import com.a30corner.twculture.util.{LogUtil, DownloadHelper, FileUtil}
import LogUtil._
import android.content.Context
import scala.concurrent.ExecutionContext.Implicits.global

object Sync {
  def dropbox(name: String) = s"http://dl.dropboxusercontent.com/u/67374708/twculture/$name"


  def openjson(url: String, out: File): Future[BufferedReader] = {
    DownloadHelper.httpGet(url, out).flatMap(
      f => Future(bufreader(new FileInputStream(f), 4096))
    )
  }

  def openFromDropbox(name: String)(implicit c: Context) = openjson(dropbox(name), c.getFileStreamPath(name))

  def openData(name: String, force: Boolean = false)(implicit c: Context): Future[BufferedReader] =
    samehashWithCached(name).flatMap{
      case true if !force =>
        D(TAG,s"cached: $name")
        Future(bufreader(c.openFileInput(name)))
      case _ =>  openFromDropbox(name)
    }.recover {
      case _ => bufreader(c.openFileInput(name))
    }


  def cached(name: String)(implicit c: Context): Boolean = {
    //XXX: this is a trick..we don't want to check category everytime since it does not change frequently
    //Improve this...if any better idea
    val threshold = if (name == "category.json") Long.MaxValue else 10800000 // 3hrs
    val f = c.getFileStreamPath(name)
    f.exists() && Math.abs(System.currentTimeMillis - f.lastModified) < threshold
  }


  def hashRemote(url: String): Future[String] =
    DownloadHelper.getHashContent(url)


  def samehash(name: String)(implicit context: Context): Future[Boolean] = {
    val lf = context.getFileStreamPath(name)
    if (lf.exists)
      hashRemote(dropbox(name + ".md5")).map(_ == FileUtil.md5sum(lf))
    else
      Future {
        false
      }
  }

  def samehashWithCached(name: String)(implicit context: Context): Future[Boolean] =
    if (cached(name)) Future(true) else samehash(name: String)


  def bufreader(in: InputStream, size: Int = 8192) = new BufferedReader(new InputStreamReader(new BufferedInputStream(in, size)))

}
