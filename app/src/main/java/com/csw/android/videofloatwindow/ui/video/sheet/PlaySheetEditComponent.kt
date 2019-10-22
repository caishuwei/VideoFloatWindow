package com.csw.android.videofloatwindow.ui.video.sheet

import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

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

    @Provides
    fun getPresenter(view: PlaySheetEditContract.View): PlaySheetEditContract.Presenter {
        return PlaySheetEditPresenter(view)
    }
}