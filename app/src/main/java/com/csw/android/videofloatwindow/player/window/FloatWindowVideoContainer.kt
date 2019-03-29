package com.csw.android.videofloatwindow.player.window

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.PlayList
import com.csw.android.videofloatwindow.player.base.VideoContainer
import com.csw.android.videofloatwindow.player.video.base.IControllerSettingHelper
import com.csw.android.videofloatwindow.player.video.base.IVideo
import com.csw.android.videofloatwindow.player.video.exo.ExoVideoView

class FloatWindowVideoContainer : VideoContainer {
    var videoFloatWindow: VideoFloatWindow? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun settingPlayController(controllerSettingHelper: IControllerSettingHelper) {
        super.settingPlayController(controllerSettingHelper)
        mVideoInfo?.let { it ->
            controllerSettingHelper.setBackClickListener(null)
                    .setTitle(it.fileName)
                    .setCloseClickListener(OnClickListener { _ ->
                        VideoFloatWindow.instance.hide()
                    })
                    .setFullScreenClickListener(OnClickListener { _ ->
                        playInFullScreen()
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
                    .setVolumeAndBrightnessControllerEnable(false)
            videoFloatWindow?.updateWindowWH(it)
        }
    }

    private fun playNext() {
        PlayHelper.tryPlayNext()
    }

    private fun playPre() {
        PlayHelper.tryPlayPrevious()
    }

}