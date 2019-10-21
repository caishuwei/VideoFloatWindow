package com.csw.android.videofloatwindow.ui.main

import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [MyModule::class])
interface MainComponent {

    fun inject(mainActivity: MainActivity)

    @Subcomponent.Builder
    interface Builder {

        fun build(): MainComponent

        @BindsInstance
        fun setView(view: MainContract.View): Builder
    }

}

@Module
class MyModule {

    @Provides
    fun getPresenter(view: MainContract.View): MainContract.Presenter {
        return MainPresenter(view)
    }

}
