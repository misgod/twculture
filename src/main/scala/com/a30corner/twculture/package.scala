package com.a30corner

import android.widget.AdapterView.OnItemSelectedListener
import android.widget.AdapterView
import android.view.View


package object twculture {

  implicit def function2ViewOnClickListener(f: View => Unit): View.OnClickListener =
    new View.OnClickListener() {
      override def onClick(view: View) = f(view)
    }


  implicit def function2OnItemSelectedListener(f: (AdapterView[_], Option[View], Int, Long) => Unit): OnItemSelectedListener =
    new OnItemSelectedListener() {
      override def onItemSelected(a: AdapterView[_], b: View, c: Int, d: Long): Unit = f(a, Some(b), c, d)
      override def onNothingSelected(parent: AdapterView[_]): Unit = f(parent, None, -1, -1)
    }


  implicit def fun2Runable(f: () => Unit): Runnable = new Runnable {
    override def run(): Unit = f()
  }

}
