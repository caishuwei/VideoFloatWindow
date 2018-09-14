package com.csw.android.videofloatwindow.util

import android.util.Log
import com.csw.android.videofloatwindow.app.MyApplication

class LogUtils {
    companion object {

        fun v(tag: String = MyApplication.instance.packageName, msg: String) {
            if (Utils.isAppInDebug()) Log.v(tag, msg)
        }

        fun d(tag: String = MyApplication.instance.packageName, msg: String) {
            if (Utils.isAppInDebug()) Log.d(tag, msg)
        }

        fun i(tag: String = MyApplication.instance.packageName, msg: String) {
            if (Utils.isAppInDebug()) Log.i(tag, msg)
        }

        fun w(tag: String = MyApplication.instance.packageName, msg: String) {
            if (Utils.isAppInDebug()) Log.w(tag, msg)
        }

        fun e(tag: String = MyApplication.instance.packageName, msg: String) {
            if (Utils.isAppInDebug()) Log.e(tag, msg)
        }
    }
}