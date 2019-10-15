package com.csw.android.videofloatwindow.player.window

import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent
import androidx.core.view.ViewCompat
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
        val instance = VideoFloatWindow(MyApplication.instance)//这里用的是Application，不用怕内存泄露
    }

    private val windowManager: WindowManager
    private val windowLayoutParams: WindowManager.LayoutParams

    private val playerView: View
    private val playerViewLayoutParams: FrameLayout.LayoutParams
    private val videoContainer: FloatWindowVideoContainer
    private var moveView: FloatWindowMoveView
    private val dp24 = ScreenInfo.dp2Px(24f)

    var onFloatWindowChangeListener: OnFloatWindowChangeListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
//        setBackgroundColor(Color.BLACK)
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        playerView = LayoutInflater.from(context).inflate(R.layout.view_video_float_window, this, false)
        playerViewLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        super.addView(playerView, playerViewLayoutParams)
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
        //设置不取得焦点
        windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowLayoutParams.gravity = Gravity.LEFT or Gravity.TOP
        windowLayoutParams.x = 0
        windowLayoutParams.y = 0

        windowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        windowLayoutParams.dimAmount = 0f
        layoutParams = windowLayoutParams
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
            PlayHelper.setTopLevelVideoContainer(videoContainer)
            onFloatWindowChangeListener?.onFloatWindowVisibilityChanged(true)
        }
    }

    private fun removeFromWindow() {
        if (parent != null) {
            windowManager.removeView(this)
            videoContainer.releaseVideoView()
            PlayHelper.removeTopLevelVideoContainer(videoContainer)
            onFloatWindowChangeListener?.onFloatWindowVisibilityChanged(false)
        }
    }

    fun setVideoInfo(videoInfo: VideoInfo) {
        videoContainer.setVideoInfo(videoInfo, true)
        videoContainer.play()
    }

    fun updateWindowWH(it: VideoInfo) {
        AreaUtils.calcVideoWH(it.whRatio) { w, h ->
            AreaUtils.windowWidth = w
            AreaUtils.windowHeight = h
            AreaUtils.alignToCenter()
            AreaUtils.adjustWindowOffset()

            playerViewLayoutParams.width = AreaUtils.windowWidth
            playerViewLayoutParams.height = AreaUtils.windowHeight
            playerView.layoutParams = playerViewLayoutParams

            if (!moveOrScale) {
                windowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
                windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                windowLayoutParams.x = AreaUtils.windowOffsetX
                windowLayoutParams.y = AreaUtils.windowOffsetY
                updateFloatWindow()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
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
                        setWindowState(true)
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
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                }
                MotionEvent.ACTION_MOVE -> {
                    if (moveView.isInMoving()) {
                        updateWindowSizeByRB(it.x + moveView.width / 2, it.y + moveView.height / 2)
                    }
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    if (moveView.isInMoving()) {
                        updateWindowSizeByRB(it.x + moveView.width / 2, it.y + moveView.height / 2)
                        setWindowState(false)
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
        LogUtils.i(javaClass.name, "onStartNestedScroll nestedScrollAxes = ${nestedScrollAxes}")
        setWindowState(true)
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        super.onNestedScrollAccepted(child, target, axes)
        //绑定滚动的轴
        LogUtils.i(javaClass.name, "onNestedScrollAccepted axes = ${axes}")
    }

    override fun onStopNestedScroll(child: View) {
        super.onStopNestedScroll(child)
        setWindowState(false)
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
        LogUtils.i(javaClass.name, "onNestedScroll dxUnconsumed = ${dxUnconsumed},dyUnconsumed = ${dyUnconsumed}")
        updateWindowPosition(AreaUtils.windowOffsetX + dxUnconsumed, AreaUtils.windowOffsetY + dyUnconsumed)
        super.onNestedScroll(target, dxConsumed + dxUnconsumed, dyConsumed + dyUnconsumed, 0, 0)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return super.onNestedPreFling(target, velocityX, velocityY)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return super.onNestedFling(target, velocityX, velocityY, consumed)
    }
    //《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《

    //悬浮窗状态切换，在进入移动或缩放状态时，切换为全屏状态(悬浮窗区域会拦截触摸事件，不管该位置是
    // 否是透明的)，进行移动缩放操作》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》
    private var moveOrScale: Boolean = false

    /**
     * 设置窗口状态
     * @param moveOrScale 移动或者缩放状态
     */
    private fun setWindowState(moveOrScale: Boolean) {
        this.moveOrScale = moveOrScale
        if (moveOrScale) {
//            setBackgroundColor(Color.parseColor("#80000000"))
            //窗口视图填满可用空间
            windowLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            windowLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
//            AreaUtils.windowOffsetX = windowLayoutParams.x
//            AreaUtils.windowOffsetY = windowLayoutParams.y
            windowLayoutParams.x = 0
            windowLayoutParams.y = 0
            updateFloatWindow()

            //播放视图根据当前窗口位置进行属性设置
            playerViewLayoutParams.width = AreaUtils.windowWidth
            playerViewLayoutParams.height = AreaUtils.windowHeight
            playerViewLayoutParams.leftMargin = AreaUtils.windowOffsetX
            playerViewLayoutParams.topMargin = AreaUtils.windowOffsetY
            playerView.layoutParams = playerViewLayoutParams

            adjustAvailableArea()
        } else {
//            setBackgroundColor(Color.parseColor("#00000000"))
            playerViewLayoutParams.width = AreaUtils.windowWidth
            playerViewLayoutParams.height = AreaUtils.windowHeight
            playerViewLayoutParams.leftMargin = 0
            playerViewLayoutParams.topMargin = 0
            playerView.layoutParams = playerViewLayoutParams

            windowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            windowLayoutParams.x = AreaUtils.windowOffsetX
            windowLayoutParams.y = AreaUtils.windowOffsetY
            updateFloatWindow()
        }
    }

    /**
     * 根据右下角手指触摸位置更新窗口尺寸
     */
    private fun updateWindowSizeByRB(right: Float, bottom: Float) {
        PlayHelper.lastPlayVideo?.getVideoInfo()?.let {
            val ratioWH = it.whRatio
            var suggestRight = Math.min(Math.max(right, 0F), AreaUtils.maxVideoWidth.toFloat())
            var suggestBottom = Math.min(Math.max(bottom, 0F), AreaUtils.maxVideoHeight.toFloat())
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
            playerViewLayoutParams.width = AreaUtils.windowWidth
            playerViewLayoutParams.height = AreaUtils.windowHeight
            playerViewLayoutParams.leftMargin = AreaUtils.windowOffsetX
            playerViewLayoutParams.topMargin = AreaUtils.windowOffsetY
            playerView.layoutParams = playerViewLayoutParams
            LogUtils.i(javaClass.name, "updateWindowSizeByRB ${AreaUtils.windowWidth}x${AreaUtils.windowHeight}")
        }
    }

    /**
     * 更新窗口位置
     */
    private fun updateWindowPosition(left: Int, top: Int) {
        AreaUtils.windowOffsetX = left
        AreaUtils.windowOffsetY = top
        AreaUtils.adjustWindowOffset()
        AreaUtils.calcWindowCenter()
        LogUtils.i(javaClass.name, "updateWindowPosition ${AreaUtils.windowOffsetX}x${AreaUtils.windowOffsetY}")
        playerViewLayoutParams.width = AreaUtils.windowWidth
        playerViewLayoutParams.height = AreaUtils.windowHeight
        playerViewLayoutParams.leftMargin = AreaUtils.windowOffsetX
        playerViewLayoutParams.topMargin = AreaUtils.windowOffsetY
        playerView.layoutParams = playerViewLayoutParams
    }

    private fun updateFloatWindow() {
        if (isShowing()) {
            windowManager.updateViewLayout(this, windowLayoutParams)
        }
    }

    /**
     * 重新调整可用区域
     */
    private fun adjustAvailableArea() {
        post {
            LogUtils.i(javaClass.name, "adjustAvailableArea ${width}x${height}")
            AreaUtils.maxVideoWidth = width
            AreaUtils.maxVideoHeight = height
        }
    }
    //《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《《
}