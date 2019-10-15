package com.csw.android.videofloatwindow.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * 碎片基类
 */
abstract class BaseFragment : Fragment(), IUICreator {

    //只在生命周期中执行的任务
    private val lifecycleTasks: WeakHashMap<Disposable, Any> = WeakHashMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getContentViewID(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view, savedInstanceState)
        initAdapter()
        initListener()
        initData()
    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
    }

    override fun initAdapter() {
    }

    override fun initListener() {
    }

    override fun initData() {
        lazyInitData()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        lazyInitData()
    }

    private fun lazyInitData() {
        if (userVisibleHint && view != null) {
            onLazyLoad()
        }
    }

    /**
     * 懒加载数据开始执行
     */
    open fun onLazyLoad() {

    }

    override fun onDestroyView() {
        clearLifecycleTask()
        super.onDestroyView()
    }

    /**
     * 添加只在界面生命周期中执行的任务，当界面销毁时取消任务
     */
    fun addLifecycleTask(task: Disposable) {
        if (!task.isDisposed) {
            lifecycleTasks[task] = task
        }
    }

    private fun clearLifecycleTask() {
        val keys = lifecycleTasks.keys
        for (key in keys) {
            key.dispose()
        }
    }
}