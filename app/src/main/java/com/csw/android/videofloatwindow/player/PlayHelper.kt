package com.csw.android.videofloatwindow.player

import android.os.Handler
import android.os.Looper
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.container.impl.VideoContainer
import com.csw.android.videofloatwindow.player.core.VideoInstanceManager
import com.csw.android.videofloatwindow.player.service.PlayService
import com.csw.android.videofloatwindow.player.video.IVideo
import com.csw.android.videofloatwindow.player.container.impl.FloatWindowVideoContainer
import com.csw.android.videofloatwindow.player.window.VideoFloatWindow
import com.csw.android.videofloatwindow.ui.video.full_screen.FullScreenActivity
import com.csw.android.videofloatwindow.player.container.impl.FullScreenVideoContainer
import java.lang.ref.WeakReference
import java.util.*

/**
 * 播放帮助类，作为视频播放视图的创建、销毁决策实现，提供全局播放下一个、上一个、视频播放停止等方法
 */
class PlayHelper {

    companion object {
        //单一前台的VideoContainer，如全屏视频或者悬浮窗视频，其可见时设置到这里，用于在通过通知切换下一首时可以通过这里绑定到容器中
        private var topLevelVideoContainer: WeakReference<VideoContainer>? = null

        //播放结束处理器
        private val playEndHandler = PlayEndHandler()

        //播放状态改变监听器集合
        private val onPlayVideoChangeListenerSet = HashSet<OnPlayChangeListener>()

        //顶层视频容器变化监听器集合
        private val onTopLevelVideoContainerChangeListenerSet = HashSet<OnTopLevelVideoContainerChangeListener>()

        //主消息循环处理器
        private val mainHandler = Handler(Looper.getMainLooper())

        init {
            addOnPlayVideoChangeListener(playEndHandler)
        }

        /**
         * 设置顶层视频容器
         */
        fun setTopLevelVideoContainer(videoContainer: VideoContainer) {
            if (videoContainer is FullScreenVideoContainer || videoContainer is FloatWindowVideoContainer) {
                topLevelVideoContainer = WeakReference(videoContainer)
                noticeTopLevelVideoContainerChanged()
            }
        }

        /**
         * 移除顶层视频容器（需要校验要移除的是否是当前的容器，如果不是，则没有移除权限）
         */
        fun removeTopLevelVideoContainer(videoContainer: VideoContainer) {
            if (videoContainer is FullScreenVideoContainer || videoContainer is FloatWindowVideoContainer) {
                topLevelVideoContainer?.let {
                    if (it.get() == videoContainer) {
                        topLevelVideoContainer = null
                        noticeTopLevelVideoContainerChanged()
                    }
                }
            }
        }

        private fun noticeTopLevelVideoContainerChanged() {
            for (listener in onTopLevelVideoContainerChangeListenerSet) {
                listener.onTopLevelVideoContainerChanged(getTopLevelVideoContainer())
            }
        }

        /**
         * 取得顶层视频容器
         */
        fun getTopLevelVideoContainer(): VideoContainer? {
            return topLevelVideoContainer?.get()
        }

        /**
         * 添加顶层容器变化监听
         */
        fun addOnTopLevelVideoContainerChangeListener(onTopLevelVideoContainerChangeListener: OnTopLevelVideoContainerChangeListener) {
            onTopLevelVideoContainerChangeListenerSet.add(onTopLevelVideoContainerChangeListener)
        }

        /**
         * 移除顶层容器变化监听
         */
        fun removeOnTopLevelVideoContainerChangeListener(onTopLevelVideoContainerChangeListener: OnTopLevelVideoContainerChangeListener) {
            onTopLevelVideoContainerChangeListenerSet.remove(onTopLevelVideoContainerChangeListener)
        }

        /**
         * 最后一个播放的VideoView
         */
        var lastPlayVideo: IVideo? = null
            set(value) {
                if (field !== value) {
                    val old = field
                    field = value
                    //上个播放的视频，如果在容器中就停止播放，否则将其释放掉
                    old?.let {
                        if (it.inContainer()) {
                            it.pause()
                        } else {
                            it.release()
                        }
                    }
                    PlayList.updateCurrIndex()
                    for (listener in onPlayVideoChangeListenerSet) {
                        listener.onPlayVideoInfoUpdated(field?.getVideoInfo())
                    }
                }
            }

        /**
         * 后台播放
         */
        var backgroundPlay = false
            set(value) {
                field = value
                if (field) {
                    PlayService.startService(MyApplication.instance)
                } else {
                    PlayService.stopService(MyApplication.instance)
                }
            }

        /**
         * 判断VideoView是否该回收了,
         */
        fun isVideoViewCanBackgroundPlay(video: IVideo): Boolean {
            if (backgroundPlay && lastPlayVideo == video) {
                return true
            }
            return false
        }

        /**
         * 播放状态发生改变时调用
         */
        fun onVideoViewPlayStateChanged(video: IVideo) {
            if (lastPlayVideo == video) {
                for (listener in onPlayVideoChangeListenerSet) {
                    listener.onPlayVideoInfoUpdated(video.getVideoInfo())
                }
            }
        }

        /**
         * 播放的歌曲发生改变时调用
         */
        fun onVideoViewVideoInfoChanged(video: IVideo) {
            if (lastPlayVideo == video) {
                for (listener in onPlayVideoChangeListenerSet) {
                    listener.onPlayVideoInfoUpdated(video.getVideoInfo())
                }
            }
        }

        //播放结束处理---------------------------------------------------------------------------

        fun setOnPlayEndHandler(onPlayEndHandler: OnPlayEndHandler?) {
            if (onPlayEndHandler == null) {
                playEndHandler.onPlayEndHandler = playEndHandler.defaultPlayEndHandler
            } else {
                playEndHandler.onPlayEndHandler = onPlayEndHandler
            }
        }

        //播放状态变化监听-----------------------------------------------------------------------
        fun addOnPlayVideoChangeListener(listener: OnPlayChangeListener) {
            onPlayVideoChangeListenerSet.add(listener)
        }

        fun removeOnPlayVideoChangeListener(listener: OnPlayChangeListener) {
            onPlayVideoChangeListenerSet.remove(listener)
        }

        //播放控制-----------------------------------------------------------------------------
        /**
         * 尝试播放下一个
         */
        fun tryPlayNext(): Boolean {
            val next = PlayList.getNext()
            if (next != null) {
                val currVideoContainer = topLevelVideoContainer?.get()
                if (currVideoContainer != null) {
                    currVideoContainer.setVideoInfo(next)
                    currVideoContainer.play()
                } else {
                    VideoInstanceManager.ensureVideo(next).play()
                }
                return true
            }
            return false
        }

        /**
         * 尝试播放上一个
         */
        fun tryPlayPrevious(): Boolean {
            val pre = PlayList.getPrevious()
            if (pre != null) {
                val currVideoContainer = topLevelVideoContainer?.get()
                if (currVideoContainer != null) {
                    currVideoContainer.setVideoInfo(pre)
                    currVideoContainer.play()
                } else {
                    VideoInstanceManager.ensureVideo(pre).play()
                }
                return true
            }
            return false
        }

        /**
         * 尝试播放当前视频
         */
        fun tryPlayCurr(): Boolean {
            lastPlayVideo?.let {
                it.play()
                return true
            }
            return false
        }

        /**
         * 停止当前正在播放的视频
         */
        fun tryPauseCurr(): Boolean {
            lastPlayVideo?.let {
                it.pause()
                return true
            }
            return false
        }

        /**
         * 尝试在全屏界面播放当前视频
         */
        fun tryPlayInFullScreen(): Boolean {
            lastPlayVideo?.let {
                FullScreenActivity.openActivity(MyApplication.instance, it.getVideoInfo())
                return true
            }
            return false
        }

        /**
         * 尝试在悬浮窗播放当前视频
         */
        fun tryPlayInFloatWindow(): Boolean {
            lastPlayVideo?.let {
                if (VideoFloatWindow.instance.show()) {
                    VideoFloatWindow.instance.setVideoInfo(it.getVideoInfo())
                    return true
                }
            }
            return false
        }


    }

