package com.csw.android.videofloatwindow.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.window.VideoFloatWindow
import com.csw.android.videofloatwindow.ui.base.BaseActivity
import com.csw.android.videofloatwindow.util.Utils
import kotlinx.android.synthetic.main.activity_full_screen.*

/**
 * 全屏播放实现
 */
class FullScreenActivity : BaseActivity() {
    companion object {
        fun openActivity(context: Context, videoInfo: VideoInfo) {
            val intent = Intent(context, FullScreenActivity::class.java)
            intent.putExtra("VideoInfo", videoInfo)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen(true)
    }

    override fun getContentViewID(): Int {
        return R.layout.activity_full_screen
    }

    override fun initData() {
        super.initData()
        initData(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        initData(intent)
    }

    private fun initData(intent: Intent?) {
        VideoFloatWindow.instance.hide()
        if (intent != null) {
            val videoInfo = intent.getSerializableExtra("VideoInfo")
            val uri = intent.data
            if (videoInfo != null && videoInfo is VideoInfo) {
                videoContainer.setVideoInfo(videoInfo, true)
                videoContainer.play()
            } else if (uri != null && "content" == uri.scheme) {
                val vi = Utils.getVideoInfo(contentResolver, uri)
                vi?.let {
                    videoContainer.setVideoInfo(it, true)
                    videoContainer.play()
                }
            }
        }
        PlayHelper.setTopLevelVideoContainer(videoContainer)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun finish() {
        PlayHelper.removeTopLevelVideoContainer(videoContainer)
        super.finish()
    }

    override fun onDestroy() {
        PlayHelper.removeTopLevelVideoContainer(videoContainer)
        videoContainer.releaseVideoView()
        super.onDestroy()
    }

}