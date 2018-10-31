package com.csw.android.videofloatwindow.dagger.base

/**
 * 视图接口，定义对切面暴露的方法
 */
interface IBaseView<P : IBasePresenter> {

    /**
     * 设置切面
     */
    fun setPresenter(presenter: P)

}