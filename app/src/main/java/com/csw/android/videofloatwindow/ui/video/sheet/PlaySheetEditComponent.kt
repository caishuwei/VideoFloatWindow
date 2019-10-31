package com.csw.android.videofloatwindow.ui.video.sheet

import com.csw.android.videofloatwindow.dagger.LifecycleCallback
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Singleton

@Singleton
@Subcomponent(modules = [MyModule::class])
interface PlaySheetEditComponent {

    fun inject(playSheetEditFragment: PlaySheetEditFragment)

    @Subcomponent.Builder
    interface Builder {

        fun build(): PlaySheetEditComponent

        @BindsInstance
        fun setView(view: PlaySheetEditContract.View): Builder
    }
}

@Module
class MyModule {
    @Singleton
    @Provides
    fun getPresenter(playSheetEditPresenter: PlaySheetEditPresenter): PlaySheetEditContract.Presenter {
        return playSheetEditPresenter
    }

    @Provides
    fun getLifecycleCallback(presenter: PlaySheetEditContract.Presenter): LifecycleCallback {
        return presenter.getLifecycleCallback()
    }
}