package com.csw.android.videofloatwindow.view

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.AreaUtils

/**
 * 视频悬浮窗口
 */
class VideoFloatWindow : FrameLayout {
    private var isAddToWindow = false
    private val windowManager: WindowManager
    private val areaUtils: AreaUtils

    private val videoContainer: FloatWindowVideoContainer

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        areaUtils = AreaUtils()
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = LayoutInflater.from(context).inflate(R.layout.view_video_float_window, this, false)
        super.addView(view, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        videoContainer = findViewById(R.id.videoContainer)
        setOnClickListener { hide() }
    }

    fun hide() {
        removeFromWindow()
    }

    fun show() {
        if (parent == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(context)) {
                    addToWindow()
                }
            } else {
                addToWindow()
            }
        }
    }


    fun isShowing(): Boolean {
        return parent != null
    }

    private fun addToWindow() {
        removeFromWindow()
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

        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.dimAmount = 0f

        windowManager.addView(this, params)
        isAddToWindow = true
    }

    private fun removeFromWindow() {
        val p = parent
        p?.let {
            windowManager.removeView(this)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event) || true
    }

    fun setVideoInfo(videoInfo: VideoInfo) {
        videoContainer.setVideoInfo(videoInfo).bindPlayer().play()
        areaUtils.calcVideoWH(videoInfo.whRatio) { w, h ->
            videoContainer.layoutParams.width = w
            videoContainer.layoutParams.height = h
            videoContainer.layoutParams = videoContainer.layoutParams
        }
    }

}