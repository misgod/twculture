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
import java.security.MessageDigest
import java.util.zip.GZIPOutputStream
import spray.json.{CompactPrinter, PrettyPrinter, JsonPrinter}
import scala.annotation.tailrec
import java.lang

object Utils {


  def zip(s: String, f: File): File = {
    var writer: BufferedWriter = null
    try {
      writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(f)), "UTF-8"))
      writer.write(s)
      writer.flush()
      f
    } finally {
      if (writer != null) writer.close()
    }
  }

  def nozip(s: String, f: File): File = {
    var writer: BufferedWriter = null
    try {
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"))
      writer.write(s)
      writer.flush()
      f
    } finally {
      if (writer != null) writer.close()
    }
  }

  def same(f1: File, f2: File): Boolean = md5sum(f1) == md5sum(f2)

  def md5sum(f: File): String = {
    val in = new FileInputStream(f)
    val s = md5sum(in)
    in.close()
    s
  }

  def md5sum(input: InputStream): String = {
    val bis = new BufferedInputStream(input)
    val buf = new Array[Byte](8192)
    val md5 = MessageDigest.getInstance("MD5")
    Stream.continually(bis.read(buf)).takeWhile(_ != -1).foreach(md5.update(buf, 0, _))
    md5.digest().map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") ( _ + _)
  }



  import scala.collection.JavaConversions._

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
    else Stream.empty)


  import java.io.{File,FileInputStream,FileOutputStream}





}


trait NoUnicodeEscJsonPrinter extends JsonPrinter {


  override protected def printString(s: String, sb: lang.StringBuilder): Unit = {
    @tailrec
    def printEscaped(s: String, ix: Int) {
      if (ix < s.length) {
        s.charAt(ix) match {
          case '"' => sb.append("\\\"")
          case '\\' => sb.append("\\\\")
          case x if 0x20 <= x && x < 0x7F => sb.append(x)
          case '\b' => sb.append("\\b")
          case '\f' => sb.append("\\f")
          case '\n' => sb.append("\\n")
          case '\r' => sb.append("\\r")
          case '\t' => sb.append("\\t")
          case x => sb.append(x)
        }
        printEscaped(s, ix + 1)
      }
    }
    sb.append('"')
    printEscaped(s, 0)
    sb.append('"')
  }
}

trait NoUnicodeEscPrettyPrinter  extends PrettyPrinter with NoUnicodeEscJsonPrinter
object NoUnicodeEscPrettyPrinter extends NoUnicodeEscPrettyPrinter

trait NoUnicodeEscCompactPrinter   extends CompactPrinter  with NoUnicodeEscJsonPrinter
object NoUnicodeEscCompactPrinter  extends NoUnicodeEscCompactPrinter

