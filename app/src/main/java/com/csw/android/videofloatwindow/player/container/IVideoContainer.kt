package com.csw.android.videofloatwindow.player.container

import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.video.IControllerSettingHelper
import com.csw.android.videofloatwindow.player.video.IVideo

interface IVideoContainer {

    /**
     * 设置视频播放信息
     */
    fun setVideoInfo(videoInfo: VideoInfo)

    /**
     * 获取播放信息
     */
    fun getVideoInfo():VideoInfo?

    /**
     * 取得当前播放实例
     */
    fun getVideo():IVideo?

    /**
     * 绑定播放器实例
     */
    fun bindVideoView()

    /**
     * 解绑播放器实例
     */
    fun unBindVideoView()

    /**
     * VideoView绑定到此视图
     */
    fun onVideoBind(video: IVideo)

    /**
     * 播放控制器初始化
     */
    fun onPlayControllerSetup(controllerSettingHelper: IControllerSettingHelper)

    /**
     * VideoView从此视图上解绑
     */
    fun onVideoUnbind(video: IVideo)

    /**
     * 开始播放，若当前还没有绑定播放器实例，则进行绑定后播放
     */
    fun play()

    /**
     * 暂停播放，若当前有绑定的播放器实例，则可以调用实例停止播放，否则不用管
     */
    fun pause()

    /**
     * 释放播放器，若当前有绑定的播放器实例，则调用实例释放播放器，否则不用管
     */
    fun release()
}