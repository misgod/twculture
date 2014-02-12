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


import util.LogUtil._
import android.os.Bundle
import android.widget.Toast
import android.app.Fragment

trait CommonFragment extends Fragment {
  val TAG = this.getClass.getSimpleName
  var isDestoryed = false


  def activity: Option[HostActivity] =
    if(getActivity ==null) None else Some(getActivity.asInstanceOf[HostActivity])

  def setTitle(t: String): Unit = activity match{
    case Some(x) => x.setTitle(t)
    case None =>  W(TAG, s"skipping setTitle: $t")
  }

  def displayLoading(show: Boolean): Unit = activity match{
    case Some(x) => x.displayLoading(show)
    case None =>  W(TAG, s"skipping setdisplayLoadingTitle: $show")
  }


  def change(f: Fragment): Unit = activity match{
    case Some(x) => x.change(f)
    case None =>  W(TAG, s"skipping change: ${f.getClass.getSimpleName}")
  }


  def changePage[T](para: T)(f: T => Fragment): Unit = activity match{
    case Some(x) => x.changePage(para)(f)
    case None =>  W(TAG, s"skipping changePage")
  }

  def runOnMain(f: () => Unit): Unit =activity match{
    case Some(x) if !isDestoryed=> x.runOnUiThread(f)
    case None =>  W(TAG, s"skipping runOnMain")
  }


  def toast(msgId:Int):Unit = runOnMain(()=>
    Toast.makeText(getActivity,msgId, Toast.LENGTH_LONG).show()
  )



  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    isDestoryed = false
  }

  override def onDestroy(): Unit = {
    isDestoryed = true
    super.onDestroy()
  }

}
