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

import android.os.Bundle

import scala.language.postfixOps
import android.view.{View, ViewGroup, LayoutInflater}
import android.widget._
import com.a30corner.twculture.util.LogUtil._
import android.widget.AdapterView.OnItemClickListener
import android.graphics.Typeface
import android.app.Fragment
import android.content.Context
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

abstract class AbstractGridFragment extends Fragment with CommonFragment {
  lazy val adapter = new CategoryAdapter()

  def populateData:Future[Seq[Category]]

  def onItemSelected(category:Category): Unit

  override def onActivityCreated(savedInstanceState: Bundle): Unit = {
    super.onActivityCreated(savedInstanceState)

    val absListView = getView.findViewById(R.id.gridview).asInstanceOf[GridView]
    absListView.setAdapter(adapter)
    absListView.setOnItemClickListener(adapter)


    if (adapter.getCount == 0) init()


    D("onActivityCreated", this.toString)
  }


  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    D(TAG, "onCreate")
  }

  def init() = {
    D(TAG, "Init")

    displayLoading(true)
    val catrgories = populateData

    catrgories onSuccess {
      case data =>
        V(TAG, "onSuccess")
        adapter.setData(data)
        displayLoading(false)
    }

    catrgories onFailure {
      case e => E(TAG, e, "get category fail...")
        displayLoading(false)
      //TODO: hint user....\
    }
  }


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, s: Bundle): View = {
    inflater.inflate(R.layout.category, container, false)
  }


  sealed class CategoryAdapter extends BaseAdapter with OnItemClickListener {
    var data: Seq[Category] = Nil

    def setData(newdata: Seq[Category]): Unit =
      runOnMain {
        () =>
          data = newdata
          notifyDataSetInvalidated()
      }

    override def getCount: Int = data.size

    override def getItemId(p: Int): Long = p

    override def getItem(position: Int): AnyRef = data(position)

    lazy val typeface = Typeface.createFromAsset(getActivity.getAssets, "fonts/wt034_trim.ttf")


    def newView(position: Int, cview: View, parent: ViewGroup): Unit = {
      val title = cview.findViewById(R.id.title).asInstanceOf[TextView]
      title.setTypeface(typeface, Typeface.NORMAL)

      val holder = ViewHolder(title)
      cview.setTag(holder)
    }

    def bindView(position: Int, cview: View, parent: ViewGroup): View = {
      val holder = cview.getTag.asInstanceOf[ViewHolder]
      holder.title.setText(data(position).name)
      cview
    }

    override def getView(position: Int, cview: View, parent: ViewGroup): View = {
      var result = cview
      if (result == null) {
        result = getActivity.getLayoutInflater.inflate(R.layout.icons, null)
        newView(position, result, parent)
      }
      bindView(position, result, parent)
    }

    def onItemClick(p1: AdapterView[_], p2: View, p: Int, p4: Long): Unit =
      onItemSelected(data(p))

    case class ViewHolder(title: TextView)
  }
}
