package com.csw.android.videofloatwindow.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.services.video.VideoService
import com.csw.android.videofloatwindow.ui.list.LocalVideosActivity
import com.csw.android.videofloatwindow.ui.list.PlaySheetVideosActivity
import com.csw.android.videofloatwindow.util.DBUtils
import com.csw.android.videofloatwindow.view.SpaceLineItemDecoration
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val LOCAL_VIDEO_PLAY_SHEET_ID = -1L
    }

    private lateinit var playSheetAdapter: PlaySheetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ContextCompat.startForegroundService(
                this@MainActivity,
                Intent(this@MainActivity, VideoService::class.java)
        )

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        playSheetAdapter = PlaySheetAdapter()
        playSheetAdapter.setOnItemClickListener { adapter, view, position ->
            val playSheet = playSheetAdapter.getItem(position)
            playSheet?.let {
                if (it.id == LOCAL_VIDEO_PLAY_SHEET_ID) {
                    startActivity(Intent(this@MainActivity, LocalVideosActivity::class.java))
                } else {
                    PlaySheetVideosActivity.openActivity(this@MainActivity, playSheet.id)
                }
            }
        }
        recyclerView.adapter = playSheetAdapter
        recyclerView.addItemDecoration(object : SpaceLineItemDecoration(0, 1, 0, 0, Color.GRAY) {
            override fun skipDraw(position: Int): Boolean {
                return position == 0
            }
        })
        smartRefreshLayout.isEnableRefresh = true
        smartRefreshLayout.isEnableLoadMore = false
        smartRefreshLayout.setOnRefreshListener {
            playSheetAdapter.setNewData(null)
            loadPlaySheets()
        }
        smartRefreshLayout.autoRefresh()
    }

    private fun loadPlaySheets() {
        Observable.create(ObservableOnSubscribe<List<PlaySheet>> {
            val result = arrayListOf<PlaySheet>()
            result.add(PlaySheet(LOCAL_VIDEO_PLAY_SHEET_ID, "本地视频", 0L))
            result.addAll(DBUtils.getPlaySheets())
            it.onNext(result)
            it.onComplete()
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    playSheetAdapter.setNewData(it)
                    smartRefreshLayout.finishRefresh(true)
                })
    }

}