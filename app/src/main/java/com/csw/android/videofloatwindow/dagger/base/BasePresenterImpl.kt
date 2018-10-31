package com.csw.android.videofloatwindow.dagger.base

open class BasePresenterImpl<P : IBasePresenter, V : IBaseView<P>> : IBasePresenter {

    constructor(view: V) {
        view.setPresenter(this as P)
    }

    override fun onUIEnterForeground() {
    }

    override fun onUIEnterBackground() {
    }
}