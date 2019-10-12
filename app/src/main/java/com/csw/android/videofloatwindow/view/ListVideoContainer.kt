package com.csw.android.videofloatwindow.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.base.VideoContainer
import com.csw.android.videofloatwindow.player.video.base.IControllerSettingHelper
import com.csw.android.videofloatwindow.player.video.base.IVideo

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

    override fun setVideoInfo(videoInfo: VideoInfo, changeVideoView: Boolean) {
        super.setVideoInfo(videoInfo, changeVideoView)
        whRatioImageView.visibility = View.VISIBLE
    }

    //在不存在顶层视频容器时，才进行VideoView获取，避免在列表界面跟顶层容器抢占>>>>>>>>>>>>>>>>>>>>>
    /**
     * 开始播放，隐藏视频预览图
     */
    override fun play(): VideoContainer {
        val topLevelVideoContainer = PlayHelper.getTopLevelVideoContainer()
        return if (topLevelVideoContainer != null) {
            //通过顶层视频容器进行播放
            mVideoInfo?.let {
                topLevelVideoContainer.setVideoInfo(it)
                topLevelVideoContainer.play()
            }
            this
        } else {
            whRatioImageView.visibility = View.GONE
            super.play()
        }
    }

    override fun pause(): VideoContainer {
        return if (PlayHelper.getTopLevelVideoContainer() != null) {
            this
        } else {
            super.pause()
        }
    }

    override fun bindVideoView() {
        if (PlayHelper.getTopLevelVideoContainer() == null) {
            super.bindVideoView()
        }
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    override fun onUnbindVideo(video: IVideo) {
        super.onUnbindVideo(video)
        whRatioImageView.visibility = View.VISIBLE
    }

    override fun settingPlayController(controllerSettingHelper: IControllerSettingHelper) {
        super.settingPlayController(controllerSettingHelper)
        mVideoInfo?.let {
            controllerSettingHelper.setTitle(it.fileName)
                    .setFloatWindowClickListener(floatWindowClickListener)
                    .setFullScreenClickListener(fullScreenClickListener)
                    .setVolumeAndBrightnessControllerEnable(false)
        }
    }

}