package com.csw.android.videofloatwindow.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.csw.android.videofloatwindow.R

class SwipeBackCommonActivity : SwipeBackActivity() {

    companion object {
        private val FRAGMENT_TAG = "common_activity_fragment_tag"
        fun <T : Fragment> openActivity(context: Context, clazz: Class<T>, data: Bundle?) {
            val intent = Intent(context, SwipeBackCommonActivity::class.java)
            intent.putExtra("clazz", clazz)
            data?.let {
                intent.putExtra("data", it)
            }
            context.startActivity(intent)
        }

        fun <T : Fragment>openActivityForResult(fragment: Fragment, requestCode: Int, clazz: Class<T>, data: Bundle?) {
            val intent = Intent(fragment.context, SwipeBackCommonActivity::class.java)
            intent.putExtra("clazz", clazz)
            data?.let {
                intent.putExtra("data", it)
            }
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    override fun initData() {
        super.initData()
        //等候第二页添加到ViewPager中才可以用来添加碎片
        window?.decorView?.post {
            val clazz = intent.getSerializableExtra("clazz")
            val data = intent.getBundleExtra("data")
            if (clazz != null && clazz is Class<*>) {
                var fragment = supportFragmentManager.findFragmentByTag(CommonActivity.FRAGMENT_TAG)
                if (fragment == null) {
                    val newInstance = clazz.newInstance()
                    if (newInstance is Fragment) {
                        fragment = newInstance
                        data?.let {
                            fragment.arguments = it
                        }
                    }
                }
                if (fragment != null) {
                    if (!fragment.isAdded) {
                        supportFragmentManager.beginTransaction().add(R.id.fl_fragment_container, fragment, CommonActivity.FRAGMENT_TAG).commitAllowingStateLoss()
                    }
                    if (fragment.isDetached) {
                        supportFragmentManager.beginTransaction().attach(fragment).commitAllowingStateLoss()
                    }
                    return@post
                }
            }
            finish()
        }
    }
}