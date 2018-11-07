package com.csw.android.videofloatwindow.ui.base

import com.csw.android.videofloatwindow.dagger.base.IBasePresenter
import com.csw.android.videofloatwindow.dagger.base.IBaseView

abstract class MvpFragment() : BaseFragment(), IBaseView {
    private var basePresenter: IBasePresenter? = null;

    override fun setBasePresenter(basePresenter: IBasePresenter) {
        this.basePresenter = basePresenter;
    }

    override fun onStart() {
        basePresenter?.onUIEnterForeground()
        super.onStart()
    }

    override fun onStop() {
        basePresenter?.onUIEnterBackground()
        super.onStop()
    }

}