package com.csw.android.videofloatwindow.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * 碎片帮助类
 */
class FragmentHelper {

    companion object {

        /**
         * 获取碎片实例，若不存在碎片则创建并添加，这些碎片一般都是用于完成某种事物，而不需要界面，如第三方登录用StartActivityForResult接收登录结果，
         * 可以通过将第三方登录的调用与回调代码封装到一个Fragment中，并将其添加到Activity或Fragment上去执行，这样可以大大减少UI界面的复杂程度，还可以隔离第三方包
         * ，避免需要多处改动（Glide中还将其用于监听Activity,Fragment的生命周期以便及时停止异步任务，RxPermissions将其用于封装权限请求过程）
         */
        fun <T : Fragment> getFragmentInstance(fragmentManager: FragmentManager, tag: String, clazz: Class<T>): T {
            var fragment = fragmentManager.findFragmentByTag(tag)
            var result: T? = null
            if (fragment != null) {
                try {
                    result = fragment as T
                } catch (e: Exception) {
                    e.printStackTrace()
                    //找到了已经存在的Fragment，但类型不同，显然Tag重复了
                    throw RuntimeException("FragmentHelper.getFragmentInstance tag：\"${tag}\"已经被使用${fragment::class.java.name}")
                }
            }
            if (result == null) {
                try {
                    result = clazz.newInstance()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            result?.let {
                if (!it.isAdded) { //未添加到fragmentManager
                    fragmentManager.beginTransaction()
                            .add(it, tag)
                            .commitAllowingStateLoss()
                    fragmentManager.executePendingTransactions()
                }
                if (it.isDetached) { //未绑定到fragmentManager
                    fragmentManager.beginTransaction()
                            .attach(it)
                            .commitAllowingStateLoss()
                    fragmentManager.executePendingTransactions()
                }
                return it
            }
            throw RuntimeException("FragmentHelper.getFragmentInstance ${clazz.name}创建实例的过程中发生了异常")
        }


    }

}