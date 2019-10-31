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
            //设置下拉刷新和上拉加载更多的样式
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
        //构建APP组件，注册MyApplication实例到组件中
        appComponent = DaggerAppComponent.builder().setMyApplication(this).build()
        //腾讯bugly初始化，一个用于获取未处理异常堆栈的工具
        CrashReport.initCrashReport(getApplicationContext(), "eb0b86c8af", false);
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        //用于解决使用太多第三方包导致方法数超出65k限制（一个dex包只能包含这么多个方法）使用分包
        //这里是兼容Android5.0以下的分包实现
        MultiDex.install(this)
    }

}