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

import java.io.{File, Reader}
import scala.io.Source
import spray.json._
import scala.Predef._
import scala.Some
import scala.collection.{SeqView, Iterable, AbstractSeq}


case class Category(categoryCode: String, categoryName: String)

case class RawInfo(UID: String,
                   category: String,
                   title: String,
                   comment: String,
                   descriptionFilterHtml: String,
                   startDate: String,
                   endDate: String,
                   imageUrl: String,
                   masterUnit: List[String],
                   otherUnit: List[String],
                   showUnit: String,
                   subUnit: List[String],
                   supportUnit: List[String],
                   sourceWebName: String,
                   sourceWebPromote: String,
                   webSales: String,
                   discountInfo: String,
                   showInfo: Seq[ShowInfo])

case class Info(UID: String,
                category: String,
                title: String,
                comment: Option[String],
                description: Option[String],
                startDate: String,
                endDate: Option[String],
                imageUrl: Option[String],
                iconImage: Option[String],
                masterUnit: String,
                otherUnit: Option[String],
                showUnit: Option[String],
                subUnit: Option[String],
                supportUnit: Option[String],
                sourceWebName: String,
                sourceWebPromote: Option[String],
                webSales: Option[String],
                discountInfo: Option[String],
                locations: String,
                showinfo: Seq[ShowInfo]
                 )


case class ShowInfo(
                     lat: Option[Double],
                     lon: Option[Double],
                     location: String,
                     locationName: String,
                     onSales: Boolean,
                     price: Option[String],
                     time: String
                     )

case class RawShowInfo(
                        lat: Option[Double],
                        lon: Option[Double],
                        location: String,
                        locationName: String,
                        onSales: Boolean,
                        price: Option[String],
                        time: String
                        )


object InfoJsonProtocol extends DefaultJsonProtocol {

  import Purify._

  implicit val f0 = jsonFormat2(Category)

  implicit object ShowJsonFormat extends RootJsonFormat[ShowInfo] {
    def write(p: ShowInfo) = JsObject(
      "latitude" -> JsString(p.lat.getOrElse("").toString),
      "longitude" -> JsString(p.lon.getOrElse("").toString),
      "location" -> JsString(p.location),
      "locationName" -> JsString(p.locationName),
      "onSales" -> JsString(if (p.onSales) "Y" else "N"),
      "price" -> JsString(p.price.getOrElse("")),
      "time" -> JsString(p.time)
    )

    def read(value: JsValue) = {
      value.asJsObject.getFields("latitude", "longitude", "location", "locationName", "onSales", "price", "time") match {
        case Seq(JsString(lat), JsString(lon), JsString(loc), JsString(lname), JsString(sale), JsString(price), JsString(time)) =>
          new ShowInfo(opt(lat, toDouble), opt(lon, toDouble), loc, lname, sale == "Y", opt(price), time)
        case _ => throw new DeserializationException("ShowInfo expected")
      }
    }
  }


  implicit val f1 = jsonFormat18(RawInfo)
  implicit val f2 = jsonFormat20(Info)
}


object OpenData {

  import InfoJsonProtocol._
  import Purify._

  val host = "http://cloud.culture.tw"
  val jsonpath = s"$host/frontsite/inquiry/queryAction.do"
  val categories = s"$host/frontsite/trans/SearchShowAction.do?method=doFindAllTypeJ"

  val efile = new File("extra.json")

  def openData(code: String) = s"$host/frontsite/trans/SearchShowAction.do?method=doFindTypeJ&category=$code"


  def fetch(url: String): Reader = {
    println(s"fetch=> $url")
    Source.fromURL(url).bufferedReader()
  }

  def fetchHtml(url: String): String =
    Source.fromURL(url).mkString


  def getCategories: Seq[Category] =
    fetchHtml(categories).asJson.convertTo[Seq[Category]]

  def getInfo(code: String, iconMap: Map[String, Data]): Seq[Info] =
    fetchHtml(openData(code)).asJson.convertTo[Seq[RawInfo]] map (r => convert(r, iconMap))
}


private object Purify {

