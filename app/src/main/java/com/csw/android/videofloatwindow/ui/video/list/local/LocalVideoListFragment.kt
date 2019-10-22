package com.csw.android.videofloatwindow.ui.video.list.local

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.ui.video.list.VideoListView
import com.csw.android.videofloatwindow.util.ScreenInfo
import com.csw.android.videofloatwindow.util.Utils
import kotlinx.android.synthetic.main.activity_local_videos.*

class LocalVideoListFragment : VideoListView<LocalVideoListContract.Presenter>(), LocalVideoListContract.View {

    companion object {
        fun createData(playSheet: PlaySheet): Bundle {
            val data = Bundle()
            data.putLong("playSheetId", playSheet.id)
            data.putString("playSheetName", playSheet.name)
            return data
        }
    }

    private var scanLocalVideosAfterFirstLoad = true
    override fun initInject() {
        MyApplication.appComponent.getLocalVideoListComponentBuilder()
                .setView(this)
                .build()
                .inject(this)
    }

    //TitleBar>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    override fun setupTitleBar(playSheetName: String?) {
        val activity = activity
        if (activity is AppCompatActivity) {
            activity.setSupportActionBar(toolbar)
            activity.supportActionBar?.setHomeButtonEnabled(true)
            toolbar.title = playSheetName
            toolbar.navigationIcon = Utils.getDrawableBySize(R.drawable.icon_menu_back, ScreenInfo.dp2Px(30f), ScreenInfo.dp2Px(30f))
            toolbar.inflateMenu(R.menu.toolbar_menu_local_videos)//填充菜单
            toolbar.overflowIcon = Utils.getDrawableBySize(R.drawable.icon_menu_more, ScreenInfo.dp2Px(30f), ScreenInfo.dp2Px(30f))
            setHasOptionsMenu(true)//设置碎片拥有菜单
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu_local_videos, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.finish()
            R.id.menu_scan -> {
                presenter.scanLocalVideos(playSheetId)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    //View>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    override fun onLoadPlaySheetFailed(errorMsg: String) {
        super.onLoadPlaySheetFailed(errorMsg)
        if (scanLocalVideosAfterFirstLoad) {
            presenter.scanLocalVideos(playSheetId)
            scanLocalVideosAfterFirstLoad = false
        }
    }

    override fun onLoadPlaySheetSucceed() {
        super.onLoadPlaySheetSucceed()
        if (scanLocalVideosAfterFirstLoad) {
            presenter.scanLocalVideos(playSheetId)
            scanLocalVideosAfterFirstLoad = false
        }
    }

    override fun onScanLocalVideosStart() {
        val scanItem = toolbar.menu.findItem(R.id.menu_scan)
        scanItem.icon = ContextCompat.getDrawable(MyApplication.instance, R.drawable.icon_menu_scan_disable)
        scanItem.setEnabled(false)
    }

    override fun onScanLocalVideosFailed(errorMsg: String) {
        val scanItem = toolbar.menu.findItem(R.id.menu_scan)
        scanItem.icon = ContextCompat.getDrawable(MyApplication.instance, R.drawable.icon_menu_scan)
        scanItem.setEnabled(true)
    }

    override fun onScanLocalVideosSucceed() {
        val scanItem = toolbar.menu.findItem(R.id.menu_scan)
        scanItem.icon = ContextCompat.getDrawable(MyApplication.instance, R.drawable.icon_menu_scan)
        scanItem.setEnabled(true)
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
}