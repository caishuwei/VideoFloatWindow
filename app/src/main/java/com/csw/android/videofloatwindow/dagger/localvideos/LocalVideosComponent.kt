package com.csw.android.videofloatwindow.dagger.localvideos

import com.csw.android.videofloatwindow.ui.list.LocalVideosFragment
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

/**
 * 定义LocalVideosMVP模块的实例池，这里注解为子实例池，这样从父实例池中取得该对象时，该对象同时含有
 * 父与子池的实例
 */
@Subcomponent(modules = [MyModule::class])
interface LocalVideosComponent {
    fun inject(localVideosFragment: LocalVideosFragment)

    @Subcomponent.Builder
    interface Builder {

        fun build(): LocalVideosComponent

        @BindsInstance
        fun setView(view: LocalVideosContract.View): Builder
    }

}

@Module
class MyModule {

    @Provides
    fun getPresenter(localVideosPresenterImpl: LocalVideosPresenterImpl): LocalVideosContract.Presenter {
        return localVideosPresenterImpl
    }

}
