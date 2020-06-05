package com.csw.android.videofloatwindow.player.container.impl

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.PlayList
import com.csw.android.videofloatwindow.player.video.IControllerSettingHelper
import com.csw.android.videofloatwindow.player.video.IVideo

/**
 * 全屏视频播放的视频容器，根据播放的视频旋转Activity，使得视频可以占用更大的空间来显示
 * 开启播放控制器的一些按钮，设置点击事件
 */
class FullScreenVideoContainer : VideoContainer {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onPlayControllerSetup(controllerSettingHelper: IControllerSettingHelper) {
        super.onPlayControllerSetup(controllerSettingHelper)
        getVideoInfo()?.let {
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

    override fun onVideoInfoChanged(old: VideoInfo?, new: VideoInfo) {
        syncVideoInfoToCurrVideo()
    }

    override fun onVideoUnbind(video: IVideo) {
        super.onVideoUnbind(video)
        finishActivity()
    }

    private fun playNext() {
        PlayHelper.tryPlayNext()
    }

    private fun playPre() {
        PlayHelper.tryPlayPrevious()
    }

    private fun finishActivity() {
        val activity = context
        if (activity is Activity) {
            activity.finish()
        }
    }
}