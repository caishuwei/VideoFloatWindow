package com.csw.android.videofloatwindow.ui.main

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.ui.base.BaseActivity
import com.csw.android.videofloatwindow.ui.base.CommonActivity
import com.csw.android.videofloatwindow.ui.list.LocalVideosFragment
import com.csw.android.videofloatwindow.ui.list.PlaySheetVideosActivity
import com.csw.android.videofloatwindow.util.DBUtils
import com.csw.android.videofloatwindow.view.SpaceLineItemDecoration
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    companion object {
        private const val LOCAL_VIDEO_PLAY_SHEET_ID = -1L
    }

    private lateinit var playSheetAdapter: PlaySheetAdapter

    override fun getContentViewID(): Int {
        return R.layout.activity_main
    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        super.initView(rootView, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(object : SpaceLineItemDecoration(0, 1, 0, 0, Color.GRAY) {
            override fun skipDraw(position: Int): Boolean {
                return position == 0
            }
        })
    }

    override fun initAdapter() {
        super.initAdapter()
        playSheetAdapter = PlaySheetAdapter()
        recyclerView.adapter = playSheetAdapter
    }

    override fun initListener() {
        super.initListener()
        playSheetAdapter.setOnItemClickListener { adapter, view, position ->
            val playSheet = playSheetAdapter.getItem(position)
            playSheet?.let {
                if (it.id == LOCAL_VIDEO_PLAY_SHEET_ID) {
                    CommonActivity.openActivity(this@MainActivity, LocalVideosFragment::class.java, null)
//                    startActivity(Intent(this@MainActivity, LocalVideosActivity::class.java))
                } else {
                    PlaySheetVideosActivity.openActivity(this@MainActivity, playSheet.id)
                }
            }
        }
        smartRefreshLayout.isEnableRefresh = true
        smartRefreshLayout.isEnableLoadMore = false
        smartRefreshLayout.setOnRefreshListener {
            playSheetAdapter.setNewData(null)
            loadPlaySheets()
        }
    }

    override fun initData() {
        super.initData()
        smartRefreshLayout.autoRefresh()
        PlayHelper.backgroundPlay = true
    }

    private fun loadPlaySheets() {
        addLifecycleTask(
                Observable.create(
                        ObservableOnSubscribe<List<PlaySheet>> {
                            val result = arrayListOf<PlaySheet>()
                            result.add(PlaySheet(LOCAL_VIDEO_PLAY_SHEET_ID, "本地视频", 0L))
                            result.addAll(DBUtils.getPlaySheets())
                            it.onNext(result)
                            it.onComplete()
                        }
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            playSheetAdapter.setNewData(it)
                            smartRefreshLayout.finishRefresh(true)
                        }
        )
    }

}