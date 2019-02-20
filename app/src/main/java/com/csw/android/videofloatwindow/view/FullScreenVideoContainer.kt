package com.csw.android.videofloatwindow.view

import android.app.Activity
import android.content.Context
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.view.View.OnClickListener
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.permission.SystemAlertWindowPermission
import com.csw.android.videofloatwindow.player.PlayerHelper
import com.csw.android.videofloatwindow.player.base.VideoContainer
import com.csw.android.videofloatwindow.ui.FullScreenActivity

class FullScreenVideoContainer : VideoContainer {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onBindPlayer(playerBindHelper: PlayerHelper.PlayerBindHelper) {
        super.onBindPlayer(playerBindHelper)
        videoInfo?.let {
            playerBindHelper.setTitle(it.fileName)
                    .setBackClickListener(OnClickListener { _ ->
                        finishActivity()
                        unBindPlayer()
                    })
                    .setFloatWindowClickListener(OnClickListener { _ ->
                        playInWindow()
                    })
                    .setPreviousClickListener(
                            if (MyApplication.instance.playerHelper.hasPrevious()) {
                                OnClickListener { _ ->
                                    playPre()
                                }
                            } else {
                                null
                            }
                    )
                    .setNextClickListener(
                            if (MyApplication.instance.playerHelper.hasNext()) {
                                OnClickListener { _ ->
                                    playNext()
                                }
                            } else {
                                null
                            }
                    )
                    .enableVolumeAndBrightnessController(true)
        }

    }

    private fun playNext() {
        val nextVideo = MyApplication.instance.playerHelper.getNext()
        if (nextVideo != null) {
            setVideoInfo(nextVideo).bindPlayer().play()
        }
    }

    private fun playPre() {
        val preVideo = MyApplication.instance.playerHelper.getPrevious()
        if (preVideo != null) {
            setVideoInfo(preVideo).bindPlayer().play()
        }
    }

    private fun finishActivity() {
        val activity = context
        if (activity is Activity) {
            activity.finish()
        }
    }

    private fun playInWindow() {
        val activity = context
        if (activity is FullScreenActivity) {
            SystemAlertWindowPermission.request(activity, object : SystemAlertWindowPermission.OnRequestResultListener {
                override fun onResult(isGranted: Boolean) {
                    if (isGranted) {
                        videoInfo?.let {
                            MyApplication.instance.playerHelper.playInFloatWindow(it)
                            finishActivity()
                        }
                    } else {
                        Snackbar.make(this@FullScreenVideoContainer, "没有悬浮窗权限", Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }
}