package com.csw.android.videofloatwindow.ui.video.sheet

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.ui.base.BaseMVPFragment
import com.csw.android.videofloatwindow.util.ScreenInfo
import com.csw.android.videofloatwindow.util.Utils
import kotlinx.android.synthetic.main.fragment_play_sheet_edit.*

class PlaySheetEditFragment : BaseMVPFragment<PlaySheetEditContract.Presenter>(), PlaySheetEditContract.View {

    companion object {
        fun createData(id: Long, name: String): Bundle {
            val data = Bundle()
            data.putLong("playSheetId", id)
            data.putString("playSheetName", name)
            return data
        }
    }

    private var adapter1: PlaySheetEditVideoListAdapter? = null
    private var adapter2: PlaySheetEditVideoListAdapter? = null
    override fun initInject() {
        MyApplication.appComponent.getPlaySheetEditComponentBuilder().setView(this).build().inject(this)
    }

    override fun getContentViewID(): Int {
        return R.layout.fragment_play_sheet_edit
    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        super.initView(rootView, savedInstanceState)
        val activity = activity
        if (activity is AppCompatActivity) {
            activity.setSupportActionBar(toolbar)
            activity.supportActionBar?.setHomeButtonEnabled(true)
            toolbar.title = "歌单编辑"
            toolbar.navigationIcon = Utils.getDrawableBySize(R.drawable.icon_menu_back, ScreenInfo.dp2Px(30f), ScreenInfo.dp2Px(30f))
            setHasOptionsMenu(true)//设置碎片拥有菜单
            toolbar.inflateMenu(R.menu.toolbar_menu_play_sheet_edit)//填充菜单
        }
        recyclerView1.layoutManager = LinearLayoutManager(rootView.context, RecyclerView.VERTICAL, false)
        recyclerView2.layoutManager = LinearLayoutManager(rootView.context, RecyclerView.VERTICAL, false)
    }


    override fun initAdapter() {
        super.initAdapter()
        adapter1 = PlaySheetEditVideoListAdapter(this)
        recyclerView1.adapter = adapter1

        adapter2 = PlaySheetEditVideoListAdapter(this)
        recyclerView2.adapter = adapter2
    }

    override fun initListener() {
        super.initListener()
        val itemTouchHelper = ItemTouchHelper(ItemDragAndSwipeCallback(adapter1))
        itemTouchHelper.attachToRecyclerView(recyclerView1)
        adapter1?.enableDragItem(itemTouchHelper)
        adapter1?.setOnItemClickListener { _, _, position ->
            val vi = adapter1?.data?.removeAt(position)
            adapter1?.notifyItemRemoved(position)
            Utils.runIfNotNull(vi, adapter2) { videoInfo, ad ->
                ad.data.add(videoInfo)
                ad.notifyItemInserted(ad.data.size)
            }
        }
        adapter2?.setOnItemClickListener { _, _, position ->
            val vi = adapter2?.data?.removeAt(position)
            adapter2?.notifyItemRemoved(position)
            Utils.runIfNotNull(vi, adapter1) { videoInfo, ad ->
                ad.data.add(videoInfo)
                ad.notifyItemInserted(ad.data.size)
            }
        }
    }

    private var playSheetId: Long? = null

    override fun initData() {
        super.initData()
        playSheetId = arguments?.getLong("playSheetId")
//        val playSheetName = arguments?.getString("playSheetName")

        playSheetId?.let {
            presenter.setPlaySheetId(it)
            presenter.initUIData()
            return
        }
        activity?.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu_play_sheet_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.finish()
            R.id.menu_save -> {
                val videos = adapter1?.data
                Utils.runIfNotNull(playSheetId, videos) { id, videoList ->
                    //存储编辑后的歌单
                    presenter.savePlaySheetVideos(videoList)
                    activity?.setResult(Activity.RESULT_OK)
                    activity?.finish()
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onDestroyView() {
        adapter1 = null
        adapter2 = null
        super.onDestroyView()
    }

    //View>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    override fun updatePlaySheet1(videoInfoList: ArrayList<VideoInfo>) {
        adapter1?.setNewData(videoInfoList)
        adapter1?.notifyDataSetChanged()
    }

    override fun updatePlaySheet2(videoInfoList: ArrayList<VideoInfo>) {
        adapter2?.setNewData(videoInfoList)
        adapter2?.notifyDataSetChanged()
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
}