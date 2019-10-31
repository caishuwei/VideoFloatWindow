package com.csw.android.videofloatwindow.dagger

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * 切面基类的实现
 * 提供添加只在切面存活时执行的任务，当切面销毁时取消订阅之前添加的RxJava任务
 * 提供添加只在UI存活时执行的任务，当UI销毁时取消订阅之前添加的RxJava任务
 */
open class BasePresenterImpl<V : IBaseView>(protected val view: V) : IBasePresenter, LifecycleCallback {

    private val presenterCompositeDisposable = CompositeDisposable()
    private var uiCompositeDisposable = CompositeDisposable()
    /**
     * UI是否还存活
     */
    protected var uiIsAlive = false
        private set


    /**
     *  添加RxJava任务处理器,任务随切片销毁而停止
     */
    fun addRxJavaTaskRunOnPresenterLive(disposable: Disposable) {
        presenterCompositeDisposable.add(disposable)
    }

    /**
     * 添加RxJava任务处理器,任务随UI销毁而停止
     */
    fun addRxJavaTaskRunOnUILive(disposable: Disposable) {
        //添加Disposable，若uiCompositeDisposable.isDispose() == true 会自动将添加进去的disposable解除订阅
        uiCompositeDisposable.add(disposable)
    }

    //IBasePresenter>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    override fun initUIData() {

    }

    override fun getLifecycleCallback(): LifecycleCallback {
        return this
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    //LifecycleCallback>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    override fun onCreated() {
    }

    override fun onUICreated() {
        uiIsAlive = true
        //创建一个新的，FragmentUI重新创建时，这里会重新调用，旧的uiCompositeDisposable已经解除订阅，导致无法添加任务
        uiCompositeDisposable = CompositeDisposable()
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
        uiIsAlive = false
        //解除所有只在UI存活时执行的任务订阅
        uiCompositeDisposable.dispose()
    }

    override fun onDestroy() {
        //解除所有只在切面存活时执行的任务订阅
        presenterCompositeDisposable.dispose()
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
}