package com.csw.android.videofloatwindow.app

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.BezierRadarHeader
import com.tencent.bugly.crashreport.CrashReport


class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
        lateinit var appComponent: AppComponent

        init {
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
                return@setDefaultRefreshHeaderCreator BezierRadarHeader(context)
            }

            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
                return@setDefaultRefreshFooterCreator ClassicsFooter(context).setDrawableSize(20f)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        MyApplication.instance = this
        appComponent = DaggerAppComponent.builder().setMyApplication(this).build()
        CrashReport.initCrashReport(getApplicationContext(), "eb0b86c8af", false);
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}