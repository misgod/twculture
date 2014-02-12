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
import android.view.{View, LayoutInflater, ViewGroup}
import scala.concurrent.Future
import android.widget.GridView
import scala.concurrent.ExecutionContext.Implicits.global


class CategoryFragment extends AbstractGridFragment with CommonFragment {
  override def onActivityCreated(savedInstanceState: Bundle): Unit = {
    super.onActivityCreated(savedInstanceState)
    setTitle(getString(R.string.event_category))
  }

  def populateData = OpenData.getCategories(getActivity)


  override def onItemSelected(category: Category): Unit = changePage(category)(InfoListFragment(_))
}


class PlaceTypeFragment extends AbstractGridFragment with CommonFragment {
  override def onActivityCreated(savedInstanceState: Bundle): Unit = {
    super.onActivityCreated(savedInstanceState)
    setTitle(getString(R.string.place_category))
  }

  def populateData = Future {
    OpenData.getPlacesType
  }


  override def onCreateView(a: LayoutInflater, b: ViewGroup, c: Bundle): View = {
    val v = super.onCreateView(a, b, c)
    v.findViewById(R.id.gridview).asInstanceOf[GridView].setNumColumns(2)
    v
  }


  override def onItemSelected(category: Category): Unit = changePage(category)(PlaceListFragment(_))

}
