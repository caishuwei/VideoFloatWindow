package com.csw.android.videofloatwindow.player.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.util.LogUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * 播放控制通知，用于通过通知控制后台播放的视频
 */
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

    private val notificationUpdater = NotificationUpdater()

    constructor(context: Context) {
        this.context = context
        mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel()
        createNotification()
    }


    private fun createNotification() {
        bigRemoteViews = RemoteViews(context.packageName, R.layout.view_play_control_large_notification)
        setupNoticationRemoteViews(bigRemoteViews)

        normalRemoteViews = RemoteViews(context.packageName, R.layout.view_play_control_big_notification)
        setupNoticationRemoteViews(normalRemoteViews)

        mNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setCustomContentView(normalRemoteViews)
                .setCustomBigContentView(bigRemoteViews)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setOnlyAlertOnce(true)
                .build()
    }

    private fun setupNoticationRemoteViews(remoteViews: RemoteViews) {
        setNotificationClickListener(remoteViews, R.id.v_notification_root, PlayService.ACTION_NOTIFICATION_CLICK, true)
        setNotificationClickListener(remoteViews, R.id.v_previous, PlayService.ACTION_PLAY_PREVIOUS)
        setNotificationClickListener(remoteViews, R.id.v_next, PlayService.ACTION_PLAY_NEXT)
        setNotificationClickListener(remoteViews, R.id.exo_play, PlayService.ACTION_PLAY_CURR)
        setNotificationClickListener(remoteViews, R.id.exo_pause, PlayService.ACTION_PAUSE_CURR)
        setNotificationClickListener(remoteViews, R.id.v_full_screen, PlayService.ACTION_FULL_SCREEN, true)
        setNotificationClickListener(remoteViews, R.id.iv_image, PlayService.ACTION_FULL_SCREEN, true)
        setNotificationClickListener(remoteViews, R.id.v_float_window, PlayService.ACTION_FLOAT_WINDOW, true)

    }

    private fun setNotificationClickListener(remoteViews: RemoteViews, viewId: Int, action: String, closeNotificationBar: Boolean = false) {
        if (closeNotificationBar) {
            val intent = Intent(MyApplication.instance, CloseNotificationBarActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.action = action
            remoteViews.setOnClickPendingIntent(
                    viewId,
                    PendingIntent.getActivity(
                            context,
                            PlayService.REQUEST_CODE,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    )
            )
        } else {
            remoteViews.setOnClickPendingIntent(
                    viewId,
                    PendingIntent.getBroadcast(
                            context,
                            PlayService.REQUEST_CODE,
                            Intent(action),
                            PendingIntent.FLAG_UPDATE_CURRENT
                    )
            )
        }
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

    /**
     * 实现间断更新，通知的更新本身是一个跨进程通信，比较耗时，这里限制两次更新的最小间隔时间
     *
     */
    private inner class NotificationUpdater : Runnable {
        private val mainHandler = Handler(Looper.getMainLooper())
        private val minSpace = 1000L//两次更新通知的最短间隔

        private var recentCallTime = 0L//最近的调用时间
        private var willWork = false//将会执行工作

        fun update() {
            if (!willWork) {
                val spaceTime = System.currentTimeMillis() - recentCallTime
                willWork = true
                if (spaceTime < minSpace) {
                    //与最近一次调用时间间隔过短，延时执行
                    mainHandler.postDelayed(this, minSpace - spaceTime)
                } else {
                    mainHandler.post(this)
                }
            }
        }

        override fun run() {
            recentCallTime = System.currentTimeMillis()
            mNotificationManager.notify(NOTIFICATION_ID, mNotification)
            willWork = false
            LogUtils.i(javaClass.simpleName, "run on $recentCallTime")
        }

    }

    private fun updateNotification() {
        notificationUpdater.update()
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

    fun loadVideoImage(imageUri: String) {
        loadImage(imageUri)
    }

    private var preLoad: FutureTarget<Bitmap>? = null
    private fun loadImage(imageUri: String) {
        preLoad?.let {
            if (!it.isDone && !it.isCancelled) {
                it.cancel(true)
                preLoad = null
            }
        }
        preLoad = Glide.with(context)
                .asBitmap()
                .load(imageUri)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .addListener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        normalRemoteViews.setImageViewResource(R.id.iv_image, R.drawable.icon_float_window)
                        bigRemoteViews.setImageViewResource(R.id.iv_image, R.drawable.icon_float_window)
                        updateNotification()
                        return true
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        return if (resource != null) {
                            normalRemoteViews.setImageViewBitmap(R.id.iv_image, resource)
                            bigRemoteViews.setImageViewBitmap(R.id.iv_image, resource)
                            updateNotification()
                            true
                        } else {
                            false
                        }
                    }

                })
                .submit()
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