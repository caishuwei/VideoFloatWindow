package com.csw.android.videofloatwindow.ui.base

import com.csw.android.videofloatwindow.dagger.IBasePresenter
import com.csw.android.videofloatwindow.dagger.LifecycleCallback
import javax.inject.Inject

/**
 * 应用MVP模式View的基类
 */
abstract class BaseMVPActivity<T : IBasePresenter> : BaseActivity() {

    //Presenter
    @Inject
    lateinit var presenter: T
    //生命周期回调
    @Inject
    lateinit var lifecycleCallback: LifecycleCallback

    override fun noticePresenterCreated() {
        super.noticePresenterCreated()
        lifecycleCallback.onCreated()
    }

    override fun noticePresenterUICreated() {
        super.noticePresenterUICreated()
        lifecycleCallback.onUICreated()
    }

    override fun onStart() {
        super.onStart()
        lifecycleCallback.onUIStart()
    }

    override fun onResume() {
        super.onResume()
        lifecycleCallback.onUIResume()
    }

    override fun onPause() {
        super.onPause()
        lifecycleCallback.onUIPause()
    }

    override fun onStop() {
        lifecycleCallback.onUIStop()
        super.onStop()
    }

    override fun onDestroy() {
        lifecycleCallback.onUIDestroy()
        lifecycleCallback.onDestroy()
        super.onDestroy()
    }


}