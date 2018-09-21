package com.csw.android.videofloatwindow.player

import android.app.Application
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class PlayerHelper(context: Application) {
    private val none = VideoInfo()
    private val view: View
    private val playerView: PlayerView
    private val vBack: View
    private val tvTitle: TextView
    private var vClose: View
    private val vFullScreen: View
    private val vFloatWindow: View
    private val vPrevious: View
    private val vNext: View

    private val player: SimpleExoPlayer
    private val mediaDataSourceFactory: DefaultDataSourceFactory

    private val playerBindHelper: PlayerBindHelper

    private var currVideoInfo: VideoInfo = none



    init {
        view = LayoutInflater.from(context).inflate(R.layout.view_player, null, false)
        playerView = view.findViewById(R.id.player_view)
        vBack = playerView.findViewById(R.id.v_back)
        tvTitle = playerView.findViewById(R.id.tv_title)
        vClose = playerView.findViewById(R.id.v_close)
        vPrevious = playerView.findViewById(R.id.v_previous)
        vPrevious.isEnabled = true
        vNext = playerView.findViewById(R.id.v_next)
        vNext.isEnabled = true
        vFullScreen = playerView.findViewById(R.id.v_full_screen)
        vFloatWindow = playerView.findViewById(R.id.v_float_window)
        playerBindHelper = PlayerBindHelper()
        val bandwidthMeter = DefaultBandwidthMeter()
        //        val window = Timeline.Window()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(
                context,
                trackSelector)
        mediaDataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.getString(R.string.app_name)),
                bandwidthMeter)
        playerView.player = player
    }

    /**
     * 绑定播放器
     */
    fun bindPlayer(container: ViewGroup, videoInfo: VideoInfo, executeBind: (playerBindHelper: PlayerBindHelper) -> (Unit)) {
        //更换播放源
        if (videoInfo.filePath != currVideoInfo.filePath) {
            val mediaSource = ExtractorMediaSource.Factory(mediaDataSourceFactory)
                    .createMediaSource(Uri.parse(videoInfo.filePath))
            player.prepare(mediaSource, true, true)
        }
        currVideoInfo = videoInfo
        //更换显示容器
        val currVideoParent = view.parent
        if (currVideoParent !== container) {
            if (currVideoParent is ViewGroup) {
                currVideoParent.removeView(view)
            }
            container.addView(
                    view,
                    ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            )
        }
        resetBind()
        executeBind(playerBindHelper)
    }

    fun unBindPlayer(container: ViewGroup, videoInfo: VideoInfo) {
        if (isCurrVideo(videoInfo) && view.parent === container) {
            container.removeView(view)
            resetBind()
        }
    }

    /**
     * 停止播放
     */
    fun stop(videoInfo: VideoInfo = currVideoInfo) {
        if (isCurrVideo(videoInfo)) {
            player.playWhenReady = false
        }
    }

    /**
     * 开始播放
     */
    fun play(videoInfo: VideoInfo = currVideoInfo) {
        if (isCurrVideo(videoInfo)) {
            player.playWhenReady = true
        }
    }

    private fun isCurrVideo(videoInfo: VideoInfo): Boolean {
        return videoInfo !== none && videoInfo.filePath == currVideoInfo.filePath
    }

    private fun resetBind() {
        playerBindHelper.setBackClickListener(null)
                .setBackClickListener(null)
                .setTitle("")
                .setCloseClickListener(null)
                .setPreviousClickListener(null)
                .setNextClickListener(null)
                .setFullScreenClickListener(null)
                .setFloatWindowClickListener(null)
    }

    inner class PlayerBindHelper() {

        /**
         * 设置返回按钮事件
         */
        fun setBackClickListener(listener: View.OnClickListener?): PlayerBindHelper {
            return setClickListener(vBack, listener)
        }

        /**
         * 设置标题
         */
        fun setTitle(titleStr: String): PlayerBindHelper {
            tvTitle.text = titleStr
            return this
        }

        /**
         * 设置关闭按钮事件
         */
        fun setCloseClickListener(listener: View.OnClickListener?): PlayerBindHelper {
            return setClickListener(vClose, listener)
        }

        /**
         * 设置上一首事件
         */
        fun setPreviousClickListener(listener: View.OnClickListener?): PlayerBindHelper {
            return setClickListener(vPrevious, listener)
        }

        /**
         * 设置上一首事件
         */
        fun setNextClickListener(listener: View.OnClickListener?): PlayerBindHelper {
            return setClickListener(vNext, listener)
        }

        /**
         * 设置全屏按钮事件
         */
        fun setFullScreenClickListener(listener: View.OnClickListener?): PlayerBindHelper {
            return setClickListener(vFullScreen, listener)
        }

        /**
         * 设置小窗口播放按钮事件
         */
        fun setFloatWindowClickListener(listener: View.OnClickListener?): PlayerBindHelper {
            return setClickListener(vFloatWindow, listener)
        }

        private fun setClickListener(view: View, listener: View.OnClickListener?): PlayerBindHelper {
            view.setOnClickListener(listener)
            view.visibility = if (listener == null) View.GONE else View.VISIBLE
            return this
        }

    }
}

