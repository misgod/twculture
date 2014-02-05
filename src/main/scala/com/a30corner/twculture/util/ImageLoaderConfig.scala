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


package com.a30corner.twculture.util


import com.nostra13.universalimageloader.utils.StorageUtils
import com.nostra13.universalimageloader.core.{DisplayImageOptions, ImageLoaderConfiguration}
import android.os.{Handler, AsyncTask}
import com.nostra13.universalimageloader.core.assist.{ImageScaleType, QueueProcessingType}
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.core.download.BaseImageDownloader
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder
import android.content.Context
import scala.language.postfixOps
import com.a30corner.twculture.R
import android.graphics.Bitmap
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer

object ImageLoaderConfig {


  def config(ctx: Context) = {


    val cacheDir = StorageUtils.getCacheDirectory(ctx)

    // Create global configuration and initialize ImageLoader with this configuration
    new ImageLoaderConfiguration.Builder(ctx)
      //      .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
      //      .discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75, null)
      .taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
      //      .taskExecutor(new ThreadPoolExecutor(3, 3, 3, TimeUnit.SECONDS, new LinkedBlockingDeque[Runnable]()))
      .taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR)
      .threadPoolSize(3) // default
      .threadPriority(Thread.NORM_PRIORITY - 1) // default
      .tasksProcessingOrder(QueueProcessingType.LIFO) // default
      .denyCacheImageMultipleSizesInMemory()
      .memoryCache(new LruMemoryCache(5 mb))
      //      .memoryCacheSize(5 mb)
      //      .memoryCacheSizePercentage(13) // default
      //      .discCache(new UnlimitedDiscCache(cacheDir)) // default
      .discCacheSize(50 mb)
      .discCacheFileCount(200)
      .discCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
      .imageDownloader(new BaseImageDownloader(ctx)) // default
      .imageDecoder(new BaseImageDecoder(false)) // default
      .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
      //      .writeDebugLogs()
      .build()
  }


  def displayForList(h:Handler) = new DisplayImageOptions.Builder()
    .showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
    .showImageOnFail(R.drawable.ic_empty) // resource or drawable
    .resetViewBeforeLoading(false) // default
    .delayBeforeLoading(300)
    .cacheInMemory(true) // default
    .cacheOnDisc(true) // default
    .considerExifParams(false) // default
    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
    .bitmapConfig(Bitmap.Config.RGB_565) // default   A8888
    //      .decodingOptions(...)
    .displayer(new SimpleBitmapDisplayer()) // default
    .handler(h) // default
    .build()

  def displayForDetail(h:Handler) = new DisplayImageOptions.Builder()
    .showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
    .showImageOnFail(R.drawable.ic_empty) // resource or drawable
    .resetViewBeforeLoading(false) // default
    .delayBeforeLoading(500)
    .cacheInMemory(false) // default
    .cacheOnDisc(true) // default
    .imageScaleType(ImageScaleType.NONE)
    .bitmapConfig(Bitmap.Config.RGB_565) // default   A8888
    .displayer(new SimpleBitmapDisplayer()) // default
    .handler(h) // default
    .build()


  implicit class ReadableSize(amount: Int) {
    def mb: Int = amount * 1024 * 1024


  }

}
