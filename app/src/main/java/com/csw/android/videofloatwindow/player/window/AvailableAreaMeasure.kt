package com.csw.android.videofloatwindow.player.window

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.util.LogUtils

/**
 * 这个类用于测量悬浮窗可用面积，View填充所有可用空间，当布局发生变化时，根据视图宽高即可得知悬浮窗可用区域
 */
class AvailableAreaMeasure(context: Context) : View(context) {

    companion object {
        val instance = AvailableAreaMeasure(MyApplication.instance)//这里用的是Application，不用怕内存泄露
    }

    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val windowLayoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

    init {
        LogUtils.i(AvailableAreaMeasure::class.java.name,"init ${context::class.java.name}")
        //visibility = INVISIBLE//只占空间不显示，但居然不走测量布局方法？那怎么知道空间占用多大？
        //设置窗口类型， android 8.0以上只能设置TYPE_APPLICATION_OVERLAY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            windowLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        windowLayoutParams.format = PixelFormat.RGBA_8888
        //设置窗口不取得焦点 不拦截触摸事件
        windowLayoutParams.flags = windowLayoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        windowLayoutParams.gravity = Gravity.START or Gravity.TOP
        windowLayoutParams.x = 0
        windowLayoutParams.y = 0
        windowLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        windowLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        windowLayoutParams.dimAmount = 0f
        layoutParams = windowLayoutParams
    }

    /**
     * 测量可用区域
     */
    fun measureAvailableArea() {
        if (ensureAddWindow()) {
            post {
                updateAreaMaxWH()
            }
        }
    }

    private fun updateAreaMaxWH() {
        if (width == 0 && height == 0) {
            return
        }
        if (AreaUtils.maxVideoWidth != width
                || AreaUtils.maxVideoHeight != height) {
            AreaUtils.maxVideoWidth = width
            AreaUtils.maxVideoHeight = height
            AreaUtils.noticeFloatWindowUpdate()
            LogUtils.i(AvailableAreaMeasure::class.java.name, "updateAreaMaxWH ($width，$height)")
        }
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        LogUtils.i(AvailableAreaMeasure::class.java.name, "layout)")
        super.layout(l, t, r, b)
        updateAreaMaxWH()
    }

    /**
     * 确保悬浮窗添加好
     */
    private fun ensureAddWindow(): Boolean {
        if (parent == null) {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
                //允许添加悬浮窗
                windowManager.addView(this, layoutParams)
                true
            } else {
                false
            }
        } else {
            return true
        }
    }

    fun removeWindow() {
        if (parent != null) {
            windowManager.removeView(this)
        }
    }

}