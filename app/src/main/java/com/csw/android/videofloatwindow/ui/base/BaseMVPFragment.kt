package com.csw.android.videofloatwindow.ui.base

import android.os.Bundle
import android.view.View
import com.csw.android.videofloatwindow.dagger.IBasePresenter
import com.csw.android.videofloatwindow.dagger.LifecycleCallback
import javax.inject.Inject

abstract class BaseMVPFragment<T : IBasePresenter> : BaseFragment() {

    //Presenter
    @Inject
    lateinit var presenter: T
    //生命周期回调
    private var lifecycleCallback: LifecycleCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initInject()
        lifecycleCallback = presenter.getLifecycleCallback()
    }

    /**
     * 初始化注入
     */
    protected open fun initInject() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleCallback?.onUICreated()
    }
    override fun onStart() {
        super.onStart()
        lifecycleCallback?.onUIStart()
    }

    override fun onResume() {
        super.onResume()
        lifecycleCallback?.onUIResume()
    }

    override fun onPause() {
        super.onPause()
        lifecycleCallback?.onUIPause()
    }

    override fun onStop() {
        lifecycleCallback?.onUIStop()
        super.onStop()
    }

    override fun onDestroyView() {
        lifecycleCallback?.onUIDestroy()
        super.onDestroyView()
    }

}