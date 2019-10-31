package com.csw.android.videofloatwindow.ui.base

import android.os.Bundle
import com.csw.android.videofloatwindow.dagger.IBasePresenter
import com.csw.android.videofloatwindow.dagger.LifecycleCallback
import javax.inject.Inject

abstract class BaseMVPFragment<T : IBasePresenter> : BaseFragment() {

    //Presenter
    @Inject
    lateinit var presenter: T
    //生命周期回调
    @Inject
    lateinit var lifecycleCallback: LifecycleCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initInject()
        lifecycleCallback.onCreated()
    }

    /**
     * 初始化注入
     */
    protected open fun initInject() {

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

    override fun onDestroyView() {
        lifecycleCallback.onUIDestroy()
        super.onDestroyView()
    }

    override fun onDestroy() {
        lifecycleCallback.onDestroy()
        super.onDestroy()
    }

}