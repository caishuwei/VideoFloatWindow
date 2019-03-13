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
import com.csw.android.videofloatwindow.player.video.CustomVideoView
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
        set(value) {
            if (!Utils.videoEquals(field, value)) {
                field = value
                onSetNewVideoInfo()
            } else {
                field = value
            }
        }

    /**
     * 设置新的视频信息
     */
    open fun onSetNewVideoInfo() {
        tryRotateScreen()
        videoInfo?.let { vi ->
            whRatio = vi.whRatio
            currVideoView?.let { vv ->
                vv.videoInfo = vi
                settingPlayController(vv.controllerSettingHelper)
            }
            return
        }
        //videoInfo为空，解除VideoView
        currVideoView?.unbindVideoContainer(this)
    }

    var currVideoView: CustomVideoView? = null

    /**
     * 绑定VideoView
     */
    open fun onBindVideoView(videoView: CustomVideoView) {
        this.currVideoView = videoView
        videoContainer.addView(videoView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        settingPlayController(videoView.controllerSettingHelper)
    }

    /**
     * 绑定播放视图后的操作，如设播放控制器的各种按钮监听
     */
    open fun settingPlayController(controllerSettingHelper: CustomVideoView.ControllerSettingHelper) {

    }

    /**
     * 解除VideoView绑定
     */
    open fun onUnbindVideoView(videoView: CustomVideoView) {
        videoContainer.removeView(videoView)
        this.currVideoView = null
    }

    /**
     * 释放播放器
     */
    fun releaseVideoView() {
        currVideoView?.let {
            it.unbindVideoContainer(this)
            if (currVideoView == null) {
                //成功解除VideoView
                it.release()
            }
        }
    }

    private fun getVideoView(videoInfo: VideoInfo): CustomVideoView {
        val videoView = currVideoView
        return if (videoView == null) {
            val vv = CustomVideoView.getVideoView(videoInfo)
            vv.bindVideoContainer(this)
            vv
        } else {
            videoView
        }
    }

    /**
     * 播放当前的视频
     */
    open fun play(): VideoContainer {
        videoInfo?.let {
            getVideoView(it).play()
        }
        return this
    }


    /**
     * 停止视频播放
     */
    open fun pause(): VideoContainer {
        videoInfo?.let {
            getVideoView(it).pause()
        }
        return this
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Utils.measureCenterInsideByScaleRatio(widthMeasureSpec, heightMeasureSpec, whRatio) { w, h ->
            super.onMeasure(w, h)
        }
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

}