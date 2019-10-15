package com.csw.android.videofloatwindow.permission

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestManageOverlayPermission(activity: FragmentActivity, listener: OnRequestResultListener) {
        //存在还未回调的任务
        Utils.runIfNotNull(activity, listener) { _, _ ->
            callback()
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
            callback()
        }
    }

    private fun callback() {
        Utils.runIfNotNull(activity, listener) { v1, v2 ->
            when {
//                Build.VERSION.SDK_INT == Build.VERSION_CODES.O ->
//                    //8.0bug 由于Settings.canDrawOverlays与修改悬浮框权限结果不同步问题，大概需要两秒的时间才能获取修改后的结果
//                    //这里直接通过添加悬浮窗进行判断
//                    v2.onResult(canAddWindow(v1))
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> v2.onResult(Settings.canDrawOverlays(v1))
                else -> v2.onResult(true)
            }
        }
        listener = null
        requestActivity = null
    }

    private fun canAddWindow(context: Context): Boolean {
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val view = View(context)
            view.setBackgroundColor(Color.RED)
            val params = WindowManager.LayoutParams()
            //设置窗口类型， android 8.0以上只能设置TYPE_APPLICATION_OVERLAY
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            //设置像素格式 但为什么用的是RGBA 而不是ARGB
            params.format = PixelFormat.RGBA_8888
            //设置不取得焦点
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            params.gravity = Gravity.START or Gravity.TOP
            params.x = 0
            params.y = 0
            params.width = 40
            params.height = 40
            params.dimAmount = 0f
            windowManager.addView(view, params)
            windowManager.removeView(view)
            return true
        } catch (e: Exception) {
            return false
        }
    }
}

