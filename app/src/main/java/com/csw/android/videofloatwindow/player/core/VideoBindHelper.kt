package com.csw.android.videofloatwindow.player.core

import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.PlayList
import com.csw.android.videofloatwindow.player.container.impl.VideoContainer
import com.csw.android.videofloatwindow.player.video.IVideo
import com.csw.android.videofloatwindow.util.Utils

/**
 * 辅助VideoContainer绑定Video
 */
class VideoBindHelper {

    companion object {

        /**
         * 为容器绑定Video实例
         */
        fun bindVideo(videoContainer: VideoContainer) {
            if (videoContainer.getVideo() != null) {
                return
            }
            val videoInfo = videoContainer.getVideoInfo() ?: return

            val video = VideoInstanceManager.ensureVideo(videoInfo)
            video.getVideoContainer()?.let {
                if (it === videoContainer) return
                unBindVideo(it)
            }
            video.setVideoContainer(videoContainer)
            videoContainer.onVideoBind(video)
            videoContainer.onPlayControllerSetup(video.getControllerSettingHelper())
        }


        /**
         * 容器解绑Video实例
         */
        fun unBindVideo(videoContainer: VideoContainer) {
            videoContainer.getVideo()?.let {
                videoContainer.onVideoUnbind(it)
                it.getControllerSettingHelper().reset()
                it.setVideoContainer(null)
            }
        }

        /**
         * 同步VideoInfo到Video中
         */
        fun syncVideoInfoToVideo(newVideoInfo: VideoInfo?, video: IVideo?) {
            if (video == null) {
                return
            }

            if (newVideoInfo == null) {
                video.getVideoContainer()?.let {
                    unBindVideo(it)
                }
            } else {
                val oldVideoInfo = video.getVideoInfo()
                if (!Utils.videoEquals(newVideoInfo, oldVideoInfo)) {
                    //释放新VideoInfo对应的Video实例
                    val instanceForNewTag = VideoInstanceManager.findVideoByTarget(newVideoInfo.target)
                    instanceForNewTag?.let { v ->
                        v.getVideoContainer()?.let { vc ->
                            unBindVideo(vc)
                        }
                        v.release()
                    }
                    //更新实例记录
                    VideoInstanceManager.removeVideo(video)
                    video.setVideoInfo(newVideoInfo)
                    VideoInstanceManager.addVideo(video)

                    //TODO同步当前播放信息
                    PlayHelper.onVideoViewVideoInfoChanged(video)
                    if (PlayHelper.lastPlayVideo == video) {
                        PlayList.updateCurrIndex()
                    }
                }
            }
        }

    }

}