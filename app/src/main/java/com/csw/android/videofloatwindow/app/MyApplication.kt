package com.csw.android.videofloatwindow.app

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import com.csw.android.videofloatwindow.greendao.DaoMaster
import com.csw.android.videofloatwindow.greendao.DaoSession
import com.csw.android.videofloatwindow.player.PlayerHelper
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.BezierRadarHeader

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication

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