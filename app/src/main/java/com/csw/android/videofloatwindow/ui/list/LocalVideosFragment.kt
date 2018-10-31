package com.csw.android.videofloatwindow.ui.list

import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.dagger.localvideos.LocalVideosContract
import com.csw.android.videofloatwindow.ui.base.BaseFragment
import com.csw.android.videofloatwindow.ui.base.MvpFragment
import javax.inject.Inject

class LocalVideosFragment() : MvpFragment(), LocalVideosContract.View {

    companion object {
        fun newInstance(): LocalVideosFragment {
            return LocalVideosFragment()
        }
    }

    @Inject
    lateinit var presenter: LocalVideosContract.Presenter

    init {
        //初始化时，注入切片对象
        MyApplication.appComponent.getLocalVideosComponentBuilder()
                .setView(this)
                .build()
                .inject(this)
    }

    override fun getContentViewID(): Int {
        return R.layout.activity_local_videos
    }

}