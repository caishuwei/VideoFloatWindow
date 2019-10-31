package com.csw.android.videofloatwindow.player.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.view.View
import androidx.core.content.ContextCompat
import com.csw.android.videofloatwindow.IVideoServiceInterface
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.PlayList
import com.csw.android.videofloatwindow.player.window.OnFloatWindowChangeListener
import com.csw.android.videofloatwindow.player.window.VideoFloatWindow

/**
 * 播放服务，使用前台服务实现，减少被回收的概率，提供播放控制通知，处理通知的播放控制事件
 */
class PlayService : Service() {

    companion object {
        /**
         * 启动播放服务
         */
        fun startService(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, PlayService::class.java))
        }

        /**
         * 关闭播放服务
         */
        fun stopService(context: Context) {
            context.stopService(Intent(context, PlayService::class.java))
        }

        const val REQUEST_CODE: Int = 100
        const val ACTION_PLAY_NEXT: String = "play_next"
        const val ACTION_PLAY_PREVIOUS: String = "play_previous"
        const val ACTION_PLAY_CURR: String = "play_curr"
        const val ACTION_PAUSE_CURR: String = "pause_curr"
        const val ACTION_FULL_SCREEN: String = "full_screen"
        const val ACTION_FLOAT_WINDOW: String = "float_window"
    }

    private val stub = object : IVideoServiceInterface.Stub() {
        override fun playInFloatWindow() {
        }
    }
    private val playerListener = object : PlayHelper.OnPlayChangeListener {
        override fun onPlayVideoInfoUpdated(videoInfo: VideoInfo?) {
            PlayHelper.lastPlayVideo?.let {
                val newVideoInfo = it.getVideoInfo()
                playControlNotification.loadVideoImage(newVideoInfo.mediaDbId)
                playControlNotification.setTitle(newVideoInfo.fileName)
                playControlNotification.setPreviousButtonVisibility(
                        if (PlayList.hasPrevious()) View.VISIBLE
                        else View.GONE
                )
                playControlNotification.setNextButtonVisibility(
                        if (PlayList.hasNext()) View.VISIBLE
                        else View.GONE
                )
                playControlNotification.setIsPlaying(it.isPlaying())
            }
        }
    }

    private val playControlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    ACTION_PLAY_NEXT -> PlayHelper.tryPlayNext()
                    ACTION_PLAY_PREVIOUS -> PlayHelper.tryPlayPrevious()
                    ACTION_PLAY_CURR -> PlayHelper.tryPlayCurr()
                    ACTION_PAUSE_CURR -> PlayHelper.tryPauseCurr()
                    ACTION_FULL_SCREEN -> PlayHelper.tryPlayInFullScreen()
                    ACTION_FLOAT_WINDOW -> PlayHelper.tryPlayInFloatWindow()
                }
            }
        }
    }
    private lateinit var playControlNotification: PlayControlNotification
    override fun onCreate() {
        super.onCreate()
        playControlNotification = PlayControlNotification(this)
        startForeground(PlayControlNotification.NOTIFICATION_ID, playControlNotification.mNotification)

        PlayHelper.addOnPlayVideoChangeListener(playerListener)
        VideoFloatWindow.instance.onFloatWindowChangeListener = object : OnFloatWindowChangeListener {
            override fun onFloatWindowVisibilityChanged(isVisibility: Boolean) {
                playControlNotification.setFloatWindowButtonVisibility(
                        if (isVisibility) View.GONE
                        else View.VISIBLE
                )
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_PLAY_NEXT)
        intentFilter.addAction(ACTION_PLAY_PREVIOUS)
        intentFilter.addAction(ACTION_PLAY_CURR)
        intentFilter.addAction(ACTION_PAUSE_CURR)
        intentFilter.addAction(ACTION_FULL_SCREEN)
        intentFilter.addAction(ACTION_FLOAT_WINDOW)
        registerReceiver(playControlReceiver, intentFilter)
    }

    override fun onBind(intent: Intent?): IBinder {
        return stub
    }

    override fun onDestroy() {
        unregisterReceiver(playControlReceiver)
        PlayHelper.removeOnPlayVideoChangeListener(playerListener)
        VideoFloatWindow.instance.onFloatWindowChangeListener = null
        super.onDestroy()
    }

}