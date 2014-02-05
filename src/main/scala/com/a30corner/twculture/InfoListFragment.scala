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

package com.a30corner.twculture


import android.support.v4.app.ListFragment
import android.os.{Handler, Bundle}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.language.postfixOps
import android.view._
import android.widget._
import com.a30corner.twculture.util.LogUtil._
import android.graphics.{Color, Bitmap}
import android.widget.AdapterView.{OnItemSelectedListener, OnItemClickListener}
import com.a30corner.twculture.util.Purify
import Purify._

import com.nostra13.universalimageloader.core.{ImageLoader, DisplayImageOptions}
import com.nostra13.universalimageloader.core.assist.{SimpleImageLoadingListener, ImageScaleType}
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer

import scala.Some
import com.a30corner.twculture.util.ImageLoaderConfig
import android.content.Context
import android.widget.AbsListView.RecyclerListener
import android.text.TextUtils


object InfoListFragment {
  def apply(c: Category): InfoListFragment = {
    val f = new InfoListFragment()
    val budle = new Bundle()
    budle.putSerializable("category", c)
    f.setArguments(budle)
    f
  }
}

class InfoListFragment extends ListFragment with CommonFragment {
  lazy val adapter = new InfoAdapter()
  lazy val options = ImageLoaderConfig.displayForList(new Handler())
  lazy val imgLoader = ImageLoader.getInstance

  lazy val category: Category = getArguments.get("category").asInstanceOf[Category]
  var curArea = 0

  override def onActivityCreated(savedState: Bundle): Unit = {
    super.onActivityCreated(savedState)
    V(TAG, "onActivityCreated")


    //skipping querying if data is ready..
    if(adapter.getCount==0) init()


    setListAdapter(adapter)
    getListView.setOnItemClickListener(adapter)
    getListView.setRecyclerListener(adapter)

    setTitle(getString(R.string.event_category) + " - " + category.name)
    setHasOptionsMenu(true)
  }

  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater): Unit = {
    inflater.inflate(R.menu.main, menu)
    import com.a30corner.twculture.function2OnItemSelectedListener

    val spinner = menu.findItem(R.id.action_location).getActionView.
      findViewById(R.id.citymenu).asInstanceOf[Spinner]
    spinner.setOnItemSelectedListener((_: AdapterView[_], _: Option[View], position: Int, _: Long) =>
      if (curArea != position) {
        curArea = position
        adapter.updateFilter()
      }
    )

    spinner.setSelection(curArea)
    super.onCreateOptionsMenu(menu, inflater)
  }


  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    V(TAG, "onCreate")

    imgLoader.init(ImageLoaderConfig.config(getActivity.getApplicationContext))
  }

  def init() = {
    implicit val ctx: Context = getActivity

    displayLoading(true)

    val detail = OpenData.getDetail(category)
    detail onSuccess {
      case data =>
        adapter.setData(category, data)
        displayLoading(false)
    }

    detail onFailure {
      case e => E(TAG, e, "get detail fail...")
        displayLoading(false)
        toast(R.string.update_faile)
    }

  }


  override def onDestroy(): Unit = {
    imgLoader.destroy()
    displayLoading(false)
    super.onDestroy()
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, s: Bundle): View =
    inflater.inflate(R.layout.infolist, container, false)


  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.action_search => Toast.makeText(getActivity, R.string.not_implement, Toast.LENGTH_SHORT).show()
      case _ => Toast.makeText(getActivity, "cc", Toast.LENGTH_SHORT).show()
    }

    super.onOptionsItemSelected(item)
  }

  def cities: Array[String] = getActivity.getResources.getStringArray(R.array.cities)

  sealed class InfoAdapter extends BaseAdapter with OnItemClickListener with RecyclerListener {
    var data: Seq[Info] = Nil
    var category: Category = null
    var dataFiltered: Seq[Info] = Nil


    def setData(c: Category, newdata: Seq[Info]): Unit = runOnMain {
      () =>
        D(TAG, s"updated=> ${newdata.size}")
        data = newdata
        category = c
        updateFilter()
    }


    def updateFilter(): Unit = {
      D(TAG, s"updateFilter ${cities(curArea)}")

      if (curArea == 0) dataFiltered = data
      else dataFiltered = data.filter(_.locations.exists(cities(curArea).contains(_)))

      notifyDataSetInvalidated()
    }

    private def narrowlocation(loc: String): String =
      if (curArea == 0) loc
      else
        loc filter (cities(curArea).contains(_))

    override def getCount: Int = dataFiltered.size

    override def getItemId(p: Int): Long = p

    override def getItem(position: Int): AnyRef = dataFiltered(position)

    def newView(position: Int, cview: View, parent: ViewGroup): Unit = {
      val title = cview.findViewById(R.id.title).asInstanceOf[TextView]
      val locations = cview.findViewById(R.id.locations).asInstanceOf[TextView]
      val free = cview.findViewById(R.id.free).asInstanceOf[TextView]
      val showdate = cview.findViewById(R.id.showdate).asInstanceOf[TextView]
      val showunit = cview.findViewById(R.id.showunit).asInstanceOf[TextView]
      val cover = cview.findViewById(R.id.cover).asInstanceOf[ImageView]

      cview.setTag(ViewHolder(locations, free, title, showunit, showdate, cover))
    }

    def bindView(position: Int, cview: View, parent: ViewGroup): View = {
      val info = dataFiltered(position)

      val holder = cview.getTag.asInstanceOf[ViewHolder]
      holder.locations.setText(narrowlocation(info.locations))
      holder.title.setText(info.title)

      val isOnsale = info.showinfo exists (_.onSales)

      holder.free.setText(if (isOnsale) R.string.sales else R.string.free)
      holder.free.setBackgroundColor(if (isOnsale) Color.RED else Color.GREEN)

      holder.showdate.setText(mergeDate(info.startDate, info.endDate))

      val unit = info.showUnit.getOrElse(info.masterUnit)
      if (TextUtils.isEmpty(unit)) {
        holder.showunit.setText(R.string.no_unit)
      } else {
        holder.showunit.setText(unit)
      }


      holder.cover.setImageBitmap(null) //reset

      info.imageUrl match {
        //TODO: too much String concat , need enhance
        case Some(url) if url.startsWith("http") =>
          imgLoader.displayImage(url, holder.cover, options, new SimpleImageLoadingListener {
            override def onLoadingComplete(url: String, view: View, img: Bitmap) =
              holder.cover.setImageBitmap(img)
          })
        case Some(url) =>
          imgLoader.displayImage(OpenData.cultureUrl + url, holder.cover, options, new SimpleImageLoadingListener {
            override def onLoadingComplete(url: String, view: View, img: Bitmap) =
              holder.cover.setImageBitmap(img)
          })
        case None => holder.cover.setImageResource(R.drawable.ic_empty)
      }
      cview
    }

    override def getView(position: Int, cview: View, parent: ViewGroup): View = {
      var v = cview
      if (v == null) {
        v = getActivity.getLayoutInflater.inflate(R.layout.info, null)
        newView(position, v, parent)
      }
      bindView(position, v, parent)
    }

    def onItemClick(parent: AdapterView[_], view: View, p: Int, id: Long): Unit =
      change(DetailInfoFragment(dataFiltered(p)))

    def onMovedToScrapHeap(view: View): Unit =
      imgLoader.cancelDisplayTask(view.getTag.asInstanceOf[ViewHolder].cover)
  }

  case class ViewHolder(
                         locations: TextView,
                         free: TextView,
                         title: TextView,
                         showunit: TextView,
                         showdate: TextView,
                         cover: ImageView
                         )


}
