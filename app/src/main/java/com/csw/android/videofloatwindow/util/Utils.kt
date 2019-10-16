package com.csw.android.videofloatwindow.util

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.View
import androidx.annotation.DrawableRes
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo

class Utils {
    companion object {
        fun getVideoInfo(contentResolver: ContentResolver, uri: Uri): VideoInfo? {
            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(uri, null, null, null, null)
                if (cursor.moveToFirst()) {
                    return VideoInfo.readFromCursor(cursor)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        /**
         * 判断两个可空变量都不为空之后调用run
         */
        fun <T, K> runIfNotNull(arg1: T?, arg2: K?, run: (arg1: T, arg2: K) -> (Unit)) {
            if (arg1 != null && arg2 != null) {
                run(arg1, arg2)
            }
        }

        /**
         * 根据测量信息以及缩放比例，确定最终View大小，回调返回测量值
         *
         * @param widthMeasureSpec View.nMeasure接收到的宽度测量值
         * @param heightMeasureSpec View.nMeasure接收到的高度测量值
         * @param wHRatio View想要的宽高比例（width/height）
         * @param onMeasureCalcCompleted 回调函数(widthMeasureSpec 最终的宽度测量值，heightMeasureSpec 最终的高度测量值)
         */
        fun measureCenterInsideByScaleRatio(
                widthMeasureSpec: Int,
                heightMeasureSpec: Int,
                wHRatio: Float,
                onMeasureCalcCompleted: (widthMeasureSpec: Int, heightMeasureSpec: Int) -> (Unit)) {
            var wSize = View.MeasureSpec.getSize(widthMeasureSpec)
            val wMode = View.MeasureSpec.getMode(widthMeasureSpec)
            var hSize = View.MeasureSpec.getSize(heightMeasureSpec)
            val hMode = View.MeasureSpec.getMode(heightMeasureSpec)
            if (hMode == View.MeasureSpec.UNSPECIFIED) {
                hSize = ScreenInfo.HEIGHT
            }
            if (wMode == View.MeasureSpec.UNSPECIFIED) {
                wSize = ScreenInfo.WIDTH
            }
            if (wMode == View.MeasureSpec.EXACTLY && hMode != View.MeasureSpec.EXACTLY) {
                //宽度精确，高度不精确
                hSize = Math.min((wSize / wHRatio).toInt(), hSize)
            } else if (wMode != View.MeasureSpec.EXACTLY && hMode == View.MeasureSpec.EXACTLY) {
                //宽度不精确，高度精确
                wSize = Math.min((hSize * wHRatio).toInt(), wSize)
            } else if (wMode != View.MeasureSpec.EXACTLY && hMode != View.MeasureSpec.EXACTLY) {
                //两个都不精确，根据可用空间的大小来决定
                if ((wSize * 1f / hSize) > wHRatio) {//可用空间宽度足
                    wSize = (hSize * wHRatio).toInt()
                } else {
                    hSize = (wSize / wHRatio).toInt()
                }
            }
            onMeasureCalcCompleted(
                    View.MeasureSpec.makeMeasureSpec(wSize, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(hSize, View.MeasureSpec.EXACTLY)
            )
        }

        fun videoEquals(videoInfo1: VideoInfo?, videoInfo2: VideoInfo?): Boolean {
            if (videoInfo1 != null && videoInfo2 != null) {
                return videoInfo1.target.equals(videoInfo2.target)
            }
            return false
        }

        /**
         * 获取图片，并设置最大宽高，若图片超出则进行缩小，这是为了toolbar返回按钮弄的
         */
        fun getDrawableBySize(@DrawableRes drawableId: Int, maxWidth: Int, maxHeight: Int): BitmapDrawable {
            val bitmap = BitmapFactory.decodeResource(MyApplication.instance.resources, drawableId)
            val scaleX = bitmap.width / maxWidth
            val scaleY = bitmap.height / maxHeight
            var scale = 1
            if (scaleX > 1) {
                scale = scaleX
            }
            if (scaleY > 1 && scaleY > scaleX) {
                scale = scaleY
            }
            if (scale != 1) {
                val scaleBitmap = Bitmap.createBitmap(bitmap.width / scale, bitmap.height / scale, Bitmap.Config.ARGB_8888)
                val scaleCanvas = Canvas(scaleBitmap)
                val scaleMatrix = Matrix()
                scaleMatrix.postScale(1f / scale, 1f / scale)
                scaleCanvas.drawBitmap(bitmap, scaleMatrix, null)
                return BitmapDrawable(MyApplication.instance.resources, scaleBitmap)
            } else {
                return BitmapDrawable(MyApplication.instance.resources, bitmap)
            }
        }
    }

}

