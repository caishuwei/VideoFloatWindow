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

class ListVideoContainer : VideoContainer {

    val whRatioImageView: WHRatioImageView

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        whRatioImageView = WHRatioImageView(context)
        whRatioImageView.setOnClickListener {
            bindPlayer()
            play()
        }
        addView(whRatioImageView,
                0,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )
    }

    override fun onBindPlayer(playerBindHelper: PlayerHelper.PlayerBindHelper) {
        super.onBindPlayer(playerBindHelper)
        videoInfo?.let {
            playerBindHelper.setTitle(it.fileName)
                    .setFloatWindowClickListener(OnClickListener { _ ->
                        playInWindow()
                    })
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
                        Snackbar.make(this@ListVideoContainer, "没有悬浮窗权限", Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }
}