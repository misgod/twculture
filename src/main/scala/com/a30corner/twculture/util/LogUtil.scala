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



import android.util.Log


object LogUtil {
  val TAG = "twculture"
  val DEBUG = true

  def V(tag: String, msg: =>String): Unit = if (DEBUG) Log.v(TAG, s"$tag > $msg")

  def D(tag: String, msg: =>String): Unit = if (DEBUG) Log.d(TAG, s"$tag > $msg")

  def E(tag: String, msg: =>String): Unit = if (DEBUG) Log.e(TAG, s"$tag > $msg")

  def W(tag: String, msg: =>String): Unit =
    Log.w(TAG, s"$tag > $msg")

  def W(tag: String, throwable: Throwable, msg: =>String): Unit =
    Log.w(TAG, s"$tag > $msg", throwable)


  def E(tag: String, throwable: Throwable, msg: =>String): Unit =
    Log.e(TAG, s"$tag > $msg", throwable)
}