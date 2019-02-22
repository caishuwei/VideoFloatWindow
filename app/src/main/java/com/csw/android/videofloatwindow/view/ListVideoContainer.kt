package com.csw.android.videofloatwindow.view

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import com.csw.android.videofloatwindow.player.PlayerHelper
import com.csw.android.videofloatwindow.player.base.VideoContainer
import com.csw.android.videofloatwindow.util.LogUtils

class ListVideoContainer : VideoContainer {

    val whRatioImageView: WHRatioImageView
    var onVideoPlayListener: PlayerHelper.OnVideoPlayListener? = null
    val floatWindowClickListener = OnClickListener { _ ->
        tryPlayInWindow()
    }
    val fullScreenClickListener = OnClickListener {
        playInFullScreen()
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        isFocusable = true
        isFocusableInTouchMode = true
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
                    .setFloatWindowClickListener(floatWindowClickListener)
                    .setFullScreenClickListener(fullScreenClickListener)
                    .setOnVideoPlayListener(onVideoPlayListener)
        }
    }
//
//    override fun play(): VideoContainer {
//        val start = System.currentTimeMillis()
//        super.play()
//        LogUtils.e(msg = "playTime->${System.currentTimeMillis() - start}")
//        return this
//    }

}