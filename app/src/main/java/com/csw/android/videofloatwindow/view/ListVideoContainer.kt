package com.csw.android.videofloatwindow.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import com.csw.android.videofloatwindow.player.base.VideoContainer
import com.csw.android.videofloatwindow.player.video.CustomVideoView

class ListVideoContainer : VideoContainer {

    val whRatioImageView: WHRatioImageView
    var onVideoPlayListener: CustomVideoView.OnVideoPlayListener? = null
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
        addView(whRatioImageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    override fun onSetNewVideoInfo() {
        super.onSetNewVideoInfo()
        whRatioImageView.visibility = View.VISIBLE
    }

    override fun play(): VideoContainer {
        whRatioImageView.visibility = View.GONE
        return super.play()
    }

    override fun onUnbindVideoView(videoView: CustomVideoView) {
        super.onUnbindVideoView(videoView)
        whRatioImageView.visibility = View.VISIBLE
    }

    override fun settingPlayController(controllerSettingHelper: CustomVideoView.ControllerSettingHelper) {
        super.settingPlayController(controllerSettingHelper)
        videoInfo?.let {
            controllerSettingHelper.setTitle(it.fileName)
                    .setFloatWindowClickListener(floatWindowClickListener)
                    .setFullScreenClickListener(fullScreenClickListener)
                    .setOnVideoPlayListener(onVideoPlayListener)
                    .setVolumeAndBrightnessControllerEnable(false)
        }
    }

}