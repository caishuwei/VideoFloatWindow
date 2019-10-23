package com.csw.android.videofloatwindow.ui.video.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.ui.base.SwipeBackCommonActivity
import com.csw.android.videofloatwindow.ui.video.sheet.PlaySheetEditFragment
import com.csw.android.videofloatwindow.util.ScreenInfo
import com.csw.android.videofloatwindow.util.Utils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_video_list.*

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
            edt_title.setText(playSheetName)
            edt_title.imeOptions = EditorInfo.IME_ACTION_DONE//设置输入法右下角按钮功能
            edt_title.setImeActionLabel("完成", EditorInfo.IME_ACTION_DONE)//定制按钮文本
            edt_title.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.action == ACTION_UP && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    val newName = edt_title.text.toString().trim()
                    if (TextUtils.isEmpty(newName)) {
                        Snackbar.make(toolbar, "歌单名称不可为空", Snackbar.LENGTH_SHORT).show()
                        edt_title.requestFocus()
                    } else if (presenter.isPlaySheetExist(newName)) {
                        Snackbar.make(toolbar, "名称已被使用", Snackbar.LENGTH_SHORT).show()
                        edt_title.requestFocus()
                    } else {
                        this@CommonVideoListFragment.playSheetName = newName
                        presenter.updatePlaySheetName(playSheetId, newName)
                        edt_title.clearFocus()
                        //关闭已打开的键盘
                        val inputMethodManager = MyApplication.instance.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                    return@OnEditorActionListener true
                }
                true
            })
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