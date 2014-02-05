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

import java.io._
import scala._
import spray.json._
import InfoJsonProtocol._
import java.net.{HttpURLConnection, URL}
import java.util.zip.GZIPInputStream

object App {

  val extraFile = new File("extra.json")
  val format = new java.text.SimpleDateFormat("yyyyMMdd")


  def genMD5sum(): Unit =
    Utils.getFileTree(new File(".")).filter(_.getName.endsWith(".json")).foreach {
      file =>
        val hf = file.getName + ".md5"
        println(s"gen hash $hf")
        val writer = new PrintWriter(hf)
        writer.write(Utils.md5sum(file))
        writer.close()
    }


  def updateData() {
    ExtraData.write(extraFile)
    val dmap = ExtraData.toMap(ExtraData.read(extraFile))
    val categoies = OpenData.getCategories
    Utils.nozip(NoUnicodeEscCompactPrinter(categoies.toJson), new File(s"category.json"))

    categoies foreach {
      c =>
        val code = c.categoryCode
        val infos = OpenData.getInfo(code, dmap)
        println(s"download=> $code")
        Utils.nozip(NoUnicodeEscCompactPrinter(infos.toJson), new File(s"$code.json"))
    }
  }

  def move(target:String){
    Utils.getFileTree(new File(".")).withFilter(a=>
      a.getName.endsWith(".json") || a.getName.endsWith(".json.md5")).foreach{f=>
      println(s"${f.getName} ==>  ${target+f.getName}")
      f.renameTo(new File(target+f.getName))
    }
  }



  def main(args: Array[String]) {
    val targetPath = Config.targetPath


    try{
      updateData()

      genMD5sum()

      move(targetPath)

    }catch{
      case a:Throwable =>
      println(a.getStackTraceString)

      //TODO: notify me...
    }



  }
}

