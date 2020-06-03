package com.csw.android.videofloatwindow.player.service

import android.app.Activity
import android.os.Bundle
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.ui.main.MainActivity

/**
 * 通知点击 通过PendingIntent.getActivity打开Activity时，才会收起通知栏
 * 这个Activity要在新的栈中打开，并关闭，这样finish才不会返回App已经打开的界面
 */
class CloseNotificationBarActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        actionBar?.hide()
        super.onCreate(savedInstanceState)
        when (intent.action) {
            PlayService.ACTION_NOTIFICATION_CLICK -> {
                MainActivity.openActivity(this)
            }
            PlayService.ACTION_PLAY_NEXT -> PlayHelper.tryPlayNext()
            PlayService.ACTION_PLAY_PREVIOUS -> PlayHelper.tryPlayPrevious()
            PlayService.ACTION_PLAY_CURR -> PlayHelper.tryPlayCurr()
            PlayService.ACTION_PAUSE_CURR -> PlayHelper.tryPauseCurr()
            PlayService.ACTION_FULL_SCREEN -> PlayHelper.tryPlayInFullScreen()
            PlayService.ACTION_FLOAT_WINDOW -> PlayHelper.tryPlayInFloatWindow()
        }
        finish()
    }

}