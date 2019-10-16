package com.csw.android.videofloatwindow.ui.base

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.csw.android.videofloatwindow.util.Utils
import com.google.android.material.snackbar.Snackbar
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
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

    /**
     * 若本地文件读写权限通过，则执行任务
     */
    fun runIfExternalStoragePermissionGranted(onPermissionGranted: () -> (Unit)) {
        val arr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        requestPermissions(*arr) {
            if (it) {
                onPermissionGranted()
            } else {
                activity?.window?.decorView?.let {
                    Snackbar.make(it, "SD卡文件读取权限被拒绝", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 请求权限，回调结果
     */
    fun requestPermissions(vararg permissions: String, onPermissionGranted: (granted: Boolean) -> (Unit)) {
        val activity = activity
        val view = activity?.window?.decorView
        Utils.runIfNotNull(activity, view) { a, v ->
            addLifecycleTask(
                    RxPermissions(a)
                            .request(*permissions)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    {
                                        onPermissionGranted(it)
                                    },
                                    {
                                        Snackbar.make(v, it.message
                                                ?: "权限请求异常", Snackbar.LENGTH_SHORT).show()
                                    }
                            )
            )
        }
    }

}