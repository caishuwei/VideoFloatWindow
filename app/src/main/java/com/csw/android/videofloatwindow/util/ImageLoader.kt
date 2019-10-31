package com.csw.android.videofloatwindow.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.*
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.model.stream.MediaStoreVideoThumbLoader
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.signature.ObjectKey
import com.csw.android.videofloatwindow.app.MyApplication

class ImageLoader {
    companion object {

//        fun loadLocalVideoImage(with: Any?, imageView: ImageView, videoDbId: Long, placeHolderImgResId: Int = 0) {
//            //url检查
//            if (TextUtils.isEmpty(url)) {
//                imageView.setImageResource(placeHolderImgResId)
//                return
//            }
//            //上下文检查
//            val context: Any
//            if (with == null) {
//                context = imageView.context
//            } else {
//                context = with
//            }
//            if (context is Activity
//                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
//                    && context.isDestroyed) {
//                //上下文是Activity，既然在这个页面加载图片，而页面已销毁，那就不用加载了
//                return
//            }
//            try {
//                var requestBuilder = getRequestManager(context, imageView.context)
//                        .load("")
//                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//                if (placeHolderImgResId != 0) {
//                    requestBuilder = requestBuilder.placeholder(placeHolderImgResId)
//                }
//                requestBuilder.into(imageView)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

        /**
         * 根据url加载图片
         * @param with 在什么页面加载(如Application,Activity,Fragment，Context),glide可以根据加载环境控制
         * 图片加载任务
         */
        fun loadImage(with: Any?, imageView: ImageView, url: String?, placeHolderImgResId: Int = 0) {
            //url检查
            if (TextUtils.isEmpty(url)) {
                imageView.setImageResource(placeHolderImgResId)
                return
            }
            //上下文检查
            val context: Any
            if (with == null) {
                context = imageView.context
            } else {
                context = with
            }
            if (context is Activity
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                    && context.isDestroyed) {
                //上下文是Activity，既然在这个页面加载图片，而页面已销毁，那就不用加载了
                return
            }
            try {
                var requestBuilder = getRequestManager(context, imageView.context)
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                if (placeHolderImgResId != 0) {
                    requestBuilder = requestBuilder.placeholder(placeHolderImgResId)
                }
                requestBuilder.into(imageView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 根据上下文类型，获取请求管理器
         */
        private fun getRequestManager(with: Any, context: Context): RequestManager {
            if (with is FragmentActivity) {
                return Glide.with(with as FragmentActivity)
            } else if (with is Activity) {
                return Glide.with(with as Activity)
            } else if (with is Fragment) {
                return Glide.with(with as Fragment)
            } else if (with is Context) {
                return Glide.with(with as Context)
            } else {
                return Glide.with(context)
            }
        }

    }
}

class LocalVideoImageSources(val videoMediaId: Long) {

}

/**
 * 模型加载器工厂
 */
class LocalVideoImageModelLoaderFactory : ModelLoaderFactory<LocalVideoImageSources, Bitmap> {

    /**
     * 构建一个模型加载器用于加载本地视频预览图
     */
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<LocalVideoImageSources, Bitmap> {
        return object : ModelLoader<LocalVideoImageSources, Bitmap> {
            override fun buildLoadData(model: LocalVideoImageSources, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap>? {
                return ModelLoader.LoadData<Bitmap>(ObjectKey(model), MyDateFetcher(model))
            }

            /**
             * 模型加载器能处理的数据类型，LocalVideoImageSources就是我们指定的用于处理本地视频预览图加载的类型，所以返回true
             * 若是处理String类型，则需要判断这是个本地文件，还是个网络地址，或者ContentProvider地址
             */
            override fun handles(model: LocalVideoImageSources): Boolean {
                return true
            }
        }
    }

    override fun teardown() {
    }

    /**
     * 数据获取
     */
    class MyDateFetcher(val model: LocalVideoImageSources) : DataFetcher<Bitmap> {

        /**
         * 返回数据类型
         */
        override fun getDataClass(): Class<Bitmap> {
            return Bitmap::class.java
        }

        /**
         * 对整个加载做结尾，如关闭流
         */
        override fun cleanup() {
        }

        /**
         * 数据来源，这里会影响Glide对本次加载结果的缓存策略
         * ，若来源是本地的，Glide可以缓存经过缩放的图片，即使加载不同尺寸，在从本地读取就是，代价低。
         * 远程图片数据获取代价高，会默认缓存原始数据
         */
        override fun getDataSource(): DataSource {
            return DataSource.LOCAL
        }

        /**
         * 取消加载任务，没办法，通过媒体库加载视频预览图可没有中断的方法
         */
        override fun cancel() {
        }

        /**
         * 加载数据
         * @param priority 本次加载任务的优先级，如果高的话，需要优先处理
         */
        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
            //通过媒体库加载图片,这里是在子线程执行
            val bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                    MyApplication.instance.contentResolver,
                    model.videoMediaId,
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    null
            )
            if (bitmap != null) {
                callback.onDataReady(bitmap)
            } else {
                callback.onLoadFailed(Exception("no video image has found"))
            }
        }

    }
}


@GlideModule
class MyAppGlideModule : AppGlideModule() {

    /**
     * 每个图片加载任务开始前会调用，允许在这里对图片加载任务做统一的设置
     */
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
    }

    /**
     * 注册组件
     */
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
//        MediaStoreVideoThumbLoader()
        registry.prepend(LocalVideoImageSources::class.java, Bitmap::class.java,LocalVideoImageModelLoaderFactory() )
    }
}