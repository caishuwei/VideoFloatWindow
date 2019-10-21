package com.csw.android.videofloatwindow.app

import android.app.Application
import android.content.Context
import com.csw.android.videofloatwindow.ui.main.MainComponent
import com.csw.android.videofloatwindow.ui.video.list.VideoListComponent
import com.csw.android.videofloatwindow.ui.video.list.local.LocalVideoListComponent
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides


/**
 * App依赖注入，实例工厂
 */
@Component(modules = [AppModule::class])
interface AppComponent {

    //添加子实例池构建器
    fun getMainComponentBuilder(): MainComponent.Builder
    fun getVideoListComponentBuilder(): VideoListComponent.Builder

    fun getLocalVideoListComponentBuilder(): LocalVideoListComponent.Builder

    /**
     * 实例工厂构建，可以先注入一些已存的实例
     */
    @Component.Builder
    interface Builder {

        /**
         * 构建MyApplicationComponent
         */
        fun build(): AppComponent

        /**
         * 注入已经存在的实例到AppComponent中，以便程序中使用
         */
        @BindsInstance
        fun setMyApplication(myApplication: MyApplication): Builder
    }
}

/**
 * 实例生成器，module的作用是，当要从Component中获取不存在的实例时，Component可以通过module来生成实例
 */
@Module
class AppModule {

    /**
     * Provides注解这个方法，表明这个方法可以提供返回值类型的实例，调用方法需要的参数是从Component中
     * 获取的。
     *
     * 可以使用一些注解，达到单例。当需要区分两个返回类型一样的Provider时，可以使用注解进行标记，这样
     * 当需要往某个对象注入实例时，该对象使用相同的注解即可获取到想要的实例。
     */
    @Provides
    fun getApplication(myApplication: MyApplication): Application {
        return myApplication
    }

    @Provides
    fun getContext(application: Application): Context {
        return application
    }
}