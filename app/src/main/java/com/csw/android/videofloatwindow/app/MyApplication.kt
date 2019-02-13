package com.csw.android.videofloatwindow.app

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.support.multidex.MultiDex
import com.csw.android.videofloatwindow.greendao.DaoMaster
import com.csw.android.videofloatwindow.greendao.DaoSession
import com.csw.android.videofloatwindow.player.PlayerHelper
import com.csw.android.videofloatwindow.player.base.BrightnessController
import com.csw.android.videofloatwindow.player.base.VolumeController
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.BezierRadarHeader
import com.tencent.bugly.crashreport.CrashReport


class MyApplication : Application() {
    lateinit var brightnessController: BrightnessController
        private set
    lateinit var volumeController: VolumeController
        private set

    companion object {
        lateinit var instance: MyApplication
        lateinit var appComponent: AppComponent

        init {
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
                return@setDefaultRefreshHeaderCreator BezierRadarHeader(context)
            }

            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
                return@setDefaultRefreshFooterCreator ClassicsFooter(context).setDrawableSize(20f)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        MyApplication.instance = this
        appComponent = DaggerAppComponent.builder().setMyApplication(this).build()
        CrashReport.initCrashReport(getApplicationContext(), "eb0b86c8af", false);
        brightnessController = BrightnessController()
        volumeController = VolumeController()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    val playerHelper: PlayerHelper by lazy {
        PlayerHelper(this)
    }

    val dbHelper: DaoMaster.DevOpenHelper by lazy {
        DaoMaster.DevOpenHelper(this, "video_db", null)
    }
    val db: SQLiteDatabase by lazy {
        dbHelper.writableDatabase
    }
    val daoMaster: DaoMaster by lazy {
        DaoMaster(db)
    }
    val daoSession: DaoSession by lazy {
        daoMaster.newSession()
    }

}