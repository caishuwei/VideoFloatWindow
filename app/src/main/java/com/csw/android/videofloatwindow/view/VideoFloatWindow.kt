package com.csw.android.videofloatwindow.view

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.csw.android.videofloatwindow.R

/**
 * 视频悬浮窗口
 */
class VideoFloatWindow : FrameLayout {
    private var isAddToWindow = false
    private val windowManager: WindowManager

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = LayoutInflater.from(context).inflate(R.layout.view_video_float_window, this, false)
        addView(view, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        setOnClickListener {
            hide()
        }
    }

    fun hide() {
        isAddToWindow = false
        removeFromWindow()
    }

    private fun removeFromWindow() {
        windowManager.removeView(this)
    }

    fun show() {
        if (!isAddToWindow) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(context)) {
                    addToWindow()
                }
            } else {
                addToWindow()
            }
        }
    }

    private fun addToWindow() {
        val params = WindowManager.LayoutParams()
        //设置窗口类型， android 8.0以上只能设置TYPE_APPLICATION_OVERLAY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        //设置像素格式 但为什么用的是RGBA 而不是ARGB
        params.format = PixelFormat.RGBA_8888
        //设置不取得焦点
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.gravity = Gravity.LEFT or Gravity.TOP
        params.x = 0
        params.y = 0

        params.width = 1080
        params.height = 1080
        params.dimAmount = 0f  //去除对话框的半透明背景

        windowManager.addView(this, params)
        isAddToWindow = true
    }

}