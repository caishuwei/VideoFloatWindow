package com.csw.android.videofloatwindow.player.video

import android.view.View
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.container.impl.VideoContainer
import com.csw.android.videofloatwindow.player.video.IControllerSettingHelper

/**
 * 视频播放实现
 */
interface IVideo {

    /**
     * 获取播放视图
     */
    fun getView(): View;

    /**
     * 获取VideoView的播放控制器设置者
     */
    fun getControllerSettingHelper(): IControllerSettingHelper

    /**
     * 设置播放信息
     */
    fun setVideoInfo(videoInfo: VideoInfo)

    /**
     * 获取播放信息
     */
    fun getVideoInfo(): VideoInfo

    /**
     * 设置容器
     */
    fun setVideoContainer(videoContainer: VideoContainer?)

    /**
     * 获取容器
     */
    fun getVideoContainer(): VideoContainer?

    /**
     * VideoView是否处于容器中
     */
    fun inContainer(): Boolean

    /**
     * 开始播放
     */
    fun play()

    /**
     * 是否正在播放
     */
    fun isPlaying(): Boolean

    /**
     * 是否播放结束
     */
    fun isEnd(): Boolean

    /**
     * 暂停播放
     */
    fun pause()

    /**
     * 释放播放器
     */
    fun release()

}