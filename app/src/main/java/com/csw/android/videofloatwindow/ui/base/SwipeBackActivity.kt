package com.csw.android.videofloatwindow.ui.base

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.util.ScreenInfo
import kotlinx.android.synthetic.main.activity_swipe_back.*

open class SwipeBackActivity : BaseActivity() {
    private lateinit var firstView: View //第一页透明
    private lateinit var secondView: View//第二页，用于显示内容
    //添加内容页面的容器，可以加碎片也可以加视图
    private lateinit var fl_fragment_container: FrameLayout

    override fun getContentViewID(): Int {
        return R.layout.activity_swipe_back
    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        super.initView(rootView, savedInstanceState)
        //修改ViewPager页面绘制位置
//        viewpager.setPageTransformer(false) { page, position ->
//            if (position < -1) {
//                page.translationX = 0f
//            } else if (position <= 1) {
//                if (position < 0) {
//                    //-1~0
//                    page.translationX = ScreenInfo.WIDTH * position * -1
//                }
//            } else {
//                page.translationX = 0f
//            }
//        }

        firstView = View(this)
        secondView = layoutInflater.inflate(
                R.layout.view_swipe_back,
                window?.decorView as ViewGroup,
                false)
        fl_fragment_container = secondView.findViewById<FrameLayout>(R.id.fl_fragment_container)
    }

    override fun initAdapter() {
        super.initAdapter()
        viewpager.adapter = object : PagerAdapter() {
            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                if (position == 0) {
                    container.addView(firstView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                    return firstView
                } else if (position == 1) {
                    container.addView(secondView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                    return secondView
                }
                return super.instantiateItem(container, position)
            }

            override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
                if (obj is View) {
                    container.removeView(obj)
                }
            }

            override fun isViewFromObject(view: View, obj: Any): Boolean {
                return view == obj
            }

            override fun getCount(): Int {
                return 2
            }
        }
    }

    override fun initListener() {
        super.initListener()
        //滑动到第一页则结束界面
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    finish()
                }
            }

        })
    }

    override fun initData() {
        super.initData()
        //无动画快速切换到第二页
        viewpager.setCurrentItem(1, false)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event) || true
    }
}