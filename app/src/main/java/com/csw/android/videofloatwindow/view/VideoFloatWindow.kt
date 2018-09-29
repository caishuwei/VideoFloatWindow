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
import com.csw.android.videofloatwindow.util.ScreenInfo

/**
 * 视频悬浮窗口
 */
class VideoFloatWindow : FrameLayout {
    private var isAddToWindow = false
    private val windowManager: WindowManager
    private val areaUtils: AreaUtils

    private val videoContainer: FloatWindowVideoContainer
    private var moveView: FloatWindowMoveView
    private val dp24 = ScreenInfo.dp2Px(24f)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        areaUtils = AreaUtils()
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = LayoutInflater.from(context).inflate(R.layout.view_video_float_window, this, false)
        super.addView(view, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        videoContainer = findViewById(R.id.videoContainer)
        moveView = findViewById(R.id.moveView)

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
        layoutParams = params
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    //当点在moveView上，直接就拦截触摸事件，自己处理
                    if (moveView.isDownOnThisView(it.x, it.y)) {
                        moveView.setInMoving(true)
                        return true
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private var startX = 0
    private var startY = 0
    private var downX = 0f
    private var downY = 0f


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    val params = this.layoutParams as WindowManager.LayoutParams
                    startX = params.x
                    startY = params.y
                    downX = it.rawX
                    downY = it.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    if (moveView.isInMoving()) {
                        moveTo(startX + it.rawX - downX, startY + it.rawY - downY)
                    }
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    if (moveView.isInMoving()) {
                        moveView.setInMoving(false)
                    }
                }
            }
        }
        return super.onTouchEvent(event) or true
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
        windowManager.addView(this, layoutParams)
        isAddToWindow = true
    }

    private fun removeFromWindow() {
        val p = parent
        p?.let {
            windowManager.removeView(this)
        }
    }

    fun moveBy(x: Float, y: Float) {
        val params = this.layoutParams as WindowManager.LayoutParams
        params.x = (params.x + x).toInt()
        params.y = (params.y + y).toInt()
        windowManager.updateViewLayout(this, params)
    }

    fun setVideoInfo(videoInfo: VideoInfo) {
        videoContainer.setVideoInfo(videoInfo).bindPlayer().play()
        areaUtils.calcVideoWH(videoInfo.whRatio) { w, h ->
            videoContainer.layoutParams.width = w
            videoContainer.layoutParams.height = h
            videoContainer.layoutParams = videoContainer.layoutParams
        }
    }

    fun moveTo(x: Float, y: Float) {
        val params = this.layoutParams as WindowManager.LayoutParams
        params.x = x.toInt()
        params.y = y.toInt()
        windowManager.updateViewLayout(this, params)
    }

    interface FloatWindowController {

        fun enterEditMode()

        fun moveVideoView(x: Float, y: Float)

        fun exitEditMode()

    }

}