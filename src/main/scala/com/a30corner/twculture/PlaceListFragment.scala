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

import android.os.{Handler, Bundle}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.language.postfixOps
import android.view._
import android.widget._
import com.a30corner.twculture.util.LogUtil._
import android.graphics.{Color, Bitmap}
import android.widget.AdapterView.OnItemClickListener
import com.a30corner.twculture.util.Purify
import Purify._

import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener

import scala.Some
import com.a30corner.twculture.util.ImageLoaderConfig
import android.content.Context
import android.widget.AbsListView.RecyclerListener
import android.text.TextUtils
import android.app.ListFragment


object PlaceListFragment {
  def apply(c: Category): PlaceListFragment = {
    val f = new PlaceListFragment()
    val budle = new Bundle()
    budle.putSerializable("category", c)
    f.setArguments(budle)
    f
  }
}


class PlaceListFragment extends ListFragment with CommonFragment {
  lazy val adapter = new PlaceAdapter()
  lazy val options = ImageLoaderConfig.displayForList(new Handler())
  lazy val imgLoader = ImageLoader.getInstance

  lazy val category: Category = getArguments.get("category").asInstanceOf[Category]
  var curArea = 0

  override def onActivityCreated(savedState: Bundle): Unit = {
    super.onActivityCreated(savedState)
    V(TAG, "onActivityCreated")


    //skipping querying if data is ready..
    if (adapter.getCount == 0) init()


    setListAdapter(adapter)
    getListView.setOnItemClickListener(adapter)
    getListView.setRecyclerListener(adapter)

    setTitle(category.name)
    setHasOptionsMenu(true)
  }

  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater): Unit = {
    inflater.inflate(R.menu.placelist, menu)
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

    val detail = OpenData.getPlaces(category)
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
    inflater.inflate(R.layout.list, container, false)


  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.action_map => Toast.makeText(getActivity, R.string.not_implement, Toast.LENGTH_SHORT).show()
      case _ => Toast.makeText(getActivity, "cc", Toast.LENGTH_SHORT).show()
    }

    super.onOptionsItemSelected(item)
  }

  def cities: Array[String] = getActivity.getResources.getStringArray(R.array.cities)

  sealed class PlaceAdapter extends BaseAdapter with OnItemClickListener with RecyclerListener {
    var data: Seq[Place] = Nil
    var category: Category = null


    def setData(c: Category, newdata: Seq[Place]): Unit = runOnMain {
      () =>
        D(TAG, s"updated=> ${newdata.size}")
        data = newdata
        category = c
        updateFilter()
    }


    def updateFilter(): Unit = {
      D(TAG, s"updateFilter ${cities(curArea)}")

      //FIXME...
      //      if (curArea == 0) dataFiltered = data
      //      else dataFiltered = data.filter(_.locations.exists(cities(curArea).contains(_)))

      notifyDataSetInvalidated()
    }


    override def getCount: Int = data.size

    override def getItemId(p: Int): Long = p

    override def getItem(position: Int): AnyRef = data(position)

    def newView(position: Int, cview: View, parent: ViewGroup): Unit = {
      val title = cview.findViewById(R.id.title).asInstanceOf[TextView]
      val openTime = cview.findViewById(R.id.openTime).asInstanceOf[TextView]
//      val price = cview.findViewById(R.id.price).asInstanceOf[TextView]
      val address = cview.findViewById(R.id.address).asInstanceOf[TextView]
      val cover = cview.findViewById(R.id.cover).asInstanceOf[ImageView]

      cview.setTag(ViewHolder(title, address, openTime, cover))
    }

    def bindView(position: Int, cview: View, parent: ViewGroup): View = {
      val place = data(position)

      val holder = cview.getTag.asInstanceOf[ViewHolder]

      holder.title.setText(place.name)
      holder.address.setText(place.cityName + " " +  place.address)
//      holder.price.setText(place.ticketPrice)
      holder.openTime.setText(place.openTime)

      holder.cover.setImageBitmap(null) //reset
      holder.cover.setVisibility(View.GONE)

      place.representImage match {
        //TODO: too much String concat , need enhance
        case Some(url) if url.startsWith("http") =>
          imgLoader.displayImage(url, holder.cover, options, new SimpleImageLoadingListener {
            override def onLoadingComplete(url: String, view: View, img: Bitmap) =
              holder.cover.setImageBitmap(img)
            holder.cover.setVisibility(View.VISIBLE)
          })
        case None => holder.cover.setVisibility(View.GONE)
      }
      cview
    }

    override def getView(position: Int, cview: View, parent: ViewGroup): View = {
      var v = cview
      if (v == null) {
        v = getActivity.getLayoutInflater.inflate(R.layout.place, null)
        newView(position, v, parent)
      }
      bindView(position, v, parent)
    }

    def onItemClick(parent: AdapterView[_], view: View, p: Int, id: Long): Unit ={
      Toast.makeText(getActivity, "Not implement yet", Toast.LENGTH_SHORT).show()
      //TODO
      //      change(DetailInfoFragment(data(p)))
    }


    def onMovedToScrapHeap(view: View): Unit =
      imgLoader.cancelDisplayTask(view.getTag.asInstanceOf[ViewHolder].cover)
  }

  case class ViewHolder(title: TextView,
                        address: TextView,
                        openTime: TextView,
                        cover: ImageView
                         )


}
