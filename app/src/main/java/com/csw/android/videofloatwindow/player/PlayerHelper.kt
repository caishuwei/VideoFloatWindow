package com.csw.android.videofloatwindow.player

import android.app.Application
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.base.PlayerListener
import com.csw.android.videofloatwindow.player.video.CustomVideoView
import com.csw.android.videofloatwindow.player.window.VideoFloatWindow
import com.csw.android.videofloatwindow.ui.FullScreenActivity
import com.csw.android.videofloatwindow.util.Utils
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import java.util.*
import kotlin.collections.HashMap

/**
 * 播放辅助类，实现播放列表功能，通知状态同步
 */
class PlayerHelper(context: Application) {
    /**
     * 设置当前播放的VideoView，实现同一时间只有一个视频在播放
     */
    var currVideoView: CustomVideoView? = null
        set(value) {
            if (field != value) {
                field?.let {
                    it.pause()
                    it.player.removeListener(componentListener)
                }
                value?.let {
                    it.player.addListener(componentListener)
                }
                field = value
            }
        }
    private val videoFloatWindow: VideoFloatWindow = VideoFloatWindow(MyApplication.instance)
    private var maxIndex = 0
    private var currIndex = Int.MIN_VALUE
    private var videoPos = HashMap<String, Int>()
    /**
     * 设置播放列表
     */
    var playList: ArrayList<VideoInfo>? = null
        set(value) {
            field = value
            videoPos.clear()
            if (value != null) {
                maxIndex = value.size - 1
                for ((i, vi) in value.withIndex()) {
                    videoPos[vi.target] = i
                }
                updateCurrIndex()
            } else {
                maxIndex = -1
                currIndex = Int.MIN_VALUE
            }
        }

    val componentListener = ComponentListener()
    private val playerListenerMap: WeakHashMap<PlayerListener, Any> = WeakHashMap()


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
        currVideoView?.let {
            it.play()
            return true
        }
        return false
    }

    /**
     * 停止当前正在播放的视频
     */
    fun tryPauseCurr(): Boolean {
        currVideoView?.let {
            it.pause()
            return true
        }
        return false
    }

    private fun tryPlay(newVideoInfo: VideoInfo?): Boolean {
        var result: Boolean
        Utils.runIfNotNull(newVideoInfo, currVideoView) { vi, vv ->
            if (!isCurrVideo(vi)) {
                vv.videoInfo = vi
                updateCurrIndex()
                val keys = playerListenerMap.keys
                if (!keys.isEmpty()) {
                    for (key in keys) {
                        key?.onVideoInfoChanged(vi)
                    }
                }
            }
            vv.play()
            result = true
        }
        result = false
        return result
    }

    private fun updateCurrIndex() {
        currVideoView?.let {
            val ci = videoPos[it.videoInfo.target]
            currIndex = if (ci == null) {
                Int.MIN_VALUE
            } else {
                ci
            }
            return
        }
        currIndex = Int.MIN_VALUE
    }

    fun tryPlayInFullScreen(): Boolean {
        currVideoView?.let {
            FullScreenActivity.openActivity(MyApplication.instance, it.videoInfo)
            return true
        }
        return false
    }


    fun tryPlayInFloatWindow(): Boolean {
        currVideoView?.let {
            FullScreenActivity.openActivity(MyApplication.instance, it.videoInfo)
            return playInFloatWindow(it.videoInfo)
        }
        return false
    }


    /**
     * 在悬浮窗中播放
     * @return true 悬浮窗显示并播放视频 else 悬浮窗播放失败
     */
    fun playInFloatWindow(videoInfo: VideoInfo): Boolean {
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
     * 获取当前播放的视频
     */
    fun getCurrent(): VideoInfo? {
        return currVideoView?.videoInfo
    }

    private fun isCurrVideo(videoInfo: VideoInfo): Boolean {
        currVideoView?.let {
            return Utils.videoEquals(videoInfo, it.videoInfo)
        }
        return false
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

    inner class ComponentListener : PlayerListener() {

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
                }
                Player.STATE_ENDED -> {
//                    val listener = playerBindHelper.onVideoPlayListener
//                    if (listener == null
//                            || !listener.onPlayCompleted(currVideoInfo)) {
//                    tryPlayNext()
//                    }
                }
            }
        }
    }


}

