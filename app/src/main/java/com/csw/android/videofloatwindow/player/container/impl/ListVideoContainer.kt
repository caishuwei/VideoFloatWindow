package com.csw.android.videofloatwindow.player.container.impl

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.video.IControllerSettingHelper
import com.csw.android.videofloatwindow.player.video.IVideo
import com.csw.android.videofloatwindow.view.WHRatioImageView

/**
 * 用于列表的视频显示容器，添加了ImageView作为视频预览图显示控件，以及与悬浮窗播放的协调，优先在悬浮窗播放
 */
class ListVideoContainer : VideoContainer {

    val whRatioImageView: WHRatioImageView

    //    var onVideoPlayListener: ExoVideoView.OnVideoPlayListener? = null
    val floatWindowClickListener = OnClickListener { _ ->
        tryPlayInWindow()
    }
    val fullScreenClickListener = OnClickListener {
        playInFullScreen()
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        isFocusable = true
        isFocusableInTouchMode = true
        whRatioImageView = WHRatioImageView(context)
        whRatioImageView.setOnClickListener {
            play()
        }
        addView(whRatioImageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    override fun setVideoInfo(videoInfo: VideoInfo) {
        super.setVideoInfo(videoInfo)
        whRatioImageView.visibility = View.VISIBLE
    }

    override fun onVideoInfoChanged(old: VideoInfo?, new: VideoInfo) {
        syncVideoInfoToCurrVideo()
    }

    //在不存在顶层视频容器时，才进行VideoView获取，避免在列表界面跟顶层容器抢占>>>>>>>>>>>>>>>>>>>>>
    /**
     * 开始播放，隐藏视频预览图
     */
    override fun play() {
        val topLevelVideoContainer = PlayHelper.getTopLevelVideoContainer()
        if (topLevelVideoContainer != null) {
            //通过顶层视频容器进行播放
            getVideoInfo()?.let {
                topLevelVideoContainer.setVideoInfo(it)
                topLevelVideoContainer.play()
            }
        } else {
            whRatioImageView.visibility = View.GONE
            super.play()
        }
    }

    override fun pause() {
        if (PlayHelper.getTopLevelVideoContainer() == null) {
            super.pause()
        }
    }

    override fun bindVideoView() {
        if (PlayHelper.getTopLevelVideoContainer() == null) {
            super.bindVideoView()
        }
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    override fun onVideoUnbind(video: IVideo) {
        super.onVideoUnbind(video)
        whRatioImageView.visibility = View.VISIBLE
    }

    override fun onPlayControllerSetup(controllerSettingHelper: IControllerSettingHelper) {
        super.onPlayControllerSetup(controllerSettingHelper)
        getVideoInfo()?.let {
            controllerSettingHelper.setTitle(it.fileName)
                    .setFloatWindowClickListener(floatWindowClickListener)
                    .setFullScreenClickListener(fullScreenClickListener)
                    .setVolumeAndBrightnessControllerEnable(false)
        }
    }

}