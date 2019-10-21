package com.csw.android.videofloatwindow.ui.base

import android.os.Bundle
import com.csw.android.videofloatwindow.dagger.IBasePresenter
import com.csw.android.videofloatwindow.dagger.LifecycleCallback
import javax.inject.Inject

abstract class BaseMVPActivity<T : IBasePresenter> : BaseActivity() {

    //Presenter
    @Inject
    lateinit var presenter: T
    //生命周期回调
    private var lifecycleCallback: LifecycleCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleCallback = presenter.getLifecycleCallback()
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

    override fun onDestroy() {
        lifecycleCallback?.onUIDestroy()
        super.onDestroy()
    }


}