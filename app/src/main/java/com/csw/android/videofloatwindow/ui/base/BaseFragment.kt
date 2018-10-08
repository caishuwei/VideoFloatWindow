package com.csw.android.videofloatwindow.ui.base

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * 碎片基类
 */
abstract class BaseFragment : Fragment(), IUICreator {

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
}