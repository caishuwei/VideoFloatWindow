package com.csw.android.videofloatwindow.view

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayerHelper
import com.csw.android.videofloatwindow.services.video.VideoService
import com.csw.android.videofloatwindow.ui.FullScreenActivity

class FloatWindowVideoContainer : VideoContainer {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onBindPlayer(playerBindHelper: PlayerHelper.PlayerBindHelper) {
        super.onBindPlayer(playerBindHelper)
        videoInfo?.let {
            playerBindHelper.setBackClickListener(null)
                    .setTitle(it.fileName)
                    .setCloseClickListener(OnClickListener {
                        VideoService.instance.videoFloatWindow.hide()
                    })
                    .setFullScreenClickListener(OnClickListener {
                        playInFullScreen()
                    })
                    .setPreviousClickListener(OnClickListener {
                        playPre()
                    })
                    .setNextClickListener(OnClickListener {
                        playNext()
                    })
        }
    }

    private fun playNext() {
        if (!videoList.isEmpty()) {
            var vi: VideoInfo? = null
            videoInfo?.let {
                var preVI: VideoInfo? = null
                for (vi2 in videoList) {
                    if (preVI != null && preVI.filePath == it.filePath) {
                        vi = vi2
                        break
                    }
                    preVI = vi2
                }
            }
            if (vi == null) vi = videoList[0]
            vi?.let { vi4 ->
                VideoService.instance.videoFloatWindow.setVideoInfo(vi4)
            }
        }
    }

    private fun playPre() {
        if (!videoList.isEmpty()) {
            var vi: VideoInfo? = null
            videoInfo?.let {
                var preVI: VideoInfo? = null
                for (vi2 in videoList) {
                    if (vi2.filePath == it.filePath) {
                        vi = preVI
                        break
                    }
                    preVI = vi2
                }
            }
            if (vi == null) vi = videoList[0]
            vi?.let { vi4 ->
                VideoService.instance.videoFloatWindow.setVideoInfo(vi4)
            }
        }
    }

    private fun playInFullScreen() {
        videoInfo?.let {
            FullScreenActivity.openActivity(context, it)
            VideoService.instance.videoFloatWindow.hide()
        }
    }

    private val videoList = ArrayList<VideoInfo>()

    fun setVideoList(videoList: ArrayList<VideoInfo>) {
        this.videoList.clear()
        this.videoList.addAll(videoList)
    }

}