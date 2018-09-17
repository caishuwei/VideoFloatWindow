package com.csw.android.videofloatwindow.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.view.VideoView
import kotlinx.android.synthetic.main.activity_full_screen.*


class FullScreenActivity : AppCompatActivity() {

    companion object {

        fun openActivity(videoView: VideoView) {

        }

        fun openActivity(context: Context, videoInfo: VideoInfo) {
            val intent = Intent(context, FullScreenActivity::class.java)
            intent.putExtra("VideoInfo", videoInfo)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen(true)
        setContentView(R.layout.activity_full_screen)
        videoView.vBack.setOnClickListener {
            finish()
        }
        videoView.vFullScreen.visibility = View.GONE

        val videoInfo = intent.getSerializableExtra("VideoInfo")
        if (videoInfo != null && videoInfo is VideoInfo) {
            requestedOrientation = if (videoInfo.getWHRatio() > 1) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            videoView.setVideoInfo(videoInfo)
            videoView.play()
        }
    }

    override fun onDestroy() {
        videoView.release()
        super.onDestroy()
    }


    fun setFullScreen(fullScreen: Boolean) {
        //兼容v7包的那个古怪的activity
        if (this is AppCompatActivity) {
            val supportActionBar = this.supportActionBar
            if (supportActionBar != null) {
                if (fullScreen) {
                    supportActionBar.hide()
                } else {
                    supportActionBar.show()
                }
            }
        }
        //全屏设置
        if (fullScreen) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
                window.decorView.systemUiVisibility = View.GONE
            } else if (Build.VERSION.SDK_INT >= 19) {
                //SYSTEM_UI_FLAG_IMMERSIVE_STICKY是沉浸模式，api19开始有的，
                // 需要搭配SYSTEM_UI_FLAG_HIDE_NAVIGATION（隐藏虚拟按钮）或SYSTEM_UI_FLAG_FULLSCREEN（隐藏状态栏）进行使用才有效
                //如果不加SYSTEM_UI_FLAG_IMMERSIVE_STICKY标签，用户手动打开会清除前两个标签，加了Sticky标签后用户打开视为暂时显示状态栏和虚拟按钮
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }
        } else {
            if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
                window.decorView.systemUiVisibility = View.VISIBLE
            } else if (Build.VERSION.SDK_INT >= 19) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }

}