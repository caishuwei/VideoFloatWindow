package com.csw.android.videofloatwindow.dagger

/**
 * 切片接口(提供一些View可以调用的方法)
 */
interface IBasePresenter{

    /**
     * 初始化UI数据
     */
    fun initUIData()

    /**
     * 取得生命周期回调对象
     */
    fun getLifecycleCallback(): LifecycleCallback

}