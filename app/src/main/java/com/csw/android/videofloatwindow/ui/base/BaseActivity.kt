package com.csw.android.videofloatwindow.ui.base

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * Activity基类
 */
abstract class BaseActivity : AppCompatActivity(), IUICreator {

    //只在生命周期中执行的任务
    private val lifecycleTasks: WeakHashMap<Disposable, Any> = WeakHashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentViewID())
        initView(window.decorView, savedInstanceState)
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
    }

    override fun onDestroy() {
        clearLifecycleTask()
        super.onDestroy()
    }


    /**
     * 设置全屏
     */
    fun setFullScreen(fullScreen: Boolean) {
        //ActionBar设置
        supportActionBar?.let {
            if (fullScreen) {
                it.hide()
            } else {
                it.show()
            }
        }
        //全屏设置
        if (fullScreen) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
                window.decorView.systemUiVisibility = View.GONE
            } else if (Build.VERSION.SDK_INT >= 19) {
                //SYSTEM_UI_FLAG_IMMERSIVE_STICKY是沉浸模式，api19开始有的，
                // 需要搭配SYSTEM_UI_FLAG_HIDE_NAVIGATION（隐藏虚拟按钮）或SYSTEM_UI_FLAG_FULLSCREEN（隐藏状态栏）进行使用才有效
                //如果不加SYSTEM_UI_FLAG_IMMERSIVE_STICKY标签，用户手动打开会清除前两个标签，加了Sticky标签后用户打开视为暂时显示状态栏和虚拟按钮
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }
        } else {
            if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
                window.decorView.systemUiVisibility = View.VISIBLE
            } else if (Build.VERSION.SDK_INT >= 19) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
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