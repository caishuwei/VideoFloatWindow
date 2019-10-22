package com.csw.android.videofloatwindow.ui.video.list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.ui.base.SwipeBackCommonActivity
import com.csw.android.videofloatwindow.ui.video.sheet.PlaySheetEditFragment
import com.csw.android.videofloatwindow.util.ScreenInfo
import com.csw.android.videofloatwindow.util.Utils
import kotlinx.android.synthetic.main.activity_local_videos.*

/**
 * 通用的VideoList碎片实现
 */
class CommonVideoListFragment : VideoListView<VideoListContract.Presenter>() {
    companion object {
        private const val REQUEST_CODE_EDIT_PLAY_SHEET = 1

        fun createData(playSheet: PlaySheet): Bundle {
            val data = Bundle()
            data.putLong("playSheetId", playSheet.id)
            data.putString("playSheetName", playSheet.name)
            return data
        }
    }

    override fun initInject() {
        MyApplication.appComponent.getVideoListComponentBuilder()
                .setView(this)
                .build()
                .inject(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EDIT_PLAY_SHEET && resultCode == Activity.RESULT_OK) {
            smartRefreshLayout.autoRefresh()
        }
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
//            toolbar.overflowIcon = Utils.getDrawableBySize(R.drawable.icon_menu_more, ScreenInfo.dp2Px(30f), ScreenInfo.dp2Px(30f))
            setHasOptionsMenu(true)//设置碎片拥有菜单
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu_video_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.finish()
            R.id.menu_add_video -> {
                Utils.runIfNotNull(playSheetId, playSheetName) { id, name ->
                    SwipeBackCommonActivity.openActivityForResult(
                            this@CommonVideoListFragment,
                            REQUEST_CODE_EDIT_PLAY_SHEET,
                            PlaySheetEditFragment::class.java,
                            PlaySheetEditFragment.createData(id, name)
                    )
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
}