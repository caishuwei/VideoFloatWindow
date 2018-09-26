package com.csw.android.videofloatwindow.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.services.video.VideoService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_create_float_window.setOnClickListener {
            startActivity(
                    Intent(
                    this@MainActivity,
                    LocalVideosActivity::class.java
                    )
            )
        }
        ContextCompat.startForegroundService(
                this@MainActivity,
                Intent(this@MainActivity, VideoService::class.java)
        )
    }

}