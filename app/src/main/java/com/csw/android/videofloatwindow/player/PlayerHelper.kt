package com.csw.android.videofloatwindow.player

import android.app.Application
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.ui.FullScreenActivity
import com.csw.android.videofloatwindow.view.VideoContainer
import com.csw.android.videofloatwindow.view.VideoFloatWindow
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.util.*

class PlayerHelper(context: Application) {
    private val none = VideoInfo()
    private val view: View = LayoutInflater.from(context).inflate(R.layout.view_player, null, false)
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

    private var videoFloatWindow: VideoFloatWindow? = null

    var playList: ArrayList<VideoInfo>? = null

    private val componentListener: ComponentListener

    private val playerListenerMap: WeakHashMap<PlayerListener, Any> = WeakHashMap()


    init {
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
        componentListener = ComponentListener()
        player.addListener(componentListener)
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

        val keys = playerListenerMap.keys
        if (!keys.isEmpty()) {
            for (key in keys) {
                key?.onVideoInfoChanged(currVideoInfo)
            }
        }
    }

    /**
     * 解绑播发器
     */
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

    /**
     * 尝试播放下一个
     */
    fun tryPlayNext(): Boolean {
        return tryPlay(getNext())
    }

    /**
     * 尝试播放上一个
     */
    fun tryPlayPrevious(): Boolean {
        return tryPlay(getPrevious())
    }

    /**
     * 尝试播放当前视频
     */
    fun tryPlayCurr(): Boolean {
        if (currVideoInfo != none) {
            return tryPlay(currVideoInfo)
        }
        return false
    }

    /**
     * 停止当前正在播放的视频
     */
    fun tryPauseCurr(): Boolean {
        if (currVideoInfo != none) {
            player.playWhenReady = false
        }
        return false
    }

    private fun tryPlay(newVideoInfo: VideoInfo?): Boolean {
        newVideoInfo?.let { videoInfo ->
            view.parent?.let { viewParent ->
                if (viewParent is View) {
                    viewParent.tag?.let {
                        if (it is VideoContainer) {
                            it.setVideoInfo(videoInfo).bindPlayer().play()
                            return true
                        }
                    }
                }
            }
        }
        return false
    }


    fun tryPlayInFullScreen(): Boolean {
        if (currVideoInfo != none) {
            FullScreenActivity.openActivity(MyApplication.instance, currVideoInfo)
            return true
        }
        return false
    }


    fun tryPlayInFloatWindow(): Boolean {
        if (currVideoInfo != none) {
            return playInFloatWindow(currVideoInfo)
        }
        return false
    }


    /**
     * 在悬浮窗中播放
     * @return true 悬浮窗显示并播放视频 else 悬浮窗播放失败
     */
    fun playInFloatWindow(videoInfo: VideoInfo = currVideoInfo): Boolean {
        val currWindow = videoFloatWindow
        return if (currWindow != null) {
            currWindow.setVideoInfo(videoInfo)
            showFloatWindow()
            true
        } else {
            false
        }
    }

    /**
     * 显示悬浮窗
     */
    fun showFloatWindow() {
        videoFloatWindow?.let {
            if (!it.isShowing()) {
                it.show()
                componentListener.onFloatWindowVisibilityChanged(true)
            }
        }
    }

    /**
     * 隐藏悬浮窗
     */
    fun hideFloatWindow() {
        videoFloatWindow?.let {
            if (it.isShowing()) {
                it.hide()
                componentListener.onFloatWindowVisibilityChanged(false)
            }
        }
    }

    /**
     * 有上一曲
     */
    fun hasPrevious(): Boolean {
        return getPrevious() != null
    }

    /**
     * 获取上一个
     */
    fun getPrevious(): VideoInfo? {
        playList?.let {
            for ((index, vi) in it.withIndex()) {
                if (isCurrVideo(vi)) {
                    if (index - 1 >= 0) {
                        return it[index - 1]
                    }
                }
            }
        }
        return null
    }

    /**
     * 有下一曲
     */
    fun hasNext(): Boolean {
        return getNext() != null
    }

    /**
     * 获取下一个
     */
    fun getNext(): VideoInfo? {
        playList?.let {
            for ((index, vi) in it.withIndex()) {
                if (isCurrVideo(vi)) {
                    if (index + 1 < it.size) {
                        return it[index + 1]
                    }
                }
            }
        }
        return null
    }

    /**
     * 设置悬浮窗
     */
    fun setVideoFloatWindow(value: VideoFloatWindow?) {
        val old = videoFloatWindow
        if (old === value) {
            return
        }
        old?.hide()
        videoFloatWindow = value
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

    fun addPlayerListener(listener: PlayerListener) {
        playerListenerMap[listener] = null
    }

    fun removePlayerListener(listener: PlayerListener) {
        playerListenerMap.remove(listener)
    }


    inner class PlayerBindHelper {

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

    private inner class ComponentListener : PlayerListener() {

        override fun onVideoInfoChanged(newVideoInfo: VideoInfo) {
            super.onVideoInfoChanged(newVideoInfo)
            val keys = playerListenerMap.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    key?.onVideoInfoChanged(newVideoInfo)
                }
            }
        }

        override fun onFloatWindowVisibilityChanged(isVisibility: Boolean) {
            super.onFloatWindowVisibilityChanged(isVisibility)
            val keys = playerListenerMap.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    key?.onFloatWindowVisibilityChanged(isVisibility)
                }
            }
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            val keys = playerListenerMap.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    key?.onPlaybackParametersChanged(playbackParameters)
                }
            }
        }

        override fun onSeekProcessed() {
            val keys = playerListenerMap.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    key?.onSeekProcessed()
                }
            }
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            val keys = playerListenerMap.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    key?.onTracksChanged(trackGroups, trackSelections)
                }
            }
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            val keys = playerListenerMap.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    key?.onPlayerError(error)
                }
            }

            //播放遇到错误了，尝试播放下一个
            tryPlayNext()
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            val keys = playerListenerMap.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    key?.onLoadingChanged(isLoading)
                }
            }
        }

        override fun onPositionDiscontinuity(reason: Int) {
            val keys = playerListenerMap.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    key?.onPositionDiscontinuity(reason)
                }
            }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            val keys = playerListenerMap.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    key?.onRepeatModeChanged(repeatMode)
                }
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            val keys = playerListenerMap.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    key?.onShuffleModeEnabledChanged(shuffleModeEnabled)
                }
            }
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            val keys = playerListenerMap.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    key?.onTimelineChanged(timeline, manifest, reason)
                }
            }
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            val keys = playerListenerMap.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    key?.onPlayerStateChanged(playWhenReady, playbackState)
                }
            }

            //自动播放下一个视频
            when (playbackState) {
                Player.STATE_IDLE -> {
                }
                Player.STATE_BUFFERING -> {
                }
                Player.STATE_READY -> {
                    view.keepScreenOn = playWhenReady
                }
                Player.STATE_ENDED -> {
                    view.keepScreenOn = false
                    tryPlayNext()

                }
            }
        }
    }


}

