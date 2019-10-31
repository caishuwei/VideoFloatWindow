package com.csw.android.videofloatwindow.dagger

/**
 * 作为切面必须要了解视图的生命周期，以便处理一下监听，或者中断一些异步任务，这个接口用于View告知Presenter自己当前的生命周期变化
 */
interface LifecycleCallback {

    /**
     * 创建
     */
    fun onCreated()

    /**
     * UI创建
     */
    fun onUICreated()

    /**
     * UI可见
     */
    fun onUIStart()

    /**
     * UI处于前台
     */
    fun onUIPause()

    /**
     * UI退出前台
     */
    fun onUIResume()

    /**
     * UI不可见
     */
    fun onUIStop()

    /**
     * UI销毁
     */
    fun onUIDestroy()

    /**
     * 销毁
     */
    fun onDestroy()
}