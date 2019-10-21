package com.csw.android.videofloatwindow.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.Constants
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.ui.base.BaseMVPActivity
import com.csw.android.videofloatwindow.ui.base.SwipeBackCommonActivity
import com.csw.android.videofloatwindow.ui.video.list.CommonVideoListFragment
import com.csw.android.videofloatwindow.ui.video.list.local.LocalVideoListFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseMVPActivity<MainContract.Presenter>(), MainContract.View {


    private lateinit var playSheetAdapter: PlaySheetAdapter

    override fun initInject() {
        MyApplication.appComponent.getMainComponentBuilder()
                .setView(this)
                .build()
                .inject(this)
    }

    override fun getContentViewID(): Int {
        return R.layout.activity_main
    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        super.initView(rootView, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    override fun initAdapter() {
        super.initAdapter()
        playSheetAdapter = PlaySheetAdapter()
        recyclerView.adapter = playSheetAdapter
    }

    override fun initListener() {
        super.initListener()
        playSheetAdapter.enableSwipeItem()
        playSheetAdapter.setOnItemClickListener { _, _, position ->
            val playSheet = playSheetAdapter.getItem(position)
            playSheet?.let {
                if (it.name == Constants.LOCAL_VIDEO_PLAY_SHEET) {
                    SwipeBackCommonActivity.openActivity(this@MainActivity, LocalVideoListFragment::class.java, LocalVideoListFragment.createData(it))
                } else {
                    SwipeBackCommonActivity.openActivity(this@MainActivity, CommonVideoListFragment::class.java, CommonVideoListFragment.createData(it))
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val playSheet = playSheetAdapter.getItem(viewHolder.adapterPosition)
                playSheet?.let {
                    if (Constants.LOCAL_VIDEO_PLAY_SHEET == it.name) {
                        //本地视频，不能删除
                        return ItemTouchHelper.ACTION_STATE_IDLE
                    }
                }
                //只能向左滑动删除
                return makeFlag(ACTION_STATE_IDLE, LEFT) or makeFlag(ACTION_STATE_SWIPE, LEFT)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                playSheetAdapter.getItem(viewHolder.adapterPosition)?.let {
                    if (Constants.LOCAL_VIDEO_PLAY_SHEET != it.name) {
//                        playSheetAdapter.data.remove(it)
//                        playSheetAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                        presenter.removePlaySheet(it)
                    }
                }
            }

        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
        smartRefreshLayout.setEnableRefresh(true)
        smartRefreshLayout.setEnableLoadMore(false)
        smartRefreshLayout.setOnRefreshListener {
            playSheetAdapter.setNewData(null)
            presenter.loadPlaySheets()
        }
        floatActionBottom.setOnClickListener {
            //添加歌单
            val input = AppCompatEditText(this@MainActivity)
            input.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            input.gravity = Gravity.CENTER
            input.maxLines = 1
            AlertDialog.Builder(this@MainActivity)
                    .setTitle("创建一个歌单")
                    .setView(input)
                    .setNegativeButton("取消") { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("创建") { dialog, which ->
                        val name = input.text.toString().trim()
                        if (name.isEmpty()) {
                            Snackbar.make(smartRefreshLayout, "名称不能为空", Snackbar.LENGTH_SHORT).show()
                        } else if (presenter.isPlaySheetExist(name)) {
                            Snackbar.make(smartRefreshLayout, "该名称已被使用", Snackbar.LENGTH_SHORT).show()
                        } else {
                            presenter.addPlaySheet(PlaySheet(name))
                            dialog.dismiss()
                        }
                    }
                    .create().show()
        }
    }

    override fun initData() {
        super.initData()
        smartRefreshLayout.autoRefresh()
        PlayHelper.backgroundPlay = true
    }

    override fun finish() {
        super.finish()
        //退出app，使用系统动画淡出
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun refreshPlaySheetList() {
        smartRefreshLayout.autoRefresh()
    }

    override fun updatePlaySheets(playSheets: List<PlaySheet>) {
        playSheetAdapter.setNewData(playSheets)
    }

    override fun onLoadPlaySheetsSucceed() {
        smartRefreshLayout.finishRefresh(true)
    }

    override fun onLoadPlaySheetsFailed(errorMsg: String) {
        smartRefreshLayout.finishRefresh(false)
        Snackbar.make(recyclerView, errorMsg, Snackbar.LENGTH_SHORT).show()
    }

}