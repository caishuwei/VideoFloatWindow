package com.csw.android.videofloatwindow.player

import android.app.Application
import android.net.Uri
import android.os.Debug
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.view.View
import android.view.ViewGroup
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.base.PlayerListener
import com.csw.android.videofloatwindow.player.base.VideoContainer
import com.csw.android.videofloatwindow.player.video.CustomVideoView
import com.csw.android.videofloatwindow.player.window.VideoFloatWindow
import com.csw.android.videofloatwindow.ui.FullScreenActivity
import com.csw.android.videofloatwindow.util.LogUtils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.util.*
import kotlin.collections.HashMap

class PlayerHelper(context: Application) {
    private val none = VideoInfo()
    private val view: CustomVideoView = CustomVideoView(MyApplication.instance)
    private val player: SimpleExoPlayer
    private val mediaDataSourceFactory: DefaultDataSourceFactory

    private val playerBindHelper: PlayerBindHelper

    private var currVideoInfo: VideoInfo = none

    private val videoFloatWindow: VideoFloatWindow = VideoFloatWindow(MyApplication.instance)

    private var maxIndex = 0
    private var currIndex = Int.MIN_VALUE
    private var videoPos = HashMap<String, Int>()
    var playList: ArrayList<VideoInfo>? = null
        set(value) {
            field = value
            videoPos.clear()
            if (value != null) {
                maxIndex = value.size - 1
                for ((i, vi) in value.withIndex()) {
                    videoPos[vi.filePath] = i
                }
                updateCurrIndex()
            } else {
                maxIndex = -1
                currIndex = Int.MIN_VALUE
            }
        }

    private val componentListener: ComponentListener

    private val playerListenerMap: WeakHashMap<PlayerListener, Any> = WeakHashMap()


