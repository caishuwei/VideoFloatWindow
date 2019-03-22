package com.csw.android.videofloatwindow.player.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.view.View
import com.csw.android.videofloatwindow.IVideoServiceInterface
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.base.PlayerListener
import com.google.android.exoplayer2.Player

class VideoService : Service() {

    companion object {

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
    private val playerListener = object : PlayerListener() {

        override fun onFloatWindowVisibilityChanged(isVisibility: Boolean) {
            super.onFloatWindowVisibilityChanged(isVisibility)
            playControlNotification.setFloatWindowButtonVisibility(
                    if (isVisibility) View.GONE
                    else View.VISIBLE
            )
        }

        override fun onVideoInfoChanged(newVideoInfo: VideoInfo) {
            super.onVideoInfoChanged(newVideoInfo)
            playControlNotification.loadVideoImage(newVideoInfo.mediaDbId)
            playControlNotification.setTitle(newVideoInfo.fileName)
            playControlNotification.setPreviousButtonVisibility(
                    if (MyApplication.instance.playerHelper.hasPrevious()) View.VISIBLE
                    else View.GONE
            )
            playControlNotification.setNextButtonVisibility(
                    if (MyApplication.instance.playerHelper.hasNext()) View.VISIBLE
                    else View.GONE
            )
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            //播放状态改变
            when (playbackState) {
                Player.STATE_IDLE -> {
                }
                Player.STATE_BUFFERING -> {
                }
                Player.STATE_READY -> {
                    playControlNotification.setIsPlaying(playWhenReady)
                }
                Player.STATE_ENDED -> {
                    playControlNotification.setIsPlaying(false)
                }
            }
        }
    }

    private val playControlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    ACTION_PLAY_NEXT -> MyApplication.instance.playerHelper.tryPlayNext()
                    ACTION_PLAY_PREVIOUS -> MyApplication.instance.playerHelper.tryPlayPrevious()
                    ACTION_PLAY_CURR -> MyApplication.instance.playerHelper.tryPlayCurr()
                    ACTION_PAUSE_CURR -> MyApplication.instance.playerHelper.tryPauseCurr()
                    ACTION_FULL_SCREEN -> MyApplication.instance.playerHelper.tryPlayInFullScreen()
                    ACTION_FLOAT_WINDOW -> MyApplication.instance.playerHelper.tryPlayInFloatWindow()
                }
            }
        }
    }
    private lateinit var playControlNotification: PlayControlNotification
    override fun onCreate() {
        super.onCreate()
        playControlNotification = PlayControlNotification(this)
        startForeground(PlayControlNotification.NOTIFICATION_ID, playControlNotification.mNotification)

        MyApplication.instance.playerHelper.addPlayerListener(playerListener)

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
        MyApplication.instance.playerHelper.removePlayerListener(playerListener)
        super.onDestroy()
    }

}