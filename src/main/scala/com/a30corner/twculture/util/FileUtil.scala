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

import java.io._
import java.nio.channels.Channels
import java.security.MessageDigest


object FileUtil {
  @throws[java.io.IOException]
  def write(f: File, input: InputStream): File = {
    if (!f.getParentFile.exists())
      f.getParentFile.mkdirs()

    val bis = new BufferedInputStream(input)
    val bos = new FileOutputStream(f)
    val buf = new Array[Byte](8192)

    try{
      Stream.continually(input.read(buf)).takeWhile(_ != -1).foreach(bos.write(buf, 0, _))
    }catch{
      case e:IOException=> throw e
    }finally{
      bos.close()
      bis.close()
    }
    f
  }


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
    }.foldLeft("") {
      _ + _
    }
  }


}
