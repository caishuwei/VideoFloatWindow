package com.csw.android.videofloatwindow.dagger.localvideos

import com.csw.android.videofloatwindow.dagger.base.BasePresenterImpl
import javax.inject.Inject

/**
 * 本地视频MVP切面实现
 *
 * Inject注解，将本类的实例构建方式注入Dagger框架中
 */
class LocalVideosPresenterImpl @Inject constructor(view: LocalVideosContract.View) :
        BasePresenterImpl<LocalVideosContract.Presenter, LocalVideosContract.View>(view),
        LocalVideosContract.Presenter {



}