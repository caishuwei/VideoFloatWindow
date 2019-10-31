package com.csw.android.videofloatwindow.ui.video.list.local

import com.csw.android.videofloatwindow.dagger.LifecycleCallback
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Singleton

/**
 * 定义LocalVideosMVP模块的实例池，这里注解为子实例池，这样从父实例池中取得该对象时，该对象同时含有
 * 父与子池的实例
 */
@Singleton
@Subcomponent(modules = [MyModule::class])
interface LocalVideoListComponent {
    fun inject(videoListView: LocalVideoListFragment)

    @Subcomponent.Builder
    interface Builder {

        fun build(): LocalVideoListComponent

        @BindsInstance
        fun setView(view: LocalVideoListContract.View): Builder
    }

}

@Module
class MyModule {
    @Singleton
    @Provides
    fun getPresenter(localVideoListPresenter: LocalVideoListPresenter<LocalVideoListContract.View>): LocalVideoListContract.Presenter {
        return localVideoListPresenter
    }

    @Provides
    fun getLifecycleCallback(presenter: LocalVideoListContract.Presenter): LifecycleCallback {
        return presenter.getLifecycleCallback()
    }
}
