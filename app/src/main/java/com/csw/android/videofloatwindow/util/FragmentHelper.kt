package com.csw.android.videofloatwindow.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FragmentHelper {

    companion object {

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