  implicit def convert(raw: RawInfo, extraMap: Map[String, Data]): Info = {
    //icon image is large, but in our json icon is for small
    val image = opt(raw.imageUrl) match {
      case d@Some(_) => d
      case None => extraMap.get(raw.UID) match {
        case Some(data) => data.iconImageUrl match {
          case img@Some(_) => img
          case None => opt(data.imageUrl)
        }
        case None => opt(raw.imageUrl)
      }
    }





    var locations = extraMap.get(raw.UID) match {
      case Some(x) => x.showInfoList.map(e =>
        cityMap.getOrElse(e.cityId, location2Area(e.location))).
        distinct.sorted.foldRight("")(_ + _)
      case None => extractLocation(raw.showInfo)
    }

    //Just a workaround, we don't have cityId and can't parse area from  address
    //So just give a trail to master unit
    if (locations.isEmpty && !raw.masterUnit.isEmpty) {
      locations = location2Area(raw.masterUnit.mkString)
    }

    extraMap.get(raw.UID)


    new Info(
      UID = raw.UID,
      category = raw.category,
      title = raw.title,
      comment = opt(raw.comment),
      description = opt(raw.descriptionFilterHtml),
      startDate = raw.startDate,
      endDate = opt(raw.endDate),
      imageUrl = image,
      iconImage = extraMap.get(raw.UID) match {
        case Some(data) => opt(data.imageUrl).orElse(data.iconImageUrl)
        case None => image
      },
      masterUnit = raw.masterUnit.mkString("; "),
      otherUnit = opt(purifyUnit(raw.otherUnit.mkString("; "))),
      showUnit = opt(purifyUnit(raw.showUnit)),
      subUnit = opt(purifyUnit(raw.subUnit.mkString("; "))),
      supportUnit = opt(purifyUnit(raw.supportUnit.mkString("; "))),
      sourceWebName = raw.sourceWebName,
      sourceWebPromote = opt(raw.sourceWebPromote),
      webSales = opt(raw.webSales),
      discountInfo = opt(raw.discountInfo),
      locations =  locations.sortBy(cityOrderMap),
      showinfo = reduceShowInfo(raw.showInfo)
    )
  }

  def reduceShowInfo(infos: Seq[ShowInfo]): Seq[ShowInfo] = {
    //TODO: performance....
    infos.groupBy(s => (s.location, s.locationName, s.price)).map {
      case (_, v) => v.reduce((a, b) => a.copy(time = a.time + ";" + b.time))
    }.toSeq
  }

  def purifyUnit(s: String): String = {
    s.replace("/中華民國", "")
  }

  def mergeDate(begin: String, end: Option[String]): String =
    end match {
      case Some(x) if x != begin => s"$begin ~ $x"
      case _ => begin
    }


  val cityMap = Map(
    1 -> "北", // "臺北市",
    2 -> "基", // "基隆市",
    3 -> "新", // "新北市",
    4 -> "宜", // "宜蘭縣",
    5 -> "桃", // "桃園縣",
    6 -> "竹", // "新竹市",
    7 -> "竹", // "新竹縣",
    8 -> "苗", // "苗栗縣",
    10 -> "中", //  "臺中市",
    11 -> "彰", //  "彰化縣",
    12 -> "投", //  "南投縣",
    13 -> "雲", //  "雲林縣",
    14 -> "嘉", //  "嘉義縣",
    15 -> "嘉", //  "嘉義市",
    16 -> "南", //  "臺南市",
    18 -> "高", //  "高雄市",
    20 -> "屏", //  "屏東縣",
    21 -> "澎", //  "澎湖縣",
    22 -> "花", //  "花蓮縣",
    23 -> "東", //  "台東縣",
    24 -> "金", //  "金門縣",
    25 -> "馬") //  "連江縣")


  val cityOrderMap = Map(
    '新' -> 1,
    '北' -> 2,
    '基' -> 3,
    '桃' -> 4,
    '竹' -> 5,
    '苗' -> 6,
    '中' -> 7,
    '彰' -> 8,
    '投' -> 9,
    '雲' -> 10,
    '嘉' -> 11,
    '南' -> 12,
    '高' -> 13,
    '屏' -> 14,
    '宜' -> 15,
    '花' -> 16,
    '東' -> 17,
    '澎' -> 18,
    '金' -> 19,
    '馬' -> 20)


  val pattern = """((?:台|臺)(?:北|中|南|東)|(?!新|台|臺|竹)北市|新北|新竹|彰化|屏東|澎湖|嘉義|雲林|南投|苗栗|桃園|高雄|基隆|宜蘭|花蓮)""".r
  val shortmap = Map[String, String](
    "台南" -> "南", "臺南" -> "南",
    "台北" -> "北", "臺北" -> "北", "北市" -> "北",
    "台中" -> "中", "臺中" -> "中",
    "台東" -> "東", "臺東" -> "東",
    "新北" -> "新",
    "新竹" -> "竹",
    "彰化" -> "彰",
    "屏東" -> "屏",
    "澎湖" -> "澎",
    "嘉義" -> "嘉",
    "雲林" -> "雲",
    "南投" -> "投",
    "苗栗" -> "苗",
    "桃園" -> "桃",
    "高雄" -> "高",
    "基隆" -> "基",
    "宜蘭" -> "宜",
    "花蓮" -> "花"
  )


  def extractLocation(ss: Seq[ShowInfo]): String =
    ss.map(_.location).map(location2Area).distinct.sorted.foldRight("")(_ + _)

  def location2Area(loc: String): String =
    pattern.findFirstMatchIn(loc) match {
      case Some(m) => shortmap(m.group(0))
      case None => ""
    }

  def location2Area(loc: Option[String]): String = loc match {
    case Some(x) => pattern.findFirstMatchIn(x) match {
      case Some(m) => shortmap(m.group(0))
      case None => ""
    }
    case None => ""

  }


  def opt(s: String): Option[String] =
    if (s == null || s == "") None else Some(s)

  def opt[T](s: Option[T]): Option[T] = s

  def toDouble(s: String): Double = s.toDouble


  def opt[T](s: String, f: String => T): Option[T] =
    if (s == null || s == "") None else Some(f(s))


}

