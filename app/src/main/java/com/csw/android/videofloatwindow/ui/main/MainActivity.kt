package com.csw.android.videofloatwindow.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.Constants
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

    private lateinit var playSheetAdapter: PlaySheetAdapter

    override fun getContentViewID(): Int {
        return R.layout.activity_main
    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        super.initView(rootView, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
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
        playSheetAdapter.setOnItemClickListener { _, _, position ->
            val playSheet = playSheetAdapter.getItem(position)
            playSheet?.let {
                if (it.name == Constants.LOCAL_VIDEO_PLAY_SHEET) {
                    CommonActivity.openActivity(this@MainActivity, LocalVideosFragment::class.java, LocalVideosFragment.createData(it))
//                    startActivity(Intent(this@MainActivity, LocalVideosActivity::class.java))
                } else {
                    PlaySheetVideosActivity.openActivity(this@MainActivity, playSheet.id)
                }
            }
        }
        smartRefreshLayout.setEnableRefresh(true)
        smartRefreshLayout.setEnableLoadMore(false)
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
                            DBUtils.insertPlaySheetIfNoExist(PlaySheet(Constants.LOCAL_VIDEO_PLAY_SHEET))//插入本地视频播放列表
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