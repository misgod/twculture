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
import scala.collection.mutable.ArrayBuffer
import scala.Some
import android.text.TextUtils
import android.content.Context

import ExecutionContext.Implicits.global



case class Category(code: String, name: String, url:String="")

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
                     location: Option[String],
                     locationName: String,
                     onSales: Boolean,
                     price: Option[String],
                     time: String
                     )



case class Place(
                  groupTypeName: String,
                  mainTypeName: String,
                  mainTypePk: String,
                  name: String,
                  typename: Option[String], //古蹟、歷史建築....
                  level: Option[String], //直轄市定古蹟、國定古蹟
                  representImage: Option[String], //代表圖像連結 http://cloud.culture.jpg
                  intro: String,
                  cityName: String,
                  address: String,
                  arriveWay: Option[String],
                  latitude: Double,
                  longitude: Double,
                  openTime: String,
                  ticketPrice: String,
                  contact: Option[String],
                  phone: Option[String],
                  fax: Option[String],
                  email: Option[String],
                  website: Option[String],
                  srcWebsite: Option[String]
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

  def getPlacesType:Seq[Category]= ICulture.placesCategory


  def getPlaces(c: Category)(implicit ctx: Context): Future[Seq[Place]] = {
    D(TAG, s"getDetail: $c")

    for {
      reader <- ICulture.getPlace(c.url)
    } yield json2seq(reader, true)(json2place)
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
    var code: String = "0"
    var name: String = ""
    while (reader.hasNext) {
      reader.nextName match {
        case "categoryCode" => code = reader.nextString
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
    var location: Option[String] = None
    var locationName: String = ""
    var onSales: Boolean = false
    var price: Option[String] = None
    var time: String = ""

    while (reader.hasNext) {
      try {
        reader.nextName match {
          case "latitude" => lat = reader.nextDouble //FIXME:  java.lang.NumberFormatException: Invalid double: ""
          case "longitude" => lon = reader.nextDouble
          case "location" => location = opt(reader.nextString)
          case "locationName" => locationName = reader.nextString
          case "onSales" => onSales = reader.nextString == "Y"
          case "price" => price = Some(reader.nextString)
          case "time" => time = reader.nextString
          case _ => reader.skipValue()
        }
      } catch {
        case e: Exception =>
          W(TAG, e, e.getMessage)
          reader.skipValue() //skip thi parseinge problem to keep
      }

    }
    reader.endObject()

    new ShowInfo(lat, lon, location, locationName, onSales, price, time)
  }


  def json2place(reader: JsonReader): Place = {
    reader.beginObject()

    var groupTypeName: String = ""
    var mainTypeName: String = ""
    var mainTypePk: String = ""
    var name: String = ""
    var typename: Option[String] = None
    var level: Option[String] = None
    var representImage: Option[String] = None
    var intro: String = ""
    var cityName: String = ""
    var address: String = ""
    var arriveWay:Option[String] = None
    var latitude: Double = 0
    var longitude: Double = 0
    var openTime: String = ""
    var ticketPrice: String = ""
    var contact: Option[String] = None
    var phone: Option[String] = None
    var fax: Option[String] = None
    var email: Option[String] = None
    var website: Option[String] = None
    var srcWebsite: Option[String] = None

    while (reader.hasNext) {
      try {
        reader.nextName match {
          case "groupTypeName" => groupTypeName = reader.nextString
          case "mainTypeName" => mainTypeName = reader.nextString
          case "mainTypePk" => mainTypePk = reader.nextString
          case "name" => name = reader.nextString
          case "typename" => typename = opt(reader.nextString)
          case "level" => level = opt(reader.nextString)
          case "representImage" => representImage = opt(reader.nextString)
          case "intro" => intro = reader.nextString
          case "cityName" => cityName = reader.nextString
          case "address" => address = reader.nextString
          case "arriveWay" => arriveWay = opt(reader.nextString)
          case "latitude" => latitude = reader.nextDouble
          case "longitude" => longitude = reader.nextDouble
          case "openTime" => openTime = reader.nextString
          case "ticketPrice" => ticketPrice = reader.nextString
          case "contact" => contact = opt(reader.nextString)
          case "phone" => phone = opt(reader.nextString)
          case "fax" => fax = opt(reader.nextString)
          case "email" => email = opt(reader.nextString)
          case "website" => website = opt(reader.nextString)
          case "srcWebsite" => srcWebsite = opt(reader.nextString)
          case _ => reader.skipValue()
        }
      } catch {
        case e: Exception =>
          W(TAG, e, e.getMessage)
          reader.skipValue() //skip thi parseinge problem to keep
      }

    }
    reader.endObject()


    new Place(groupTypeName, mainTypeName, mainTypePk, name, typename, level, representImage,
      intro, cityName, address, arriveWay,latitude, longitude, openTime, ticketPrice, contact, phone,
      fax, email, website, srcWebsite)
  }


  def opt(s: String): Option[String] =
    if (TextUtils.isEmpty(s)) None else Some(s)


}