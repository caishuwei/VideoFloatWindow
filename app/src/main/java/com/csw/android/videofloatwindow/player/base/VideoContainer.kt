package com.csw.android.videofloatwindow.player.base

import android.content.Context
import android.content.pm.ActivityInfo
import android.util.AttributeSet
import android.widget.FrameLayout
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayerHelper
import com.csw.android.videofloatwindow.ui.FullScreenActivity
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
        videoContainer.tag = this
        super.addView(videoContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        super.setBackgroundColor(0xFF000000.toInt())
    }

    var videoInfo: VideoInfo? = null
        private set


    open fun bindPlayer(): VideoContainer {
        MyApplication.instance.playerHelper.bindPlayer(videoContainer) {
            onBindPlayer(it)
        }
        return this
    }

    open fun unBindPlayer(): VideoContainer {
        MyApplication.instance.playerHelper.unBindPlayer(videoContainer)
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


    open fun setVideoInfo(videoInfo: VideoInfo?): VideoContainer {
        if (this.videoInfo != videoInfo) {
            unBindPlayer();
        }
        this.videoInfo = videoInfo
        tryRotateScreen()
        return this
    }

    /**
     * 根据视频的比例，在全屏播放的情况下，长的视频设置为竖屏播放，宽的视频设置为横屏播放
     */
    private fun tryRotateScreen() {
        Utils.runIfNotNull(context, videoInfo) { c, v ->
            if (c is FullScreenActivity) {
                c.requestedOrientation = if (v.whRatio > 1) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

}