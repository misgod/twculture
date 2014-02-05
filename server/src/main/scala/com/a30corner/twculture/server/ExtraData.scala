/*
* Copyright (C) 2014 Szu-Hsien Lee (misgod.tw@gmail.com)
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

package com.a30corner.twculture.server

import spray.json._
import java.io.{PrintWriter, File}
import scala.io.Source


case class Extra(data: List[Data], paging: Paging)

case class Data(ids: String,
                iconImageUrl: Option[String],
                imageUrl: String,
                showInfoList:List[ShowInfoList]
                 )

case class ShowInfoList(cityId: Int,location:Option[String])


case class Paging(currentPage: Int, pageCount: Int, recordCount: Int)


object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val f0 = jsonFormat2(ShowInfoList)

  implicit val f1 = jsonFormat4(Data)

  implicit object PagingJsonFormat extends RootJsonFormat[Paging] {
    def write(p: Paging) = JsObject(
      "currentPage" -> JsString(p.currentPage.toString),
      "pageCount" -> JsString(p.pageCount.toString),
      "recordCount" -> JsString(p.recordCount.toString)
    )

    def read(value: JsValue) = {
      value.asJsObject.getFields("currentPage", "pageCount", "recordCount") match {
        case Seq(JsString(cur), JsString(pcount), JsString(rcount)) =>
          new Paging(cur.toInt, pcount.toInt, rcount.toInt)
        case _ => throw new DeserializationException("Paging expected")
      }
    }
  }

  implicit val f3 = jsonFormat2(Extra) //extra must after data & paging...
}


object ExtraData {

  import spray.json._
  import MyJsonProtocol._
  import OpenData._

  def log(s: String): String = {
    println(s)
    s
  }

  def url(page: Int, count: Int): String =
    log(s"$jsonpath?method=doSearch2&siteId=101&iscancel=true&sortBy=dataImportDate&currentPage=$page&recordCount=$count")


  private def extract(page: Int, count: Int): Extra =
    fetchHtml(url(page, count)).asJson.convertTo[Extra]


  def extradata(page: Int, pcount: Int = 100, rcount: Int = 0): Stream[List[Data]] = {
    require(page > 0)
    println(s"extradata=> $page / $pcount")
    if (page > pcount) {
      Stream.empty
    } else {
      val a = extract(page, rcount)
      a.data #:: extradata(a.paging.currentPage + 1, a.paging.pageCount, a.paging.recordCount)
    }
  }

  def write(f:File): File = {
    val writer = new PrintWriter(f)

    val c = for {
      s <- ExtraData.extradata(1)
      a <- s
    } yield a.toJson

    writer.write("[")
    writer.write(c.mkString(","))
    writer.write("]")
    writer.close()
    f
  }


  def read(f: File): List[Data] = {
    val a = Source.fromFile(f).mkString.asJson.convertTo[List[Data]]
    a.filter(b => b.iconImageUrl.getOrElse("") != "" || b.imageUrl != "")
  }

  def toMap(d: List[Data]): Map[String, Data] =
    d.groupBy(_.ids).map {
      case (k, v) => k -> v.head
    }


}
