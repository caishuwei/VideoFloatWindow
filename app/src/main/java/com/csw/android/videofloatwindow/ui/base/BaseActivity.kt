package com.csw.android.videofloatwindow.ui.base

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.dagger.IBaseView
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * Activity基类
 */
abstract class BaseActivity : AppCompatActivity(), IUICreator, IBaseView {

    //只在生命周期中执行的任务
    private val lifecycleTasks: WeakHashMap<Disposable, Any> = WeakHashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //在设置布局之前注入
        initInject()

        setContentView(getContentViewID())
        initView(window.decorView, savedInstanceState)
        initAdapter()
        initListener()
        initData()
    }

    /**
     * 初始化注入
     */
    open fun initInject() {

    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
    }

    override fun initAdapter() {
    }

    override fun initListener() {
    }

    override fun initData() {
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        super.startActivity(intent, options)
        overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
    }

    override fun startActivityFromChild(child: Activity, intent: Intent?, requestCode: Int, options: Bundle?) {
        super.startActivityFromChild(child, intent, requestCode, options)
        overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.close_enter, R.anim.close_exit)
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