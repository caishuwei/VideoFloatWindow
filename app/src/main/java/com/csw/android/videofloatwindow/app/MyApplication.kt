package com.csw.android.videofloatwindow.app

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import com.csw.android.videofloatwindow.greendao.DaoMaster
import com.csw.android.videofloatwindow.greendao.DaoSession

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        MyApplication.instance = this
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