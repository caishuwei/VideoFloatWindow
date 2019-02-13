package com.csw.android.videofloatwindow.ui.list

import android.Manifest
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.widget.PopupWindowCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.AddItem
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.ui.FullScreenActivity
import com.csw.android.videofloatwindow.ui.base.BaseActivity
import com.csw.android.videofloatwindow.util.DBUtils
import com.csw.android.videofloatwindow.view.SpaceLineItemDecoration
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_local_videos.*

class LocalVideosActivity : BaseActivity() {

    private lateinit var videosAdapter: VideosAdapter


    override fun getContentViewID(): Int {
        return R.layout.activity_local_videos;
    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        super.initView(rootView, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false)

    }

    override fun initAdapter() {
        super.initAdapter()
        videosAdapter = VideosAdapter()
        recyclerView.adapter = videosAdapter
    }

    override fun initListener() {
        super.initListener()
        videosAdapter.setOnItemClickListener { _, view, position ->
            val videoInfo = videosAdapter.getItem(position)
            videoInfo?.let {
                FullScreenActivity.openActivity(view.context, it)
            }
        }
        videosAdapter.setOnItemLongClickListener { _, view, position ->
            val videoInfo = videosAdapter.getItem(position)
            videoInfo?.let {
                showAddVideoInfoPopupWindow(view, it)
                return@setOnItemLongClickListener true
            }
            return@setOnItemLongClickListener false
        }
        smartRefreshLayout.isEnableRefresh = true
        smartRefreshLayout.isEnableLoadMore = false
        smartRefreshLayout.setOnRefreshListener {
            videosAdapter.setNewData(null)
            requestLocalVideos()
        }
    }

    override fun initData() {
        super.initData()
        smartRefreshLayout.autoRefresh()
    }

    private fun requestLocalVideos() {
        addLifecycleTask(
                RxPermissions(this)
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .map {
                            if (it) {
                                return@map getLocalVideos()
                            } else {
                                Snackbar.make(recyclerView, "SD卡文件读取权限被拒绝", Snackbar.LENGTH_SHORT).show()
                                return@map arrayListOf<VideoInfo>()
                            }
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    smartRefreshLayout.finishRefresh()
                                    videosAdapter.setNewData(it)
                                    MyApplication.instance.playerHelper.playList = it
                                },
                                {
                                    smartRefreshLayout.finishRefresh()
                                    Snackbar.make(recyclerView, it.message
                                            ?: "未知异常", Snackbar.LENGTH_SHORT).show()
                                }
                        )
        );
    }

    /**
     * 查询设备媒体库获取所有视频文件数据记录，数据量大的时候耗时较长
     */
    private fun getLocalVideos(): ArrayList<VideoInfo> {
        val data = arrayListOf<VideoInfo>()
        val cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,//要查询的列，null所有列
                null,//筛选条件（用占位符代替参数值）
                null,//筛选参数
                MediaStore.Video.Media.DATE_MODIFIED + " DESC "//按数据修改日期反向排序
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    data.add(VideoInfo.readFromCursor(cursor))
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return data
    }

    private fun showAddVideoInfoPopupWindow(view: View, videoInfo: VideoInfo) {
        val popupWindow = PopupWindow()
        val recyclerView = RecyclerView(view.context)
        recyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(object : SpaceLineItemDecoration(0, 1, 0, 0, Color.GRAY) {
            override fun skipDraw(position: Int): Boolean {
                return position == 0
            }
        })
        val popupWindowPlaySheetAdapter = PopupWindowPlaySheetAdapter(getPlaySheets())
        popupWindowPlaySheetAdapter.setOnItemClickListener { _, view, position ->
            val item = popupWindowPlaySheetAdapter.getItem(position)
            item?.let { it ->
                when (item.itemType) {
                    AddItem.ITEM_TYPE -> {
                        val inputPlaySheetName = AppCompatEditText(view.context)
                        inputPlaySheetName.hint = "输入列表名称"
                        AlertDialog.Builder(view.context)
                                .setTitle("添加播放列表")
                                .setView(inputPlaySheetName)
                                .setNegativeButton("取消") { dialog, _ ->
                                    dialog.cancel()
                                }
                                .setPositiveButton("创建") { dialog, _ ->
                                    val name = inputPlaySheetName.text.toString().trim()
                                    if (TextUtils.isEmpty(name)) {
                                        Snackbar.make(view, "列表名称不能为空", Snackbar.LENGTH_LONG).show()
                                    } else if (DBUtils.isPlaySheetExists(name)) {
                                        Snackbar.make(view, "该列表已经存在", Snackbar.LENGTH_LONG).show()
                                    } else {
                                        DBUtils.insetPlaySheet(PlaySheet(name))
                                        Snackbar.make(view, "列表创建成功", Snackbar.LENGTH_LONG).show()
                                        dialog.dismiss()
                                        popupWindowPlaySheetAdapter.setNewData(getPlaySheets())
                                    }
                                }
                                .create()
                                .show()
                    }
                    PlaySheet.ITEM_TYPE -> {
                        val playSheet = it as PlaySheet
                        DBUtils.insertVideoInfo(videoInfo)
                        if (DBUtils.isVideoInPlaySheet(playSheet.id, videoInfo.id)) {
                            Snackbar.make(view, "${playSheet.name} 已经含有该视频", Snackbar.LENGTH_LONG).show()
                        } else {
                            DBUtils.insetPlaySheetVideo(playSheet.id, videoInfo.id)
                            Snackbar.make(view, "添加成功", Snackbar.LENGTH_SHORT).show()
                            popupWindow.dismiss()
                        }
                    }
                }
            }
        }
        recyclerView.adapter = popupWindowPlaySheetAdapter
        recyclerView.setBackgroundColor(Color.WHITE)
        popupWindow.contentView = recyclerView
        popupWindow.width = WindowManager.LayoutParams.MATCH_PARENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.isFocusable = true//窗口如果不取得焦点，点击外部时弹窗消失，但点击事件仍会被外部控件响应
        popupWindow.isTouchable = true//设置窗口可以响应触摸事件
        popupWindow.isOutsideTouchable = true//设置窗口外部可以响应触摸事件
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))//设置背景了，窗口外部才可以响应触摸事件
        PopupWindowCompat.setOverlapAnchor(popupWindow, true)//设置窗体位置覆盖在View上
        PopupWindowCompat.showAsDropDown(popupWindow, view, 0, 0, Gravity.CENTER);//显示窗体
    }

    private fun getPlaySheets(): ArrayList<MultiItemEntity> {
        val result = arrayListOf<MultiItemEntity>(AddItem())
        result.addAll(DBUtils.getPlaySheets())
        return result
    }
}