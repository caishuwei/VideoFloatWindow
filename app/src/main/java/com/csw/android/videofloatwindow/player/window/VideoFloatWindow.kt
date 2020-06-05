package com.csw.android.videofloatwindow.player.window

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.view.*
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent
import androidx.core.view.ViewCompat
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.container.impl.FloatWindowVideoContainer
import com.csw.android.videofloatwindow.util.LogUtils

/**
 * 视频悬浮窗口
 * <br/>
 * 嵌套滚动用于处理手指在视频上滑动时移动窗口位置
 */
class VideoFloatWindow(context: Context) : FrameLayout(context), NestedScrollingParent {

    companion object {
        val instance = VideoFloatWindow(MyApplication.instance)//这里用的是Application，不用怕内存泄露
    }

    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val windowLayoutParams: WindowManager.LayoutParams

    private val playerView: View = LayoutInflater.from(context).inflate(R.layout.view_video_float_window, this, false)
    private val videoContainer: FloatWindowVideoContainer
    private var moveView: FloatWindowMoveView

    var onFloatWindowChangeListener: OnFloatWindowChangeListener? = null

    private val windowUpdateHelper = WindowUpdateHelper()

    init {
        super.addView(playerView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        videoContainer = findViewById(R.id.videoContainer)
        videoContainer.videoFloatWindow = this
        moveView = findViewById(R.id.moveView)

        windowLayoutParams = WindowManager.LayoutParams()
        //设置窗口类型， android 8.0以上只能设置TYPE_APPLICATION_OVERLAY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            windowLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        //设置像素格式 但为什么用的是RGBA 而不是ARGB
        windowLayoutParams.format = PixelFormat.RGBA_8888
        //不允许触摸,这样触摸事件能传递到窗口之下
        //WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        //设置不取得焦点
        windowLayoutParams.flags = windowLayoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowLayoutParams.gravity = Gravity.START or Gravity.TOP
        windowLayoutParams.x = 0
        windowLayoutParams.y = 0

        windowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        windowLayoutParams.dimAmount = 0f
    }

    fun hide() {
        removeFromWindow()
    }

    fun show(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return if (Settings.canDrawOverlays(context)) {
                addToWindow()
                true
            } else {
                false
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
            windowManager.addView(this, windowLayoutParams)
            PlayHelper.setTopLevelVideoContainer(videoContainer)
            onFloatWindowChangeListener?.onFloatWindowVisibilityChanged(true)
            AvailableAreaMeasure.instance.measureAvailableArea()
        }
    }

    private fun removeFromWindow() {
        if (parent != null) {
            windowManager.removeView(this)
            PlayHelper.removeTopLevelVideoContainer(videoContainer)
            onFloatWindowChangeListener?.onFloatWindowVisibilityChanged(false)
            AvailableAreaMeasure.instance.removeWindow()
            videoContainer.release()
        }
    }

    fun setVideoInfo(videoInfo: VideoInfo) {
        videoContainer.setVideoInfo(videoInfo)
        videoContainer.play()
    }

    fun updateWindowWH(it: VideoInfo) {
        AreaUtils.calcVideoWH(it.whRatio) { w, h ->
            AreaUtils.windowWidth = w
            AreaUtils.windowHeight = h
            AreaUtils.alignToCenter()
            AreaUtils.adjustWindowOffset()

            windowLayoutParams.width = AreaUtils.windowWidth
            windowLayoutParams.height = AreaUtils.windowHeight
            windowLayoutParams.x = AreaUtils.windowOffsetX
            windowLayoutParams.y = AreaUtils.windowOffsetY
            updateFloatWindow()
        }
    }

    fun onAvailableAreaChanged() {
        videoContainer.getVideoInfo()?.let {
            updateWindowWH(it)
        }
    }

    //窗口缩放实现》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》
    /**
     * 事件拦截，若按下位置在窗口右下角，直接拦截事件作为缩放窗口操作
     */
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


    /**
     * 触摸事件处理，缩放窗口
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                }
                MotionEvent.ACTION_MOVE -> {
                    if (moveView.isInMoving()) {
                        windowUpdateHelper.updateWindowSizeByRB(
                                it.x + moveView.width / 2,
                                it.y + moveView.height / 2
                        )
                    }
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    if (moveView.isInMoving()) {
                        windowUpdateHelper.updateWindowSizeByRB(
                                it.x + moveView.width / 2,
                                it.y + moveView.height / 2
                        )
                        moveView.setInMoving(false)
                    }
                }
            }
        }
        return super.onTouchEvent(event) or true
    }


    //《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《


    //用嵌套滑动实现窗口移动》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》
    override fun getNestedScrollAxes(): Int {
        return ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        //不管哪个方向的嵌套滑动，我们都要
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        super.onNestedScrollAccepted(child, target, axes)
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
        windowUpdateHelper.updateWindowPosition(AreaUtils.windowOffsetX + dxUnconsumed, AreaUtils.windowOffsetY + dyUnconsumed)
        super.onNestedScroll(target, dxConsumed + dxUnconsumed, dyConsumed + dyUnconsumed, 0, 0)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return super.onNestedPreFling(target, velocityX, velocityY)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return super.onNestedFling(target, velocityX, velocityY, consumed)
    }
    //《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《

    private fun updateFloatWindow() {
        if (isShowing()) {
            windowManager.updateViewLayout(this, windowLayoutParams)
        }
    }

    /**
     * 这个类用于过滤缩放过程中一些不必要的缩放次数，实现窗口缩放的流畅性
     * 窗口移动倒是很流畅，不需要修改
     */
    private inner class WindowUpdateHelper {
        private val setNewRB = 1
        private val mainHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message?) {
                msg?.let {
                    when (it.what) {
                        setNewRB -> {
                            handleSetNewRB()
                        }
                    }
                }
            }
        }

