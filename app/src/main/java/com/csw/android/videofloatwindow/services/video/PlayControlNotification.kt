package com.csw.android.videofloatwindow.services.video

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import android.view.View
import android.widget.RemoteViews
import com.csw.android.videofloatwindow.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class PlayControlNotification {

    companion object {
        private const val NOTIFICATION_CHANNEL_GROUP_ID = "视频播放控制通知独立分组"
        private const val NOTIFICATION_CHANNEL_GROUP_NAME = "视频播放控制通知独立分组"
        private const val NOTIFICATION_CHANNEL_GROUP_DESC = "设置视频播放控制的通知在独立分组里面，避免与其他通知合并导致控制不可见"

        const val NOTIFICATION_ID: Int = 1
        const val NOTIFICATION_CHANNEL_ID: String = "NOTIFICATION_CHANNEL_ID"
        const val NOTIFICATION_CHANNEL_NAME: String = "视频播放控制通知渠道"
        const val NOTIFICATION_CHANNEL_DESC: String = "该渠道用于提供视频播放控制通知的显示"

    }

    private val context: Context
    private val mNotificationManager: NotificationManager
    lateinit var mNotification: Notification
    private lateinit var bigRemoteViews: RemoteViews
    private lateinit var normalRemoteViews: RemoteViews

    private val updateNotificationTask: Runnable
    private val mainHandler = Handler(Looper.getMainLooper())

    constructor(context: Context) {
        this.context = context
        mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        updateNotificationTask = Runnable {
            mNotificationManager.notify(NOTIFICATION_ID, mNotification)
        }
        createChannel()
        createNotification()
    }


    private fun createNotification() {
        bigRemoteViews = RemoteViews(context.packageName, R.layout.view_play_control_big_notification)
        setNotificationClickListener(bigRemoteViews, R.id.v_previous, VideoService.ACTION_PLAY_PREVIOUS)
        setNotificationClickListener(bigRemoteViews, R.id.v_next, VideoService.ACTION_PLAY_NEXT)
        setNotificationClickListener(bigRemoteViews, R.id.exo_play, VideoService.ACTION_PLAY_CURR)
        setNotificationClickListener(bigRemoteViews, R.id.exo_pause, VideoService.ACTION_PAUSE_CURR)
        setNotificationClickListener(bigRemoteViews, R.id.v_full_screen, VideoService.ACTION_FULL_SCREEN)
        setNotificationClickListener(bigRemoteViews, R.id.v_float_window, VideoService.ACTION_FLOAT_WINDOW)

        normalRemoteViews = RemoteViews(context.packageName, R.layout.view_play_control_normal_notification)
        setNotificationClickListener(normalRemoteViews, R.id.v_previous, VideoService.ACTION_PLAY_PREVIOUS)
        setNotificationClickListener(normalRemoteViews, R.id.v_next, VideoService.ACTION_PLAY_NEXT)
        setNotificationClickListener(normalRemoteViews, R.id.exo_play, VideoService.ACTION_PLAY_CURR)
        setNotificationClickListener(normalRemoteViews, R.id.exo_pause, VideoService.ACTION_PAUSE_CURR)
        setNotificationClickListener(normalRemoteViews, R.id.v_full_screen, VideoService.ACTION_FULL_SCREEN)
        setNotificationClickListener(normalRemoteViews, R.id.v_float_window, VideoService.ACTION_FLOAT_WINDOW)

        mNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setCustomContentView(normalRemoteViews)
                .setCustomBigContentView(bigRemoteViews)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setOnlyAlertOnce(true)
                .build()


    }

    private fun setNotificationClickListener(remoteViews: RemoteViews, viewId: Int, broadcastAction: String) {
        remoteViews.setOnClickPendingIntent(
                viewId,
                PendingIntent.getBroadcast(
                        context,
                        VideoService.REQUEST_CODE,
                        Intent(broadcastAction),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        )
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //建立独立分组
            val group = NotificationChannelGroup(NOTIFICATION_CHANNEL_GROUP_ID, NOTIFICATION_CHANNEL_GROUP_NAME)
            mNotificationManager.createNotificationChannelGroup(group)
            //建立通知渠道
            val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = NOTIFICATION_CHANNEL_DESC
            channel.group = NOTIFICATION_CHANNEL_GROUP_ID
            channel.enableLights(false)
            channel.enableVibration(false)
            mNotificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification() {
        mainHandler.removeCallbacks(updateNotificationTask)
        mainHandler.postDelayed(updateNotificationTask, 100)
    }

    fun setFloatWindowButtonVisibility(i: Int) {
        setViewVisibility(R.id.v_float_window, i)
        updateNotification()
    }

    fun setPreviousButtonVisibility(i: Int) {
        setViewVisibility(R.id.v_previous, i)
        updateNotification()
    }

    fun setNextButtonVisibility(i: Int) {
        setViewVisibility(R.id.v_next, i)
        updateNotification()
    }


    fun setTitle(fileName: String) {
        normalRemoteViews.setTextViewText(
                R.id.tv_title,
                fileName
        )
        bigRemoteViews.setTextViewText(
                R.id.tv_title,
                fileName
        )
        updateNotification()
    }

    fun setIsPlaying(playing: Boolean) {
        setViewVisibility(R.id.exo_play, if (playing) View.GONE else View.VISIBLE)
        setViewVisibility(R.id.exo_pause, if (playing) View.VISIBLE else View.GONE)
        updateNotification()
    }

    fun loadVideoImage(mediaDbId: Long) {
        loadImage(mediaDbId)
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
                    context.contentResolver,
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
                            normalRemoteViews.setImageViewBitmap(R.id.iv_image, it)
                            bigRemoteViews.setImageViewBitmap(R.id.iv_image, it)
                            updateNotification()
                        },
                        {
                            normalRemoteViews.setImageViewResource(R.id.iv_image, R.drawable.icon_float_window)
                            bigRemoteViews.setImageViewResource(R.id.iv_image, R.drawable.icon_float_window)
                            updateNotification()
                        })
    }

    private fun setViewVisibility(viewId: Int, viewVisibility: Int) {
        normalRemoteViews.setViewVisibility(
                viewId,
                viewVisibility
        )
        bigRemoteViews.setViewVisibility(
                viewId,
                viewVisibility
        )
    }
}