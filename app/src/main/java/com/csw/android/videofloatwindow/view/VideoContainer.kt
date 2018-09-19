package com.csw.android.videofloatwindow.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayerHelper
import com.csw.android.videofloatwindow.util.Utils

open class VideoContainer : FrameLayout {
    private val videoContainer: FrameLayout;
    var whRatio: Float = 16f / 9
        set(value) {
            var ratio = value
            if (ratio <= 0) {
                ratio = 16f / 9
            }
            if (whRatio != ratio) {
                field = ratio
                requestLayout()
            }
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        videoContainer = FrameLayout(context)
        super.addView(videoContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        super.setBackgroundColor(0xFF000000.toInt())
    }

    var videoInfo: VideoInfo? = null
        private set


    open fun bindPlayer(): VideoContainer {
        videoInfo?.let {
            MyApplication.instance.playerHelper.bindPlayer(videoContainer, it) {
                onBindPlayer(it)
            }
        }
        return this
    }

    open fun unBindPlayer(): VideoContainer {
        videoInfo?.let {
            MyApplication.instance.playerHelper.unBindPlayer(videoContainer, it)
        }
        return this
    }

    open fun onBindPlayer(playerBindHelper: PlayerHelper.PlayerBindHelper) {
    }

    open fun play(): VideoContainer {
        videoInfo?.let {
            MyApplication.instance.playerHelper.play(it)
        }
        return this
    }

    open fun stop(): VideoContainer {
        videoInfo?.let {
            MyApplication.instance.playerHelper.stop(it)
        }
        return this
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Utils.measureCenterInsideByScaleRatio(widthMeasureSpec, heightMeasureSpec, whRatio) { w, h ->
            super.onMeasure(w, h)
        }
    }


    fun setVideoInfo(videoInfo: VideoInfo?): VideoContainer {
        if (this.videoInfo != videoInfo) {
            unBindPlayer();
        }
        this.videoInfo = videoInfo
        return this
    }

}