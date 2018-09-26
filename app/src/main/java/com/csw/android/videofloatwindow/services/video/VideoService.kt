package com.csw.android.videofloatwindow.services.video

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import android.view.View
import android.widget.RemoteViews
import com.csw.android.videofloatwindow.IVideoServiceInterface
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayerListener
import com.csw.android.videofloatwindow.view.VideoFloatWindow
import com.google.android.exoplayer2.Player
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class VideoService : Service() {

    companion object {
        const val FOREGROUND_ID: Int = 1
        const val NOTIFICATION_CHANNEL_ID: String = "NOTIFICATION_CHANNEL_ID"

        const val REQUEST_CODE: Int = 100
        const val ACTION_PLAY_NEXT: String = "play_next"
        const val ACTION_PLAY_PREVIOUS: String = "play_previous"
        const val ACTION_PLAY_CURR: String = "play_curr"
        const val ACTION_PAUSE_CURR: String = "pause_curr"
        const val ACTION_FULL_SCREEN: String = "full_screen"
        const val ACTION_FLOAT_WINDOW: String = "float_window"
    }

    private lateinit var mNotification: Notification
    private lateinit var remoteViews: RemoteViews
    lateinit var videoFloatWindow: VideoFloatWindow
        private set
    private val stub = object : IVideoServiceInterface.Stub() {
        override fun playInFloatWindow() {
        }
    }
    private lateinit var mNotificationManager: NotificationManager
    private val playerListener = object : PlayerListener() {

        override fun onFloatWindowVisibilityChanged(isVisibility: Boolean) {
            super.onFloatWindowVisibilityChanged(isVisibility)
            remoteViews.setViewVisibility(
                    R.id.v_float_window,
                    if (isVisibility) View.GONE
                    else View.VISIBLE
            )
            updateNotification()
        }

        override fun onVideoInfoChanged(newVideoInfo: VideoInfo) {
            super.onVideoInfoChanged(newVideoInfo)
            loadImage(newVideoInfo.mediaDbId)
            remoteViews.setTextViewText(R.id.tv_title, newVideoInfo.fileName)
            remoteViews.setViewVisibility(
                    R.id.v_previous,
                    if (MyApplication.instance.playerHelper.hasPrevious()) View.VISIBLE
                    else View.GONE
            )
            remoteViews.setViewVisibility(
                    R.id.v_next,
                    if (MyApplication.instance.playerHelper.hasNext()) View.VISIBLE
                    else View.GONE
            )
            updateNotification()
        }

        var disposable: Disposable? = null
        private fun loadImage(mediaDbId: Long) {
            //删除正在进行的加载任务
            disposable?.let {
                if (!it.isDisposed) {
                    it.dispose()
                }
            }
            //进行图片加载
            disposable = Observable.create<Bitmap> {
                val bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                        contentResolver,
                        mediaDbId,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        null
                )
                if (bitmap != null) {
                    it.onNext(bitmap)
                    it.onComplete()
                } else {
                    it.onError(Throwable("no video image has found"))
                }
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                remoteViews.setImageViewBitmap(R.id.iv_image, it)
                                updateNotification()
                            },
                            {
                                remoteViews.setImageViewResource(R.id.iv_image, R.drawable.icon_float_window)
                                updateNotification()
                            })
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
                    remoteViews.setViewVisibility(
                            R.id.exo_play,
                            if (playWhenReady) View.GONE
                            else View.VISIBLE
                    )
                    remoteViews.setViewVisibility(
                            R.id.exo_pause,
                            if (playWhenReady) View.VISIBLE
                            else View.GONE
                    )
                }
                Player.STATE_ENDED -> {
                    remoteViews.setViewVisibility(
                            R.id.exo_pause,
                            View.GONE
                    )
                    remoteViews.setViewVisibility(
                            R.id.exo_play,
                            View.VISIBLE
                    )
                }
            }
            updateNotification()
        }
    }

    private fun updateNotification() {
        mNotificationManager.notify(FOREGROUND_ID, mNotification)
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

    override fun onCreate() {
        super.onCreate()
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotification()
        startForeground(VideoService.FOREGROUND_ID, mNotification)
        videoFloatWindow = VideoFloatWindow(this)
        MyApplication.instance.playerHelper.setVideoFloatWindow(videoFloatWindow)
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


    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(
                    VideoService.NOTIFICATION_CHANNEL_ID,
                    "视频悬浮窗",
                    NotificationManager.IMPORTANCE_HIGH
            )
            nc.description = "提供视频播放控制"
            mNotificationManager.createNotificationChannel(nc)
        }

        remoteViews = RemoteViews(packageName, R.layout.view_play_control_notification)
        setNotificationClickListener(R.id.v_previous, VideoService.ACTION_PLAY_PREVIOUS)
        setNotificationClickListener(R.id.v_next, VideoService.ACTION_PLAY_NEXT)
        setNotificationClickListener(R.id.exo_play, VideoService.ACTION_PLAY_CURR)
        setNotificationClickListener(R.id.exo_pause, VideoService.ACTION_PAUSE_CURR)
        setNotificationClickListener(R.id.v_full_screen, VideoService.ACTION_FULL_SCREEN)
        setNotificationClickListener(R.id.v_float_window, VideoService.ACTION_FLOAT_WINDOW)

        mNotification = NotificationCompat.Builder(this, VideoService.NOTIFICATION_CHANNEL_ID)
                .setCustomBigContentView(remoteViews)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setOnlyAlertOnce(true)
                .build()
    }

    private fun setNotificationClickListener(viewId: Int, broadcastAction: String) {
        remoteViews.setOnClickPendingIntent(
                viewId,
                PendingIntent.getBroadcast(
                        this,
                        VideoService.REQUEST_CODE,
                        Intent(broadcastAction),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        return stub
    }

    override fun onDestroy() {
        videoFloatWindow.hide()
        unregisterReceiver(playControlReceiver)
        MyApplication.instance.playerHelper.setVideoFloatWindow(null)
        MyApplication.instance.playerHelper.removePlayerListener(playerListener)
        super.onDestroy()
    }

}