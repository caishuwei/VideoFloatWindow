package com.csw.android.videofloatwindow.player.core

import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.video.IVideo
import com.csw.android.videofloatwindow.player.video.impl.exo.ExoVideoView
import com.csw.android.videofloatwindow.util.LogUtils
import java.util.*

class VideoInstanceManager {
    companion object {

        //VideoView的创建与回收
        private val videoMap = WeakHashMap<String, IVideo>()

        /**
         * 取得Video实例
         */
        fun ensureVideo(videoInfo: VideoInfo): IVideo {
            var instance = videoMap[videoInfo.target]
            if (instance == null) {
                instance = createVideo(videoInfo)
                videoMap[videoInfo.target] = instance
                LogUtils.i(VideoInstanceManager::class.java.simpleName, "videoMap size = " + videoMap.size)
            }
            return instance
        }

        private fun createVideo(videoInfo: VideoInfo): IVideo {
            return ExoVideoView(videoInfo)
        }

        /**
         * 添加Video实例
         */
        fun addVideo(video: IVideo): Boolean {
            val target = video.getVideoInfo().target
            if (videoMap.containsKey(target)) {
                return false
            }
            videoMap[target] = video
            return true
        }

        /**
         * 释放video实例
         */
        fun removeVideo(video: IVideo) {
            videoMap.remove(video.getVideoInfo().target)
            LogUtils.i(VideoInstanceManager::class.java.simpleName, "videoMap size = " + videoMap.size)
        }

        /**
         * 根据target查找现存的Video实例
         */
        fun findVideoByTarget(target: String): IVideo? {
            return videoMap[target]
        }

        /**
         * 判断是否存在实例
         */
        fun hasInstance(target: String?): Boolean {
            return if (target != null) {
                videoMap.contains(target)
            } else {
                false
            }
        }

    }
}