package com.csw.android.videofloatwindow.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.PlayList
import com.csw.android.videofloatwindow.player.base.VideoContainer
import com.csw.android.videofloatwindow.player.video.base.IControllerSettingHelper

class FullScreenVideoContainer : VideoContainer {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun settingPlayController(controllerSettingHelper: IControllerSettingHelper) {
        super.settingPlayController(controllerSettingHelper)
        mVideoInfo?.let {
            controllerSettingHelper
                    .setTitle(it.fileName)
                    .setBackClickListener(OnClickListener { _ ->
                        finishActivity()
                    })
                    .setFloatWindowClickListener(OnClickListener { _ ->
                        tryPlayInWindow()
                    })
                    .setPreviousClickListener(
                            if (PlayList.hasPrevious()) {
                                OnClickListener { _ ->
                                    playPre()
                                }
                            } else {
                                null
                            }
                    )
                    .setNextClickListener(
                            if (PlayList.hasNext()) {
                                OnClickListener { _ ->
                                    playNext()
                                }
                            } else {
                                null
                            }
                    )
                    .setVolumeAndBrightnessControllerEnable(true)
        }
        tryRotateScreen()
    }


    private fun playNext() {
        PlayHelper.tryPlayNext()
    }

    private fun playPre() {
        PlayHelper.tryPlayPrevious()
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