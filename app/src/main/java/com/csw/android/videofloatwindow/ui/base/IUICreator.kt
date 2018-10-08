package com.csw.android.videofloatwindow.ui.base

import android.os.Bundle
import android.view.View

/**
 * UI创建
 */
interface IUICreator {

    /**
     * 获取布局文件Id
     */
    fun getContentViewID(): Int

    /**
     * 初始化视图
     */
    fun initView(rootView: View, savedInstanceState: Bundle?)

    /**
     * 初始化适配器
     */
    fun initAdapter()

    /**
     * 初始化监听
     */
    fun initListener()

    /**
     * 初始化界面数据
     */
    fun initData()
}