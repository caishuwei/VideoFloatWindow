package com.csw.android.videofloatwindow.util

class Utils {
    companion object {
        fun <T, K> runIfNotNull(arg1: T?, arg2: K?, run: (arg1: T, arg2: K) -> (Unit)) {
            if (arg1 != null && arg2 != null) {
                run(arg1, arg2)
            }
        }
    }

}

