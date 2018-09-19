package com.csw.android.videofloatwindow.ui

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.csw.android.videofloatwindow.IVideoServiceInterface
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.services.video.VideoService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var videoServiceInterface: IVideoServiceInterface? = null
    private val conn: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            videoServiceInterface = IVideoServiceInterface.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            videoServiceInterface = null;
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_create_float_window.setOnClickListener {
            startActivity(Intent(this@MainActivity, LocalVideosActivity::class.java))
        }
        ContextCompat.startForegroundService(this@MainActivity, Intent(this@MainActivity, VideoService::class.java))
        bindService(Intent(this@MainActivity, VideoService::class.java), conn, Service.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        unbindService(conn)
        super.onDestroy()
    }

}