package com.csw.android.videofloatwindow.player.base

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import android.widget.FrameLayout
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.permission.SystemAlertWindowPermission
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.video.base.IControllerSettingHelper
import com.csw.android.videofloatwindow.player.video.base.IVideo
import com.csw.android.videofloatwindow.player.window.VideoFloatWindow
import com.csw.android.videofloatwindow.ui.FullScreenActivity
import com.csw.android.videofloatwindow.util.FragmentHelper
import com.csw.android.videofloatwindow.util.LogUtils
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

    var mVideoInfo: VideoInfo? = null
        private set

    open fun setVideoInfo(videoInfo: VideoInfo, changeVideoView: Boolean = false) {
        if (!Utils.videoEquals(mVideoInfo, videoInfo)) {
            mVideoInfo = videoInfo
            if (changeVideoView) {
                currVideo?.unbindVideoContainer(this, true)
            } else {
                currVideo?.let {
                    it.setVideoInfo(videoInfo)
                    settingPlayController(it.getControllerSettingHelper());
                }
            }
        }
    }

    /**
     * 绑定VideoView
     */
    fun bindVideoView() {
        mVideoInfo?.let {
            getVideo(it)
        }
    }

    var currVideo: IVideo? = null

    /**
     * 绑定VideoView
     */
    open fun onBindVideo(video: IVideo) {
        this.currVideo = video
        videoContainer.addView(video.getView(), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        settingPlayController(video.getControllerSettingHelper())
    }

    /**
     * 绑定播放视图后的操作，如设播放控制器的各种按钮监听
     */
    open fun settingPlayController(controllerSettingHelper: IControllerSettingHelper) {

    }

    /**
     * 解除VideoView绑定
     */
    open fun onUnbindVideo(video: IVideo) {
        videoContainer.removeView(video.getView())
        this.currVideo = null
    }

    /**
     * 释放播放器
     */
    fun releaseVideoView() {
        currVideo?.unbindVideoContainer(this)
    }

    private fun getVideo(videoInfo: VideoInfo): IVideo {
        val videoView = currVideo
        return if (videoView == null) {
            val vv = PlayHelper.getVideo(videoInfo)
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
        mVideoInfo?.let {
            getVideo(it).play()
        }
        return this
    }


    /**
     * 停止视频播放
     */
    open fun pause(): VideoContainer {
        mVideoInfo?.let {
            getVideo(it).pause()
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
    fun tryRotateScreen() {
        Utils.runIfNotNull(context, mVideoInfo) { c, v ->
            if (c is Activity) {
                c.requestedOrientation = if (v.whRatio > 1) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
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
                        Snackbar.make(this@VideoContainer, "没有悬浮窗权限", Snackbar.LENGTH_SHORT).show()
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
            VideoFloatWindow.instance.setVideoInfo(it)
            VideoFloatWindow.instance.show()
        }
    }

    /**
     *  在UI销毁时释放VideoView
     */
    fun releaseOnUiDestroy(fragmentManager: FragmentManager) {
        UiDestroyListenerFragment.releaseVideoOnUiDestroy(fragmentManager, this)
    }

    class UiDestroyListenerFragment : Fragment() {

        companion object {
            fun releaseVideoOnUiDestroy(fragmentManager: FragmentManager, videoContainer: VideoContainer) {
                val instance = FragmentHelper.getFragmentInstance(fragmentManager, UiDestroyListenerFragment::class.java.name, UiDestroyListenerFragment::class.java)
                instance.registerVideoContainer(videoContainer)
            }
        }

        private val videoContainerSet = HashSet<VideoContainer>()
        private var isViewDestroyed = false
        private fun registerVideoContainer(videoContainer: VideoContainer) {
            videoContainerSet.add(videoContainer)
            if (isViewDestroyed) {
                videoContainer.releaseVideoView()
            }
        }

        /**
         * 这是一个没有视图的fragment，onViewCreated不会走，但onDestroyView会
         */
        override fun onDestroyView() {
            LogUtils.i("UiDestroyListenerFragment", "onDestroyView")
            isViewDestroyed = true
            for (videoContainer in videoContainerSet) {
                videoContainer.releaseVideoView()
            }
            videoContainerSet.clear()
            super.onDestroyView()
        }

        override fun onDestroy() {
            LogUtils.i("UiDestroyListenerFragment", "onDestroy")
            super.onDestroy()
        }

    }

}