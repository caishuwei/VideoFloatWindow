package com.csw.android.videofloatwindow.util

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * RxBus，用RxJava实现的一个消息订阅，发布框架。主要用于取代广播（ps，这里应该是无法完全取代广播的，
 * Android的组件可以运行在不同进程，若有进程间发消息的需要，广播还是比较好的选择）
 */
class RxBus private constructor() {

    companion object {
        private const val DEFULT_CHANNEL = "DefaultChannel"
        private val channelWhitRxBus = HashMap<String, RxBus>()

        /**
         * 获取默认的RxBus实例
         */
        fun getDefault(): RxBus {
            return getRxBusByChannel(DEFULT_CHANNEL)
        }

        /**
         * 获取指定渠道的RxBus实例
         */
        fun getRxBusByChannel(channel: String): RxBus {
            val rxBus = channelWhitRxBus[channel]
            if (rxBus != null) {
                return rxBus
            } else {
                val newInstance = RxBus()
                channelWhitRxBus[channel] = newInstance
                return newInstance
            }
        }

    }

    //PublishSubject 用于事件发布与注册观察者，事件无粘性
    //BehaviorSubject 最后一次事件带粘性
    //ReplaySubject 所有事件都带粘性，新注册的观察者会接受之前发送的所有事件
    //序列化传输的数据可以实现线程安全，但速度会比较慢
    private val subject = PublishSubject.create<Any>().toSerialized()

    /**
     * 发出一个事件
     */
    fun post(event: Any) {
        subject.onNext(event)
    }

    /**
     * 根据事件类型获取被观察者，通过订阅观察者可以在该类型事件发生时得到回调，注意即时注销监听，否则内存泄露
     */
    fun <T> toObservable(eventType: Class<T>): Observable<T> {
        return subject.ofType(eventType)
    }
}