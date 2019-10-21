package com.csw.android.videofloatwindow.dagger

interface LifecycleCallback {
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
}