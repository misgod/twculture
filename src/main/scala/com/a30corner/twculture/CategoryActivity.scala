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

import android.support.v4.app.{FragmentTransaction, Fragment}

import android.os.Bundle
import android.view.View
import android.support.v7.app.ActionBarActivity


class CategoryActivity extends ActionBarActivity {

  override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    setContentView(R.layout.main)

    if (savedState == null) {
      change(new CategoryFragment, stack = false)
    } else {
      //do nothing
    }

  }

  def displayLoading(loading: Boolean): Unit = runOnUiThread(
    () =>
      findViewById(R.id.loading).setVisibility(if (loading) View.VISIBLE else View.GONE)
  )


  def setTitle(title: String): Unit =  getSupportActionBar.setTitle(title)


  def change(f: Fragment, stack: Boolean = true): Unit = {
    val transaction = getSupportFragmentManager.beginTransaction()
    transaction.replace(R.id.container, f)
    if (stack) {
      transaction.addToBackStack(null)
    }
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
    transaction.commit()
  }


  def changePage[T](para: T)(f: T => Fragment): Unit =
    change(f(para))


}
