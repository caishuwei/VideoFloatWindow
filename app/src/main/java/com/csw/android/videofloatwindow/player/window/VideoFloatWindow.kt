package com.csw.android.videofloatwindow.player.window

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.support.v4.view.NestedScrollingParent
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.base.AreaUtils
import com.csw.android.videofloatwindow.util.LogUtils
import com.csw.android.videofloatwindow.util.ScreenInfo

/**
 * 视频悬浮窗口
 * <br/>
 * 嵌套滚动用于处理手指在视频上滑动时移动窗口位置
 */
class VideoFloatWindow : FrameLayout, NestedScrollingParent {
    companion object {
        val instance = VideoFloatWindow(MyApplication.instance)
    }

    private val windowManager: WindowManager
    private val areaUtils: AreaUtils

    private val videoContainer: FloatWindowVideoContainer
    private var moveView: FloatWindowMoveView
    private val dp24 = ScreenInfo.dp2Px(24f)

    var onFloatWindowChangeListener: OnFloatWindowChangeListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        areaUtils = AreaUtils()
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = LayoutInflater.from(context).inflate(R.layout.view_video_float_window, this, false)
        super.addView(view, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        videoContainer = findViewById(R.id.videoContainer)
        videoContainer.videoFloatWindow = this
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                }
                MotionEvent.ACTION_MOVE -> {
                    if (moveView.isInMoving()) {
                        updateWindowSizeByRB(it.rawX, it.rawY)
                    }
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    if (moveView.isInMoving()) {
                        updateWindowSizeByRB(it.rawX, it.rawY)
                        moveView.setInMoving(false)
                    }
                }
            }
        }
        return super.onTouchEvent(event) or true
    }

    private fun updateWindowSizeByRB(r: Float, b: Float) {
        val params = this.layoutParams as WindowManager.LayoutParams
        if (params.width > 0 && params.height > 0) {
            PlayHelper.lastPlayVideo?.getVideoInfo()?.let {
                val ratioWH = it.whRatio
                val areaFillW = (r - params.x) * (r - params.x) / ratioWH
                val areaFillH = (b - params.y) * (b - params.y) * ratioWH
                if (areaFillW > areaFillH) {
                    params.width = ((b - params.y) * ratioWH).toInt()
                    params.height = (b - params.y).toInt()
                } else {
                    params.width = (r - params.x).toInt()
                    params.height = ((r - params.x) / ratioWH).toInt()
                }
                areaUtils.suggestArea = params.width * params.height
                anchorPosX = params.x + params.width / 2
                anchorPosY = params.y + params.height / 2
                updateWindowPosition(anchorPosX, anchorPosY)
            }
        }
    }

    fun hide() {
        removeFromWindow()
    }

    fun show(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(context)) {
                addToWindow()
                return true
            } else {
                return false
            }
        } else {
            addToWindow()
            return true
        }
    }


    fun isShowing(): Boolean {
        return parent != null
    }

    private fun addToWindow() {
        if (parent == null) {
            windowManager.addView(this, layoutParams)
            PlayHelper.onVideoContainerEnterForeground(videoContainer)
            onFloatWindowChangeListener?.onFloatWindowVisibilityChanged(true)
        }
    }

    private fun removeFromWindow() {
        if (parent != null) {
            windowManager.removeView(this)
            videoContainer.releaseVideoView()
            PlayHelper.onVideoContainerExitForeground(videoContainer)
            onFloatWindowChangeListener?.onFloatWindowVisibilityChanged(false)
        }
    }

    fun setVideoInfo(videoInfo: VideoInfo) {
        videoContainer.setVideoInfo(videoInfo, true)
        videoContainer.play()
    }

    fun updateWindowWH(it: VideoInfo) {
        areaUtils.calcVideoWH(it.whRatio) { w, h ->
            videoContainer.layoutParams.width = w
            videoContainer.layoutParams.height = h
            videoContainer.layoutParams = videoContainer.layoutParams

            this.layoutParams.width = w
            this.layoutParams.height = h
            updateWindowPosition(anchorPosX, anchorPosY)
        }
    }


    private var anchorPosX = 0
    private var anchorPosY = 0
    private fun updateWindowPosition(x: Int, y: Int) {
        anchorPosX = Math.min(Math.max(x, 0), ScreenInfo.WIDTH)
        anchorPosY = Math.min(Math.max(y, 0), ScreenInfo.HEIGHT)
        val params = this.layoutParams as WindowManager.LayoutParams
        params.x = anchorPosX - params.width / 2
        params.y = anchorPosY - params.height / 2
        if (isShowing()) {
            windowManager.updateViewLayout(this, params)
        }
    }

    //----------------------------------- 处理嵌套滚动的情况 -----------------------------------------

    override fun getNestedScrollAxes(): Int {
        return ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        //不管哪个方向的嵌套滑动，我们都要
        LogUtils.e("onStartNestedScroll", "nestedScrollAxes = ${nestedScrollAxes}")
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        super.onNestedScrollAccepted(child, target, axes)
        //绑定滚动的轴
        LogUtils.e("onNestedScrollAccepted", "axes = ${axes}")
    }

    override fun onStopNestedScroll(child: View) {
        super.onStopNestedScroll(child)

    }

    /**
     * 优先于子视图先消耗滚动量，这里不需要，父方法继续向上分发
     */
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        super.onNestedPreScroll(target, dx, dy, consumed)
    }

    /**
     * 执行滚动
     */
    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        //将剩下的滚动量消耗掉
        LogUtils.e("onNestedScroll", "dxUnconsumed = ${dxUnconsumed},dyUnconsumed = ${dyUnconsumed}")
        updateWindowPosition(anchorPosX + dxUnconsumed, anchorPosY + dyUnconsumed)
        super.onNestedScroll(target, dxConsumed + dxUnconsumed, dyConsumed + dyUnconsumed, 0, 0)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return super.onNestedPreFling(target, velocityX, velocityY)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return super.onNestedFling(target, velocityX, velocityY, consumed)
    }

}