        private fun handleSetNewRB() {
            PlayHelper.lastPlayVideo?.getVideoInfo()?.let {
                val ratioWH = it.whRatio
                val suggestRight = Math.min(Math.max(windowLayoutParams.x + right, 0F), AreaUtils.maxVideoWidth.toFloat())
                val suggestBottom = Math.min(Math.max(windowLayoutParams.y + bottom, 0F), AreaUtils.maxVideoHeight.toFloat())
                var destWidth = suggestRight - AreaUtils.windowOffsetX
                var destHeight = suggestBottom - AreaUtils.windowOffsetY
                //宽度调整
                if (destWidth / ratioWH < AreaUtils.minVideoHeight) {//当前宽度下，高度会小于最小高度
                    destWidth = AreaUtils.minVideoHeight * ratioWH//宽度调整到高度可以达到最小高度
                }
                if (destWidth > AreaUtils.maxVideoWidth) {
                    destWidth = AreaUtils.maxVideoWidth.toFloat()
                }
                if (destWidth < AreaUtils.minVideoWidth) {
                    destWidth = AreaUtils.minVideoWidth.toFloat()
                }
                //高度调整
                if (destHeight * ratioWH < AreaUtils.minVideoWidth) {//当前高度下，宽度会小于最小宽度
                    destHeight = AreaUtils.minVideoWidth / ratioWH//高度调整到宽度可以达到最小宽度
                }
                if (destHeight > AreaUtils.maxVideoHeight) {
                    destHeight = AreaUtils.maxVideoHeight.toFloat()
                }
                if (destHeight < AreaUtils.minVideoHeight) {
                    destHeight = AreaUtils.minVideoHeight.toFloat()
                }
                val areaFillW = destWidth * destWidth / ratioWH//按缩放比例宽度拉到手指横坐标的面积
                val areaFillH = destHeight * destHeight * ratioWH//按缩放比例高度拉到手指纵坐标的面积
                //取面积小的进行设置
                if (areaFillW > areaFillH) {
                    AreaUtils.windowWidth = (destHeight * ratioWH).toInt()
                    AreaUtils.windowHeight = destHeight.toInt()
                } else {
                    AreaUtils.windowWidth = destWidth.toInt()
                    AreaUtils.windowHeight = (destWidth / ratioWH).toInt()
                }
                AreaUtils.suggestArea = AreaUtils.windowWidth * AreaUtils.windowHeight
                AreaUtils.adjustWindowOffset()
                AreaUtils.calcWindowCenter()
                //更新playerView位置
                windowLayoutParams.width = AreaUtils.windowWidth
                windowLayoutParams.height = AreaUtils.windowHeight
                windowLayoutParams.x = AreaUtils.windowOffsetX
                windowLayoutParams.y = AreaUtils.windowOffsetY
                updateFloatWindow()
                LogUtils.i(javaClass.name, "updateWindowSizeByRB ${AreaUtils.windowWidth}x${AreaUtils.windowHeight}")
            }
        }

        private var right: Float = 0f
        private var bottom: Float = 0f

        /**
         * 根据右下角手指触摸位置更新窗口尺寸
         * 设置新的窗口尺寸，由于windowManager更新视图大小速度有限，导致窗口缩放非常卡顿，这里通过主线程消息循环，减少修改次数
         * 过于频繁的更新只会变得更卡，这里限制最多30毫秒更新一次
         */
        fun updateWindowSizeByRB(r: Float, b: Float) {
            right = r
            bottom = b
            if (!mainHandler.hasMessages(setNewRB)) {
                mainHandler.sendEmptyMessageDelayed(setNewRB, 30)
            }
        }

        /**
         * 更新窗口位置
         */
        fun updateWindowPosition(left: Int, top: Int) {
            AreaUtils.windowOffsetX = left
            AreaUtils.windowOffsetY = top
            AreaUtils.adjustWindowOffset()
            AreaUtils.calcWindowCenter()
            LogUtils.i(javaClass.name, "updateWindowPosition ${AreaUtils.windowOffsetX}x${AreaUtils.windowOffsetY}")
            windowLayoutParams.width = AreaUtils.windowWidth
            windowLayoutParams.height = AreaUtils.windowHeight
            windowLayoutParams.x = AreaUtils.windowOffsetX
            windowLayoutParams.y = AreaUtils.windowOffsetY
            updateFloatWindow()
        }

    }
    //《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《
}