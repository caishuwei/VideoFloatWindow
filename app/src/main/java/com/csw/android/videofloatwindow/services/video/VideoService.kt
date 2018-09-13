package com.csw.android.videofloatwindow.services.video

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.csw.android.videofloatwindow.IVideoServiceInterface
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.view.VideoFloatWindow

class VideoService : Service() {

    companion object {
        const val FOREGROUND_ID: Int = 1
        const val NOTIFICATION_CHANNEL_ID: String = "NOTIFICATION_CHANNEL_ID"
    }

    private lateinit var mNotification: Notification
    private lateinit var videoFloatWindow: VideoFloatWindow
    private val stub = object : IVideoServiceInterface.Stub() {
        override fun playInFloatWindow() {
            videoFloatWindow.show()

        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotification()
        startForeground(VideoService.FOREGROUND_ID, mNotification)
        videoFloatWindow = VideoFloatWindow(this)
    }

    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(
                    VideoService.NOTIFICATION_CHANNEL_ID,
                    "视频悬浮窗",
                    NotificationManager.IMPORTANCE_HIGH
            )
            nc.description = "提供视频播放控制"
            val nm: NotificationManager = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(nc)
        }

        mNotification = NotificationCompat.Builder(this, VideoService.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setContentTitle("视频悬浮窗")
                .setContentText("提供视频播放控制")
                .build()
    }

    override fun onBind(intent: Intent?): IBinder {
        return stub
    }

    override fun onDestroy() {
        videoFloatWindow.hide()
        super.onDestroy()
    }

}