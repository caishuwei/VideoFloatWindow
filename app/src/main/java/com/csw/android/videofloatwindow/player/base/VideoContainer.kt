package com.csw.android.videofloatwindow.player.base

import android.content.Context
import android.content.pm.ActivityInfo
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.widget.FrameLayout
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.permission.SystemAlertWindowPermission
import com.csw.android.videofloatwindow.player.PlayerHelper
import com.csw.android.videofloatwindow.ui.FullScreenActivity
import com.csw.android.videofloatwindow.util.Utils

open class VideoContainer : FrameLayout {
    private val videoContainer: FrameLayout;
    var whRatio: Float = 16f / 9
        set(value) {
            var ratio = value
            if (ratio <= 0) {
                ratio = 16f / 9
            }
            if (whRatio != ratio) {
                field = ratio
                requestLayout()
            }
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        videoContainer = FrameLayout(context)
        videoContainer.tag = this
        super.addView(videoContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        super.setBackgroundColor(0xFF000000.toInt())
    }

    var videoInfo: VideoInfo? = null
        private set


    /**
     * 绑定播放视图
     */
    open fun bindPlayer(): VideoContainer {
        MyApplication.instance.playerHelper.bindPlayer(videoContainer) {
            onBindPlayer(it)
        }
        return this
    }

    /**
     * 解除播放视图绑定
     */
    open fun unBindPlayer(): VideoContainer {
        MyApplication.instance.playerHelper.unBindPlayer(videoContainer)
        return this
    }


    /**
     * 绑定播放视图后的操作，如设播放控制器的各种按钮监听
     */
    open fun onBindPlayer(playerBindHelper: PlayerHelper.PlayerBindHelper) {

    }

    /**
     * 播放器绑定解除
     */
    open fun onUnBindPlayer(playBindHelper: PlayerHelper.PlayerBindHelper) {

    }


    /**
     * 播放当前的视频
     */
    open fun play(): VideoContainer {
        videoInfo?.let {
            MyApplication.instance.playerHelper.play(it)
        }
        return this
    }

    /**
     * 停止视频播放
     */
    open fun stop(): VideoContainer {
        videoInfo?.let {
            MyApplication.instance.playerHelper.stop(it)
        }
        return this
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Utils.measureCenterInsideByScaleRatio(widthMeasureSpec, heightMeasureSpec, whRatio) { w, h ->
            super.onMeasure(w, h)
        }
    }

    /**
     * 设置播放信息
     */
    open fun setVideoInfo(videoInfo: VideoInfo?): VideoContainer {
//        if (this.videoInfo != videoInfo) {
//            unBindPlayer();
//        }
        this.videoInfo = videoInfo
        tryRotateScreen()
        return this
    }

    /**
     * 根据视频的比例，在全屏播放的情况下，长的视频设置为竖屏播放，宽的视频设置为横屏播放
     */
    private fun tryRotateScreen() {
        Utils.runIfNotNull(context, videoInfo) { c, v ->
            if (c is FullScreenActivity) {
                c.requestedOrientation = if (v.whRatio > 1) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    fun playInFullScreen() {
        videoInfo?.let {
            FullScreenActivity.openActivity(context, it)
        }
    }

    fun tryPlayInWindow() {
        val activity = context
        if (activity is FragmentActivity) {
            SystemAlertWindowPermission.request(activity, object : SystemAlertWindowPermission.OnRequestResultListener {
                override fun onResult(isGranted: Boolean) {
                    if (isGranted) {
                        playInWindow()
                    } else {
                        Snackbar.make(this@VideoContainer, "没有悬浮窗权限", Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    open fun playInWindow() {
        videoInfo?.let {
            MyApplication.instance.playerHelper.playInFloatWindow(it)
        }
    }

    fun isBindPlayer(): Boolean {
        return videoContainer.childCount > 0
    }


}