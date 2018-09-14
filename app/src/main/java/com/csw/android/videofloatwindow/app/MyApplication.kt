package com.csw.android.videofloatwindow.app

import android.app.Application

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        MyApplication.instance = this
    }
}