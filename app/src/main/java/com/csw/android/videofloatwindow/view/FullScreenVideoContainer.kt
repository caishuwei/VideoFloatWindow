package com.csw.android.videofloatwindow.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.player.base.VideoContainer
import com.csw.android.videofloatwindow.player.video.CustomVideoView

class FullScreenVideoContainer : VideoContainer {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun settingPlayController(controllerSettingHelper: CustomVideoView.ControllerSettingHelper) {
        super.settingPlayController(controllerSettingHelper)
        videoInfo?.let {
            controllerSettingHelper
                    .setTitle(it.fileName)
                    .setBackClickListener(OnClickListener { _ ->
                        finishActivity()
                    })
                    .setFloatWindowClickListener(OnClickListener { _ ->
                        tryPlayInWindow()
                    })
                    .setPreviousClickListener(
                            if (MyApplication.instance.playerHelper.hasPrevious()) {
                                OnClickListener { _ ->
                                    playPre()
                                }
                            } else {
                                null
                            }
                    )
                    .setNextClickListener(
                            if (MyApplication.instance.playerHelper.hasNext()) {
                                OnClickListener { _ ->
                                    playNext()
                                }
                            } else {
                                null
                            }
                    )
                    .setVolumeAndBrightnessControllerEnable(true)
        }

    }

    private fun playNext() {
        val nextVideo = MyApplication.instance.playerHelper.getNext()
        if (nextVideo != null) {
            videoInfo = nextVideo
            play()
        }
    }

    private fun playPre() {
        val preVideo = MyApplication.instance.playerHelper.getPrevious()
        if (preVideo != null) {
            videoInfo = preVideo
            play()
        }
    }

    override fun playInWindow() {
        super.playInWindow()
        finishActivity()
    }

    private fun finishActivity() {
        val activity = context
        if (activity is Activity) {
            activity.finish()
        }
    }
}