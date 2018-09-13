package com.csw.android.videofloatwindow.permission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.csw.android.videofloatwindow.util.Utils

/**
 * 系统对话框权限请求封装
 */
class SystemAlertWindowPermission : Fragment() {

    interface OnRequestResultListener {
        fun onResult(isGranted: Boolean)
    }

    companion object {
        private const val REQUEST_CODE = 1
        private const val FRAGMENT_TAG = "SystemAlertWindowRequest.SystemAlertWindowPermission"

        /**
         * 请求系统顶层对话框权限
         */
        fun request(activity: FragmentActivity, listener: OnRequestResultListener) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(activity)) {
                    listener.onResult(true)
                } else {
                    val fragment = getFragment(activity.supportFragmentManager)
                    fragment.requestManageOverlayPermission(activity, listener)
                }
            } else {
                listener.onResult(true)
            }
        }

        private fun getFragment(fragmentManager: FragmentManager): SystemAlertWindowPermission {
            val result = fragmentManager.findFragmentByTag(SystemAlertWindowPermission.FRAGMENT_TAG)
            return if (result != null && result is SystemAlertWindowPermission) {
                result
            } else {
                val newInstance = SystemAlertWindowPermission()
                fragmentManager.beginTransaction()
                        .add(newInstance, SystemAlertWindowPermission.FRAGMENT_TAG)
                        .commitAllowingStateLoss()
                fragmentManager.executePendingTransactions()
                newInstance
            }
        }
    }

    private var listener: OnRequestResultListener? = null
    private var requestActivity: FragmentActivity? = null
    private val mainHandler = Handler()
    private val delayTask = Runnable {
        Utils.runIfNotNull(activity, listener) { v1, v2 ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                v2.onResult(Settings.canDrawOverlays(v1))
            } else {
                v2.onResult(true)
            }
        }
        listener = null
        requestActivity = null
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestManageOverlayPermission(activity: FragmentActivity, listener: OnRequestResultListener) {
        //存在还未回调的任务，移除并直接执行
        Utils.runIfNotNull(activity, listener) { _, _ ->
            mainHandler.removeCallbacks(delayTask)
            delayTask.run()
        }
        //请求权限
        this.requestActivity = activity
        this.listener = listener
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:" + activity.packageName)
        startActivityForResult(intent, SystemAlertWindowPermission.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SystemAlertWindowPermission.REQUEST_CODE) {
            //由于Settings.canDrawOverlays与修改悬浮框权限结果不同步问题，延迟一段时间后再判断回调结果
            mainHandler.postDelayed(delayTask, 2000)
        }
    }
}

