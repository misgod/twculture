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

import java.text.SimpleDateFormat
import java.text.ParseException
import LogUtil._
import java.util.{TimeZone, Locale, Calendar}
import scala.Predef._
import scala.Some
import Calendar.{MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY}

object Purify {
  lazy val dformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
  lazy val dayOfWeekMap = Map(
    MONDAY -> "一",
    TUESDAY -> "二",
    WEDNESDAY -> "三",
    THURSDAY -> "四",
    FRIDAY -> "五",
    SATURDAY -> "六",
    SUNDAY -> "日"
  )


  def mergeDate(begin: String, end: Option[String]): String =
    end match {
      case Some(x) if x != begin => s"$begin ~ $x"
      case _ => begin
    }

  def dayOfWeek(date: String): Option[String] = {
    try {
      val calendar = Calendar.getInstance(Locale.CHINESE)
      calendar.setTime(dformat.parse(date))
      dayOfWeekMap.get(calendar.get(Calendar.DAY_OF_WEEK))
    } catch {
      case e: ParseException =>
        W("dayOfWeek", s"date parse fail...$date")
        None
    }
  }


  /** yyyy/MM/dd HH:mm:ss --> yyyy/MM/dd(DayOfWeek) [HH:mm:ss]  */
  def refineDateStr(date: String): String = {
    dayOfWeek(date) match {
      case Some(x) =>
        val d = date.trim.split("\\s", 2)

        //11:59:00 is a trick to represent no time infomation or whole day
        val time = if (d(1) == "23:59:00") "" else d(1)


        s"($x) ${d(0)} $time"
      case None => date

    }
  }


}
