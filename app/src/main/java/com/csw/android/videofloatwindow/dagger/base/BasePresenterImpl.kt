package com.csw.android.videofloatwindow.dagger.base

open class BasePresenterImpl : IBasePresenter {

    constructor(view: IBaseView) {
        view.setBasePresenter(this)
    }

    override fun onUIEnterForeground() {
    }

    override fun onUIEnterBackground() {
    }
}