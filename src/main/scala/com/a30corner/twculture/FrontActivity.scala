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
import android.view.View
import android.app.Activity
import android.content.Intent
import android.widget.TextView
import android.graphics.Typeface


class FrontActivity extends Activity {
  lazy val typeface = Typeface.createFromAsset(getAssets, "fonts/wt034_trim.ttf")

  override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    setContentView(R.layout.front)


    findViewById(R.id.event).asInstanceOf[TextView].setTypeface(typeface)
    findViewById(R.id.place).asInstanceOf[TextView].setTypeface(typeface)

  }

  def onClick(v: View): Unit = v.getId match {
    case R.id.event => startActivity(new Intent("twcultire.intent.action.EVENT"))
    case R.id.place => startActivity(new Intent("twcultire.intent.action.PLACE"))
  }

}
