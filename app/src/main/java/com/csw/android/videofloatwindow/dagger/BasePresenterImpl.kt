package com.csw.android.videofloatwindow.dagger

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BasePresenterImpl<V : IBaseView>(protected val view: V) : IBasePresenter, LifecycleCallback {

    private val compositeDisposable = CompositeDisposable()


    /**
     * 添加RxJava任务处理器,用于在界面销毁时中断任务
     */
    fun addRxJavaTaskDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun getLifecycleCallback(): LifecycleCallback {
        return this
    }

    //LifecycleCallback>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    override fun onUICreated() {
    }

    override fun onUIStart() {
    }

    override fun onUIPause() {
    }

    override fun onUIResume() {
    }

    override fun onUIStop() {
    }

    override fun onUIDestroy() {
        compositeDisposable.dispose()
    }

    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
}