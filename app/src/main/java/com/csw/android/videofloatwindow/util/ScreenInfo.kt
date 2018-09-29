package com.csw.android.videofloatwindow.util

import android.content.res.Resources
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.COMPLEX_UNIT_SP

class ScreenInfo {
    companion object {
        val WIDTH = Resources.getSystem().displayMetrics.widthPixels
        val HEIGHT = Resources.getSystem().displayMetrics.heightPixels
        val DENSITY = Resources.getSystem().displayMetrics.density
        val SCALED_DENSITY = Resources.getSystem().displayMetrics.scaledDensity

        fun dp2Px(dpValue: Float): Int {
            return (TypedValue.applyDimension(COMPLEX_UNIT_DIP, dpValue, Resources.getSystem().displayMetrics) + .5f).toInt()
        }

        fun sp2Px(spValue: Float): Int {
            return (TypedValue.applyDimension(COMPLEX_UNIT_SP, spValue, Resources.getSystem().displayMetrics) + .5f).toInt()
        }

        fun px2Dp(pxValue: Float): Int {
            return (pxValue / DENSITY + .5f).toInt()
        }

        fun px2Sp(pxValue: Float): Int {
            return (pxValue / SCALED_DENSITY + .5f).toInt()
        }
    }

}