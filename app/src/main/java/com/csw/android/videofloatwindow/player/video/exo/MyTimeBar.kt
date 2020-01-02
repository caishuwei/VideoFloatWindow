package com.csw.android.videofloatwindow.player.video.exo

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.csw.android.videofloatwindow.util.ScreenInfo

class MyTimeBar(context: Context, attrs: AttributeSet) : CopyFormDefaultTimeBar(context, attrs) {

    init {
        //以下属性是可用通过xml配置实现，但我懒得写到xml中了，这里直接在视图初始化后覆盖这些属性
        barHeight = ScreenInfo.dp2Px(3f)//修改进度条高度
        scrubberDraggedSize = ScreenInfo.dp2Px(30f)//修改用户拖拽指示器时，指示器的大小
        setPlayedColor(Color.argb(255, 66, 133, 244))//已播放颜色
        setScrubberColor(Color.argb(255, 66, 133, 244))//指示器颜色
        setBufferedColor(Color.argb(255, 200, 200, 200)) //已缓存颜色
        setUnplayedColor(Color.argb(255, 150, 150, 150))//未播放颜色
        //这个Drawable由于是通过代码自定义的，如果要通过xml进行自定义drawable并修改按下和抬起时的尺寸，API要求23以上，无法适配低版本
        scrubberDrawable = MySeekBarIndicateDrawable()//设置指示器Drawable
    }

}