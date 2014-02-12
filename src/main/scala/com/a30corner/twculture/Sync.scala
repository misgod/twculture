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
import scala.io.Source

object Sync {
  def dropbox(name: String) = s"http://dl.dropboxusercontent.com/u/67374708/twculture/$name"


  def openjson(url: String, out: File): Future[BufferedReader] = {
    DownloadHelper.httpGet(url, out).flatMap(
      f => Future(bufreader(new FileInputStream(f), 4096))
    )
  }

  def openFromDropbox(name: String)(implicit c: Context) = openjson(dropbox(name), c.getFileStreamPath(name))

  def openData(name: String, force: Boolean = false)(implicit c: Context): Future[BufferedReader] =
    samehashWithCached(name).flatMap {
      case true if !force =>
        D(TAG, s"cached: $name")
        Future(bufreader(c.openFileInput(name)))
      case _ => openFromDropbox(name)
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


object ICulture {
  def mainTypeMethod(id:String) = s"method=exportEmapJsonByMainType&mainType=$id"

  def typeMethod(id:String) = s"method=exportEmapJson&typeId=$id"


//  ?method=exportEmapJson&typeId=H

  val placesCategory = Seq(
    Category("10", "展演空間", mainTypeMethod("10")),
    Category("11", "藝文中心", mainTypeMethod("11")),
    Category("13", "創意園區", mainTypeMethod("13")),
    Category("14", "藝術村", mainTypeMethod("14")),
    Category("L", "文創商店", typeMethod("L")),
    Category("M", "獨立書店", typeMethod("M")),
    Category("K", "特色圖書館",  typeMethod("K")),
    Category("I", "文化行政據點",  typeMethod("I")),
    Category("H", "博物館",  typeMethod("H")),
    Category("F", "公共藝術", typeMethod("F")),
    Category("E", "文化景觀",  typeMethod("E")),
    Category("D", "社區",  typeMethod("D")),
    Category("C", "地方文化館",  typeMethod("C")),
    Category("B", "工藝之家",  typeMethod("B")),
    Category("A", "文化資產",  typeMethod("A"))
    //  "遺址" -> "typeId=A&classifyId=2.1" ,
    //    "聚落"->"typeId=A&classifyId=1.3",
    //    "歷史建築"->"typeId=A&classifyId=1.2",
    //    "古蹟"->"typeId=A&classifyId=1.1"
  )

  def url(t: String) = s"http://cloud.culture.tw/frontsite/trans/emapOpenDataAction.do?$t"


  def getPlace(t: String): Future[BufferedReader] = Future {
    Source.fromURL(url(t)).bufferedReader()
  }


}
