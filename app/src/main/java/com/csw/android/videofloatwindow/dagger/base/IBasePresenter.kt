package com.csw.android.videofloatwindow.dagger.base

/**
 * 切片接口(提供一些View可以调用的方法)
 */
interface IBasePresenter {

    /**
     * 当UI进入前台时调用
     */
    fun onUIEnterForeground()

    /**
     * 当UI进入后台时调用
     */
    fun onUIEnterBackground()

}