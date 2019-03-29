package com.csw.android.videofloatwindow.player.video.base

import android.view.View
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.base.VideoContainer

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
     * 绑定到VideoContainer中
     */
    fun bindVideoContainer(videoContainer: VideoContainer)

    /**
     * 从videoContainer解绑VideoView
     * @param release true 解绑后释放VideoView
     */
    fun unbindVideoContainer(videoContainer: VideoContainer, release: Boolean = true)

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
     * 暂停播放
     */
    fun pause()

    /**
     * 释放播放器
     */
    fun release()

}