package com.csw.android.videofloatwindow.ui.video.full_screen

import android.app.Activity
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
            if (!(context is Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)//若该页面启动了多次，将该页面重排到栈顶
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
        videoContainer.setBindVideoOnViewEnterForeground(true)
        videoContainer.setPauseOnViewExitForeground(!PlayHelper.backgroundPlay)
        videoContainer.setReleaseOnViewDestroy(true)
        videoContainer.registerViewLifeCycleObserver(lifecycle, true)
        if (intent != null) {
            val videoInfo = intent.getSerializableExtra("VideoInfo")
            val uri = intent.data
            if (videoInfo != null && videoInfo is VideoInfo) {
                videoContainer.setVideoInfo(videoInfo)
                videoContainer.play()
            } else if (uri != null && "content" == uri.scheme) {
                //处理媒体库Uri，这通常是文件浏览器之类的寻找能处理视频播放的app，最终进入到这里
                val vi = Utils.getVideoInfo(contentResolver, uri)
                vi?.let {
                    videoContainer.setVideoInfo(it)
                    videoContainer.play()
                }
            }
        }
        PlayHelper.setTopLevelVideoContainer(videoContainer)
    }

    override fun onResume() {
        super.onResume()
        videoContainer.play()
    }

    override fun finish() {
        //退出界面就不需要停止播放了
        videoContainer.setPauseOnViewExitForeground(false)
        PlayHelper.removeTopLevelVideoContainer(videoContainer)
        super.finish()
    }

    override fun onDestroy() {
        PlayHelper.removeTopLevelVideoContainer(videoContainer)
        super.onDestroy()
    }

}