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



package com.a30corner.twculture


import scala.concurrent._
import com.a30corner.twculture.util.LogUtil._
import java.io._

import android.util.JsonReader
import scala.collection.mutable.{ListBuffer, ArrayBuffer}
import com.a30corner.twculture.util.DownloadHelper
import scala.Some
import android.text.TextUtils


import ExecutionContext.Implicits.global
import android.content.Context


case class Category(code: Int, name: String)

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
                     lat: Double,
                     lon: Double,
                     location: String,
                     locationName: String,
                     onSales: Boolean,
                     price: Option[String],
                     time: String
                     )

object OpenData {

  import JSonConvert._

  val cultureUrl = "http://cloud.culture.tw/"

  def getCategories(implicit ctx: Context): Future[Seq[Category]] = {
    D(TAG, "getCategories")
    for {
      reader <- Sync.openData("category.json")
    } yield json2seq(reader)(json2category)
  }


  def getDetail(c: Category)(implicit ctx: Context): Future[Seq[Info]] = {
    D(TAG, s"getDetail: $c")

    for {
      reader <- Sync.openData(s"${c.code}.json")
    } yield json2seq(reader)(json2info)
  }

}


object JSonConvert {
  def json2seq[T](in: Reader, withclose: Boolean = true)(f: JsonReader => T): Seq[T] = {
    V("json2seq", "begin")
    val reader = new JsonReader(in)
    val buf = ArrayBuffer[T]()
    try {
      reader.beginArray()
      while (reader.hasNext) {
        buf += f(reader)
      }
      reader.endArray()
    } catch {
      case e: Exception => W("json2seq", e, e.getMessage)
    } finally {
      if (withclose) reader.close()
    }
    V("json2seq", "end")
    buf.toSeq
  }


  def json2seq[T](reader: JsonReader)(f: JsonReader => T): Seq[T] = {
    val buf = ArrayBuffer[T]()

    reader.beginArray()
    while (reader.hasNext) {
      buf += f(reader)
    }
    reader.endArray()

    buf.toSeq
  }

  def json2info(reader: JsonReader): Info = {
    V("json2info", "begin")

    var uid = ""
    var category = ""
    var title = ""
    var comment: Option[String] = None
    var description: Option[String] = None
    var discount: Option[String] = None
    var iconImage: Option[String] = None
    var imgurl: Option[String] = None
    var masterUnit: String = ""
    var showUnit: Option[String] = None
    var startDate: String = ""
    var endDate: Option[String] = None
    var otherUnit: Option[String] = None
    var subUnit: Option[String] = None
    var supportUnit: Option[String] = None
    var webSales: Option[String] = None
    var sourceWebName: String = ""
    var sourceWebPromote: Option[String] = None
    var slocation = ""
    var showInfo: Seq[ShowInfo] = Nil

    reader.beginObject()
    while (reader.hasNext) {
      val name = reader.nextName()
      name match {
        case "UID" => uid = reader.nextString()
        case "category" => category = reader.nextString()
        case "title" => title = reader.nextString()
        case "comment" => comment = opt(reader.nextString())
        case "description" => description = opt(reader.nextString().replace("\r", "\n"))
        case "discountInfo" => discount = opt(reader.nextString)
        case "endDate" => endDate = opt(reader.nextString)
        case "iconImage" => iconImage = opt(reader.nextString)
        case "imageUrl" => imgurl = opt(reader.nextString)
        case "masterUnit" => masterUnit = reader.nextString
        case "otherUnit" => otherUnit = opt(reader.nextString)
        case "showUnit" => showUnit = opt(reader.nextString())
        case "sourceWebName" => sourceWebName = reader.nextString()
        case "sourceWebPromote" => sourceWebPromote = opt(reader.nextString)
        case "startDate" => startDate = reader.nextString()
        case "subUnit" => subUnit = opt(reader.nextString)
        case "supportUnit" => supportUnit = opt(reader.nextString)
        case "webSales" => webSales = opt(reader.nextString())
        case "locations" => slocation = reader.nextString()
        case "showinfo" => showInfo = json2seq(reader)(json2ShowInfo)
        case _ => reader.skipValue() //D(TAG, s"skip ${reader.nextString()}")
      }
    }
    reader.endObject()

    V("json2info", "end")
    Info(
      UID = uid,
      category = category,
      title = title,
      comment = comment,
      description = description,
      startDate = startDate,
      endDate = endDate,
      iconImage = iconImage,
      imageUrl = imgurl,
      masterUnit = masterUnit,
      otherUnit = otherUnit,
      showUnit = showUnit,
      subUnit = subUnit,
      supportUnit = supportUnit,
      sourceWebName = sourceWebName,
      sourceWebPromote = sourceWebPromote,
      webSales = webSales,
      discountInfo = discount,
      locations = slocation,
      showinfo = showInfo
    )
  }

  def json2category(reader: JsonReader): Category = {
    reader.beginObject()
    var code: Int = 0
    var name: String = ""
    while (reader.hasNext) {
      reader.nextName match {
        case "categoryCode" => code = reader.nextInt
        case "categoryName" => name = reader.nextString
        case _ => reader.skipValue()
      }
    }
    reader.endObject()
    new Category(code, name)
  }

  def json2ShowInfo(reader: JsonReader): ShowInfo = {
    reader.beginObject()

    var lat: Double = 0
    var lon: Double = 0
    var location: String = ""
    var locationName: String = ""
    var onSales: Boolean = false
    var price: Option[String] = None
    var time: String = ""

    while (reader.hasNext) {
      try{
        reader.nextName match {
          case "latitude" => lat = reader.nextDouble //FIXME:  java.lang.NumberFormatException: Invalid double: ""
          case "longitude" => lon = reader.nextDouble
          case "location" => location = reader.nextString
          case "locationName" => locationName = reader.nextString
          case "onSales" => onSales = reader.nextString == "Y"
          case "price" => price = Some(reader.nextString)
          case "time" => time = reader.nextString
          case _ => reader.skipValue()
        }
      }catch{
        case e:Exception =>
          W(TAG,e,e.getMessage)
          reader.skipValue()  //skip thi parseinge problem to keep
      }

    }
    reader.endObject()

    new ShowInfo(lat, lon, location, locationName, onSales, price, time)
  }


  def opt(s: String): Option[String] =
    if (TextUtils.isEmpty(s)) None else Some(s)


}