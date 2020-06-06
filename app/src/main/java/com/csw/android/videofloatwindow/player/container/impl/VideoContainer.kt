package com.csw.android.videofloatwindow.player.container.impl

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.permission.SystemAlertWindowPermission
import com.csw.android.videofloatwindow.player.container.IVideoContainer
import com.csw.android.videofloatwindow.player.core.VideoBindHelper
import com.csw.android.videofloatwindow.player.video.IControllerSettingHelper
import com.csw.android.videofloatwindow.player.video.IVideo
import com.csw.android.videofloatwindow.player.window.VideoFloatWindow
import com.csw.android.videofloatwindow.ui.video.full_screen.FullScreenActivity
import com.csw.android.videofloatwindow.util.Utils
import com.google.android.material.snackbar.Snackbar

/**
 * 视频容器，实现VideoView与UI上视图的绑定与解绑，使得VideoView可以随意脱离这个界面添加到另一个界面
 */
open class VideoContainer : FrameLayout, IVideoContainer {
    private val videoContainer: FrameLayout
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
        super.addView(videoContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        super.setBackgroundColor(0xFF000000.toInt())
    }

    private var mVideoInfo: VideoInfo? = null

    override fun setVideoInfo(videoInfo: VideoInfo) {
        if (!Utils.videoEquals(mVideoInfo, videoInfo)) {
            val old = mVideoInfo
            mVideoInfo = videoInfo
            onVideoInfoChanged(old, videoInfo)
        }
    }

    /**
     * 视频信息改变
     */
    open fun onVideoInfoChanged(old: VideoInfo?, new: VideoInfo) {
        releaseCurrVideo()
    }

    fun releaseCurrVideo() {
        currVideo?.let {
            unBindVideoView()
            it.release()
        }
    }

    fun syncVideoInfoToCurrVideo() {
        currVideo?.let {
            VideoBindHelper.syncVideoInfoToVideo(mVideoInfo, it)
            onPlayControllerSetup(it.getControllerSettingHelper())
        }
    }

    override fun getVideoInfo(): VideoInfo? {
        return mVideoInfo
    }

    override fun bindVideoView() {
        VideoBindHelper.bindVideo(this)
    }

    override fun unBindVideoView() {
        VideoBindHelper.unBindVideo(this)
    }

    private var currVideo: IVideo? = null
    override fun getVideo(): IVideo? {
        return currVideo
    }

    override fun onVideoBind(video: IVideo) {
        this.currVideo = video
        videoContainer.addView(video.getView(), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    override fun onPlayControllerSetup(controllerSettingHelper: IControllerSettingHelper) {

    }

    override fun onVideoUnbind(video: IVideo) {
        videoContainer.removeView(video.getView())
        this.currVideo = null
    }

    override fun play() {
        if (currVideo == null) {
            bindVideoView()
        }
        currVideo?.play()
    }

    override fun pause() {
        currVideo?.pause()
    }

    override fun release() {
        currVideo?.let {
            unBindVideoView()
            it.release()
        }
    }

    private var mLifecycleObserver = LifecycleEventObserver { source, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
            }
            Lifecycle.Event.ON_START -> {
            }
            Lifecycle.Event.ON_RESUME -> {
                if (bindVideoOnViewEnterForeground) {
                    bindVideoView()
                }
            }
            Lifecycle.Event.ON_PAUSE -> {
                if (pauseOnViewExitForeground) {
                    pause()
                }
            }
            Lifecycle.Event.ON_STOP -> {
            }
            Lifecycle.Event.ON_DESTROY -> {
                if (releaseOnViewDestroy) {
                    release()
                }
            }
            else -> {
            }
        }
    }
    var bindVideoOnViewEnterForeground = false
        private set
    var pauseOnViewExitForeground = false
        private set
    var releaseOnViewDestroy = false
        private set

    override fun setBindVideoOnViewEnterForeground(bindVideo: Boolean) {
        bindVideoOnViewEnterForeground = bindVideo
    }

    override fun setPauseOnViewExitForeground(pause: Boolean) {
        pauseOnViewExitForeground = pause
    }

    override fun setReleaseOnViewDestroy(release: Boolean) {
        releaseOnViewDestroy = release
    }

    override fun registerViewLifeCycleObserver(viewLifeCycle: Lifecycle, register: Boolean) {
        if (register) {
            viewLifeCycle.addObserver(mLifecycleObserver)
        } else {
            viewLifeCycle.removeObserver(mLifecycleObserver)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Utils.measureCenterInsideByScaleRatio(widthMeasureSpec, heightMeasureSpec, whRatio) { w, h ->
            super.onMeasure(w, h)
        }
    }

    /**
     * 根据视频的比例，在全屏播放的情况下，长的视频设置为竖屏播放，宽的视频设置为横屏播放
     */
    fun tryRotateScreen() {
        Utils.runIfNotNull(context, mVideoInfo) { c, v ->
            if (c is Activity) {
                c.requestedOrientation = if (v.whRatio > 1) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    /**
     * 全屏播放
     */
    fun playInFullScreen() {
        mVideoInfo?.let {
            FullScreenActivity.openActivity(context, it)
        }
    }

    /**
     * 检查悬浮窗权限并在悬浮窗中播放
     */
    fun tryPlayInWindow() {
        val activity = context
        if (activity is FragmentActivity) {
            SystemAlertWindowPermission.request(activity, object : SystemAlertWindowPermission.OnRequestResultListener {
                override fun onResult(isGranted: Boolean) {
                    if (isGranted) {
                        playInWindow()
                    } else {
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                            //Android 8.0存在一个bug，用户在设置页面开启悬浮窗权限并返回，此时并不能得到正确的悬浮窗权限结果，但实际已经可以添加悬浮窗
                            Snackbar.make(this@VideoContainer, "没有悬浮窗权限，Android 8.0手机第一次开启权限会提示失败，但实际已有权限，再次点击悬浮窗按钮即可", Snackbar.LENGTH_LONG).show()
                        } else {
                            Snackbar.make(this@VideoContainer, "没有悬浮窗权限", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }

    /**
     * 在悬浮窗中播放
     */
    open fun playInWindow() {
        mVideoInfo?.let {
            VideoFloatWindow.instance.show()
            VideoFloatWindow.instance.setVideoInfo(it)
        }
    }
}