    /**
     * 播放变化监听
     */
    interface OnPlayChangeListener {

        /**
         * 播放的视频信息更新
         */
        fun onPlayVideoInfoUpdated(videoInfo: VideoInfo?)
    }

    /**
     * 播放结束处理
     */
    interface OnPlayEndHandler {
        fun handlePlayEnd(videoInfo: VideoInfo?)
    }

    private class PlayEndHandler : OnPlayChangeListener {
        //默认播放结束则播放列表下一个
        val defaultPlayEndHandler = object : OnPlayEndHandler {
            override fun handlePlayEnd(videoInfo: VideoInfo?) {
                lastPlayVideo?.let {
                    mainHandler.post {
                        tryPlayNext()
                    }
                }
            }
        }
        var onPlayEndHandler = defaultPlayEndHandler
        override fun onPlayVideoInfoUpdated(videoInfo: VideoInfo?) {
            lastPlayVideo?.let {
                if (it.isEnd()) {
                    onPlayEndHandler.handlePlayEnd(videoInfo)
                }
            }
        }
    }

    /**
     * 顶层视频容器变化监听
     */
    interface OnTopLevelVideoContainerChangeListener {
        /**
         * 顶层视频容器发生变化
         */
        fun onTopLevelVideoContainerChanged(videoContainer: VideoContainer?)
    }
}