package com.csw.android.videofloatwindow.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.ui.base.BaseActivity
import com.csw.android.videofloatwindow.util.LogUtils
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
        setFullScreen(true)
        super.onCreate(savedInstanceState)
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
        MyApplication.instance.playerHelper.hideFloatWindow()
        if (intent != null) {
            val videoInfo = intent.getSerializableExtra("VideoInfo")
            val uri = intent.data
            if (videoInfo != null && videoInfo is VideoInfo) {
                videoContainer.setVideoInfo(videoInfo).play().bindPlayer()
            } else if (uri != null && "content" == uri.scheme) {
                videoContainer.setVideoInfo(Utils.getVideoInfo(contentResolver, uri)).play().bindPlayer()
            }
        } else {
            videoContainer.setVideoInfo(null)
        }
    }

    override fun onDestroy() {
        videoContainer.unBindPlayer()
        super.onDestroy()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        LogUtils.e(msg = "Activity__>${event?.action}___>${super.onTouchEvent(event)}");
        return super.onTouchEvent(event)
    }

}