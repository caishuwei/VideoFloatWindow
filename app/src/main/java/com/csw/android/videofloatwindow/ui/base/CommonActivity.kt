package com.csw.android.videofloatwindow.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.csw.android.videofloatwindow.R

class CommonActivity : BaseActivity() {

    companion object {
        val FRAGMENT_TAG = "common_activity_fragment_tag"
        fun <T : Fragment> openActivity(context: Context, clazz: Class<T>, data: Bundle?) {
            val intent = Intent(context, CommonActivity::class.java)
            intent.putExtra("clazz", clazz)
            data?.let {
                intent.putExtra("data", it)
            }
            context.startActivity(intent)
        }

    }

    override fun getContentViewID(): Int {
        return R.layout.activity_common
    }

    override fun initData() {
        super.initData()

        val clazz = intent.getSerializableExtra("clazz")
        val data = intent.getBundleExtra("data")
        if (clazz != null && clazz is Class<*>) {
            var fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
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
                    supportFragmentManager.beginTransaction().add(R.id.fl_fragment_container, fragment, FRAGMENT_TAG).commitAllowingStateLoss()
                }
                if (fragment.isDetached) {
                    supportFragmentManager.beginTransaction().attach(fragment).commitAllowingStateLoss()
                }
                return
            }
        }
        finish()
    }
}