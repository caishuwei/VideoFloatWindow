package com.csw.android.videofloatwindow.player.container

import androidx.lifecycle.Lifecycle
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
    fun getVideoInfo(): VideoInfo?

    /**
     * 取得当前播放实例
     */
    fun getVideo(): IVideo?

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

    /**
     * 在视图进入前台时，自动绑定播放器
     * @param bindVideo ui进入前台时绑定播放器
     */
    fun setBindVideoOnViewEnterForeground(bindVideo: Boolean)

    /**
     * 在视图退出前台时，停止播放
     * @param pause 是否在ui pause时停止
     */
    fun setPauseOnViewExitForeground(pause: Boolean)

    /**
     * 在视图释放时销毁播放器
     * @param release 是否释放
     */
    fun setReleaseOnViewDestroy(release: Boolean)

    /**
     * 观察View的生命周期,这个方法在bindVideoOnViewResume这些生命周期监听设置之后才调用，因为注册这个监听后
     * ，会一步步同步到当前View的生命周期。因此若pauseOnViewPause(true)
     */
    fun registerViewLifeCycleObserver(viewLifeCycle: Lifecycle, register: Boolean)
}