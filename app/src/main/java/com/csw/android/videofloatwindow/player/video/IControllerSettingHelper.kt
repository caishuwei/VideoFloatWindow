package com.csw.android.videofloatwindow.player.video

import android.view.View

/**
 * 播放控制设置辅助者
 */
interface IControllerSettingHelper {

    /**
     * 设置返回按钮点击事件
     */
    fun setBackClickListener(listener: View.OnClickListener?): IControllerSettingHelper

    /**
     * 设置标题
     */
    fun setTitle(titleStr: String): IControllerSettingHelper

    /**
     * 设置关闭按钮事件
     */
    fun setCloseClickListener(listener: View.OnClickListener?): IControllerSettingHelper

    /**
     * 设置上一首事件
     */
    fun setPreviousClickListener(listener: View.OnClickListener?): IControllerSettingHelper

    /**
     * 设置上一首事件
     */
    fun setNextClickListener(listener: View.OnClickListener?): IControllerSettingHelper

    /**
     * 设置全屏按钮事件
     */
    fun setFullScreenClickListener(listener: View.OnClickListener?): IControllerSettingHelper

    /**
     * 设置小窗口播放按钮事件
     */
    fun setFloatWindowClickListener(listener: View.OnClickListener?): IControllerSettingHelper

    /**
     * 启用音量与亮度控制
     */
    fun setVolumeAndBrightnessControllerEnable(enable: Boolean): IControllerSettingHelper

    /**
     * 重新设置控制器状态
     */
    fun reset(): IControllerSettingHelper

    /**
     * 设置视图点击事件，并根据点击事件是否为空控制视图显示或隐藏
     */
    fun setClickListener(view: View, listener: View.OnClickListener?): IControllerSettingHelper {
        view.setOnClickListener(listener)
        view.visibility = if (listener == null) View.GONE else View.VISIBLE
        return this
    }
}