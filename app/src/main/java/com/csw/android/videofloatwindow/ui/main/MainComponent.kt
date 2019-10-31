package com.csw.android.videofloatwindow.ui.main

import com.csw.android.videofloatwindow.dagger.LifecycleCallback
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Singleton

@Singleton
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

    /**
     * dagger不会自动将MainPresenter转为MainContract.Presenter进行注入，
     *
     *
     *  @Provides dagger框架发现这个注解后会生成一个Provider<MainContract.Presenter>对象用于获取MainContract.Presenter
     *  @Singleton Component和Provides同时添加这个注解可以使每个MainComponent里产生的MainContract.Presenter实例是唯一的，
     *      看了下源码是通过将Provider<MainContract.Presenter>作为MainComponent的一个成员变量，Provider<MainContract.Presenter>
     *      在构造时就初始化了，通过DoubleCheck.provider包装一层实现get()取得的实例永远是同一个。
     *      当然，由于Provider<MainContract.Presenter>是MainComponent的成员变量，所以只在这个MainComponent里是单例，
     *      多个MainComponent每个都会拥有自己的Provider<MainContract.Presenter>
     *
     *      至于为什么在这里加单例限制而不是直接限制MainPresenter为单例，是因为MainPresenter采用构造方法注解
     *      这么做的好处是MainPresenter构造方法就算添加新的参数，也不会影响到其它调用构造方法的地方，因为是dagger去调用的构造方法
     *      所以改动会很灵活，只需要在这里添加新参数的Provider就行，或者直接在MainComponent构造时注入参数实例
     *
     *      所以要注意，这里如果要添加新的Provides，不能直接使用MainPresenter作为参数，因为这样会创建两个MainPresenter实例
     *      也不能在View那里通过@Inject直接注入MainPresenter类型变量实例，这样也会导致产生多个MainPresenter实例，
     *      都使用这里提供的接口MainContract.Presenter实例即可
     */
    @Singleton
    @Provides
    fun getPresenter(mainPresenter: MainPresenter): MainContract.Presenter {
        return mainPresenter
    }

    @Provides
    fun getLifecycleCallback(presenter: MainContract.Presenter): LifecycleCallback {
        return presenter.getLifecycleCallback()
    }
}
