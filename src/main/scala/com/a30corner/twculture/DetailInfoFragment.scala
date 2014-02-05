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

import android.support.v4.app.Fragment
import android.os.{Handler, Bundle}

import scala.language.postfixOps
import android.view.{View, ViewGroup, LayoutInflater}
import android.widget.{LinearLayout, ImageView, TextView}

import android.graphics.Bitmap
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener
import android.text.{SpannableString, TextUtils}
import com.a30corner.twculture.util.{LogUtil, ImageLoaderConfig, Purify}
import com.a30corner.twculture.util.Purify._


import com.a30corner.twculture.util.LogUtil._
import ViewGroup.LayoutParams.{WRAP_CONTENT, MATCH_PARENT}
import android.text.style.{URLSpan, UnderlineSpan}
import android.view.View.OnClickListener
import android.content.Intent
import android.net.Uri


object DetailInfoFragment {
  def apply(info: Info): DetailInfoFragment = {

    val f = new DetailInfoFragment()
    val budle = new Bundle()
    budle.putSerializable("info", info)
    f.setArguments(budle)
    f
  }
}

class DetailInfoFragment extends Fragment {
  val TAG = "DetailInfoFragment"
  lazy val options = ImageLoaderConfig.displayForDetail(new Handler)
  lazy val info = getArguments.get("info").asInstanceOf[Info]


  override def onActivityCreated(savedInstanceState: Bundle): Unit = {
    super.onActivityCreated(savedInstanceState)

    implicit val cv = getView

    val coverView = cv.findViewById(R.id.cover).asInstanceOf[ImageView]
    val titleView = cv.findViewById(R.id.title).asInstanceOf[TextView]
    val descriptionView = cv.findViewById(R.id.description).asInstanceOf[TextView]

    titleView.setText(info.title)


    info.description match {
      case Some(desc) => descriptionView.setText(desc)
      case None => V(TAG, "no description")
    }


    val eventDateView = cv.findViewById(R.id.event_date).asInstanceOf[TextView]
    eventDateView.setText(mergeDate(info.startDate, info.endDate))


    assign(R.id.lab_discount, R.id.discount, info.discountInfo)
    assign(R.id.lab_eventsource, R.id.eventsource, info.sourceWebName)
    assign(R.id.lab_eventweb, R.id.eventweb, info.webSales)
    assign(R.id.lab_event_contact, R.id.event_contact, info.sourceWebPromote)
    assign(R.id.lab_showunit, R.id.showunit, info.showUnit)
    assign(R.id.lab_masterunit, R.id.masterunit, info.masterUnit)
    assign(R.id.lab_subunit, R.id.subunit, info.subUnit)
    assign(R.id.lab_supportunit, R.id.supportunit, info.supportUnit)
    assign(R.id.lab_otherunit, R.id.otherunit, info.otherUnit)

    info.imageUrl match {
      case Some(url) if url.startsWith("http") =>
        ImageLoader.getInstance.displayImage(url, coverView, options, new SimpleImageLoadingListener {
          override def onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
            coverView.setImageBitmap(loadedImage)
          }
        })
      case Some(url) =>
        ImageLoader.getInstance.displayImage(OpenData.cultureUrl + url, coverView, options, new SimpleImageLoadingListener {
          override def onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
            coverView.setImageBitmap(loadedImage)
          }
        })
      case None => coverView.setImageBitmap(null)
    }


    D(TAG, s"ShowInfo count: ${info.showinfo.size}")

    val inflater = LayoutInflater.from(getActivity)
    info.showinfo.foreach {
      show =>
        val container = getView.findViewById(R.id.container).asInstanceOf[ViewGroup]
        V("inflater", show.toString)

        val view = inflater.inflate(R.layout.show_info, null, false)

        val content = new SpannableString(show.locationName)
        content.setSpan(new URLSpan("#"), 0, show.locationName.length, 0)

        val content1 = new SpannableString(show.location)
        content1.setSpan(new URLSpan("#"), 0, show.location.length, 0)

        val time = show.time.split(";").map(Purify.refineDateStr).mkString("\n")

        assign(R.id.lab_show_time, R.id.show_time, time)(view)
        assign(R.id.lab_locationName, R.id.locationName, content)(view)
        assign(R.id.lab_location, R.id.location, content1)(view)
        assign(R.id.lab_price, R.id.price, show.price)(view)

        view.findViewById(R.id.locationName).setOnClickListener {
          v: View =>
            val uri = Uri.parse(s"geo:${show.lat},${show.lon}?q=${show.locationName}")
            val intent = new Intent(android.content.Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        view.findViewById(R.id.location).setOnClickListener {
          v: View =>
            val uriStr = if (show.lat != 0 && show.lon != 0) {
              s"geo:${show.lat},${show.lon}?q=${show.lat},${show.lon} (${show.location})"
            } else {
              s"geo:0,0?q=${show.location}"
            }
            val intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr))
            startActivity(intent)
        }
        val params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        params.setMargins(16, 10, 16, 10)
        container.addView(view, params)
    }
  }


  private def assign(lab: Int, text: Int, o: Option[CharSequence])(implicit parent: View): Unit = o match {
    case Some(x) => assign(lab, text, x)
    case None =>
  }
                                                                Purify
  private def assign(lab: Int, text: Int, s: CharSequence)(implicit parent: View): Unit = {
    val textview = parent.findViewById(text).asInstanceOf[TextView]
    textview.setText(s)
    if (!TextUtils.isEmpty(s)) {
      textview.setVisibility(View.VISIBLE)
      parent.findViewById(lab).setVisibility(View.VISIBLE)
    }
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, s: Bundle): View =
    inflater.inflate(R.layout.detail_info, container, false)

}
