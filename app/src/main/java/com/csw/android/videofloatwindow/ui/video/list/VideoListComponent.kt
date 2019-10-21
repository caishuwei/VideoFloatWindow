package com.csw.android.videofloatwindow.ui.video.list

import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

/**
 * 定义LocalVideosMVP模块的实例池，这里注解为子实例池，这样从父实例池中取得该对象时，该对象同时含有
 * 父与子池的实例
 */
@Subcomponent(modules = [MyModule::class])
interface VideoListComponent {
    fun inject(commonVideoListFragment: CommonVideoListFragment)

    @Subcomponent.Builder
    interface Builder {

        fun build(): VideoListComponent

        @BindsInstance
        fun setView(view: VideoListContract.View): Builder
    }

}

@Module
class MyModule {

    @Provides
    fun getPresenter(view: VideoListContract.View): VideoListContract.Presenter {
        return VideoListPresenter<VideoListContract.View>(view)
    }

}
