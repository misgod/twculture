// so we can use keywords from Android, such as 'Android' and 'proguardOptions'
import android.Keys._
 
import android.Dependencies.{apklib,aar}

// load the android plugin into the build
android.Plugin.androidBuild
 
// project name, completely optional
name := "TaiwanCulture"
 
// pick the version of scala you want to use
scalaVersion := "2.10.2"
 
// scala 2.10 flag for feature warnings
scalacOptions in Compile ++= Seq("-feature", "-language:implicitConversions")


// for non-ant-based projects, you'll need this for the specific build target:
platformTarget in Android := "android-17"
 
// call install and run without having to prefix with android:
run <<= run in Android
 
install <<= install in Android

useProguard in Android := true

//proguardCache in Android += ProguardCache("android.support.v4") % "support-v4" % "r7"


proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize","-dontpreverify","-dontnote scala.**")
               //"-dump dump.txt", "-printmapping /mapping.txt")

proguardOptions in Android ++= Seq(
  "-keepclasseswithmembers class org.opendata.twculture.* {*;}",
  "-keepclasseswithmembers class org.opendata.twculture.** {*;}",
  "-keepclassmembers class * { ** MODULE$;}",
//  "-keep class scala.collection.SeqLike {public protected *;}" ,
  "-keepclasseswithmembers class scala.concurrent.Promise{*;}",
  "-keepclasseswithmembers class scala.concurrent.Promise${*;}",
  "-keepclasseswithmembers class scala.concurrent.Future${*;}",
  "-keepclasseswithmembers class scala.concurrent.ExecutionContext$Implicits${*;}",
  "-keepclasseswithmembers class scala.Predef${*;}",
  "-keepclasseswithmembers class android.support.v7.**{*;}",
  "-keepclasseswithmembers class scala.Array${** canBuildFrom;}"
)
//
//

//proguardConfig in Android <<= baseDirectory map { b =>
//  IO.readLines(b / ( "proguard.cfg"))
//}

//libraryDependencies += "com.android.support" % "gridlayout-v7" % "18.0.0"

libraryDependencies += "com.android.support" % "appcompat-v7" % "18.0.0"

libraryDependencies += "com.nostra13.universalimageloader" % "universal-image-loader" % "1.9.0"