    init {
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        player = view.player
        componentListener = ComponentListener()
        player.addListener(componentListener)
        playerBindHelper = PlayerBindHelper()
        val bandwidthMeter = DefaultBandwidthMeter()
        mediaDataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.getString(R.string.app_name)),
                bandwidthMeter)
    }

    /**
     * 绑定播放器
     */
    fun bindPlayer(container: ViewGroup, executeBind: (playerBindHelper: PlayerBindHelper) -> (Unit)) {
        //更换显示容器
        val currVideoParent = view.parent
        if (currVideoParent !== container) {
            if (currVideoParent is ViewGroup) {
                val tag = currVideoParent.tag
                if (tag is VideoContainer) {
                    tag.unBindPlayer()
                } else {
                    currVideoParent.removeView(view)
                }
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

    /**
     * 解绑播发器
     */
    fun unBindPlayer(container: ViewGroup) {
        if (view.parent === container) {
//            Debug.startMethodTracing(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).absolutePath + "/shixintrace.trace")
            val start = System.currentTimeMillis()
            resetBind()
            container.removeView(view)
            LogUtils.e(msg = "bindPlayerTime->${System.currentTimeMillis() - start}")
//            Debug.stopMethodTracing()
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
        tryPlay(videoInfo)
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
            return true
        }
        return false
    }

    private fun tryPlay(newVideoInfo: VideoInfo?): Boolean {
        newVideoInfo?.let { videoInfo ->
            var isVideoChanged = false
            if (!isCurrVideo(newVideoInfo)) {
                //更新播放源
                val mediaSource = ExtractorMediaSource.Factory(mediaDataSourceFactory)
                        .createMediaSource(Uri.parse(videoInfo.filePath))
                player.prepare(mediaSource, true, true)
                currVideoInfo = videoInfo
                isVideoChanged = true
            } else {
                //仍旧是当前的视频，如果当前视频已经播放完毕，那么我们跳到视频开头，重新播放
                when (player.playbackState) {
                    Player.STATE_IDLE -> {
                        val mediaSource = ExtractorMediaSource.Factory(mediaDataSourceFactory)
                                .createMediaSource(Uri.parse(videoInfo.filePath))
                        player.prepare(mediaSource, true, true)
                    }
                    Player.STATE_ENDED -> {
                        player.seekTo(0)
                    }
                }
            }
            player.playWhenReady = true

            //通知播放的视频已经发生改变
            if (isVideoChanged) {
                updateCurrIndex()
                val keys = playerListenerMap.keys
                if (!keys.isEmpty()) {
                    for (key in keys) {
                        key?.onVideoInfoChanged(currVideoInfo)
                    }
                }
            }
            return true
        }
        return false
    }

    private fun updateCurrIndex() {
        val ci = videoPos[currVideoInfo.filePath]
        currIndex = if (ci == null) {
            Int.MIN_VALUE
        } else {
            ci
        }
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
        videoFloatWindow.show()
        return if (videoFloatWindow.isShowing()) {
            videoFloatWindow.setVideoInfo(videoInfo)
            true
        } else {
            false
        }
    }

    /**
     * 隐藏悬浮窗
     */
    fun hideFloatWindow() {
        videoFloatWindow.hide()
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
            if (currIndex - 1 >= 0 && currIndex - 1 <= maxIndex) {
                return it[currIndex - 1]
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
            if (currIndex + 1 >= 0 && currIndex + 1 <= maxIndex) {
                return it[currIndex + 1]
            }
        }
        return null
    }

    /**
     * 获取上一个
     */
    fun getCurrent(): VideoInfo? {
        if (isCurrVideo(currVideoInfo)) {
            return currVideoInfo
        }
        return null
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
                .setVolumeAndBrightnessControllerEnable(false)
                .setOnVideoPlayListener(null)
    }

    fun addPlayerListener(listener: PlayerListener) {
        playerListenerMap[listener] = null
    }

    fun removePlayerListener(listener: PlayerListener) {
        playerListenerMap.remove(listener)
    }

    fun dispatchFloatWindowVisibleChanged(visible: Boolean) {
        componentListener.onFloatWindowVisibilityChanged(visible)
    }

    inner class PlayerBindHelper {
        var onVideoPlayListener: OnVideoPlayListener? = null

        /**
         * 设置返回按钮事件
         */
        fun setBackClickListener(listener: View.OnClickListener?): PlayerBindHelper {
            return setClickListener(view.vBack, listener)
        }

        /**
         * 设置标题
         */
        fun setTitle(titleStr: String): PlayerBindHelper {
            view.tvTitle.text = titleStr
            return this
        }

        /**
         * 设置关闭按钮事件
         */
        fun setCloseClickListener(listener: View.OnClickListener?): PlayerBindHelper {
            return setClickListener(view.vClose, listener)
        }

        /**
         * 设置上一首事件
         */
        fun setPreviousClickListener(listener: View.OnClickListener?): PlayerBindHelper {
            return setClickListener(view.vPrevious, listener)
        }

        /**
         * 设置上一首事件
         */
        fun setNextClickListener(listener: View.OnClickListener?): PlayerBindHelper {
            return setClickListener(view.vNext, listener)
        }

        /**
         * 设置全屏按钮事件
         */
        fun setFullScreenClickListener(listener: View.OnClickListener?): PlayerBindHelper {
            return setClickListener(view.vFullScreen, listener)
        }

        /**
         * 设置小窗口播放按钮事件
         */
        fun setFloatWindowClickListener(listener: View.OnClickListener?): PlayerBindHelper {
            return setClickListener(view.vFloatWindow, listener)
        }

        /**
         * 启用音量与亮度控制
         */
        fun setVolumeAndBrightnessControllerEnable(enable: Boolean): PlayerBindHelper {
            view.enableVolumeAndBrightnessController = enable
            return this
        }

        fun setOnVideoPlayListener(listener: OnVideoPlayListener?): PlayerBindHelper {
            onVideoPlayListener = listener
            return this
        }

        private fun setClickListener(view: View, listener: View.OnClickListener?): PlayerBindHelper {
            view.setOnClickListener(listener)
            view.visibility = if (listener == null) View.GONE else View.VISIBLE
            return this
        }
    }

    /**
     * 视频播放监听
     */
    interface OnVideoPlayListener {
        /**
         * 视频播放结束
         * @return true 表示自己处理播放结束后的事件，false 默认处理（尝试播放下一个）
         */
        fun onVideoPlayCompleted(videoInfo: VideoInfo): Boolean
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
                    val listener = playerBindHelper.onVideoPlayListener
                    if (listener == null
                            || !listener.onVideoPlayCompleted(currVideoInfo)) {
                        tryPlayNext()
                    }
                }
            }
        }
    }


}

