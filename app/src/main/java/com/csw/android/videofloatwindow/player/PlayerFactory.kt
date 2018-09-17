package com.csw.android.videofloatwindow.player

import android.content.Context
import android.net.Uri
import com.csw.android.videofloatwindow.R
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class PlayerFactory {

    companion object {
        fun newPlayer(context: Context, filePath: String): Player {
            val bandwidthMeter = DefaultBandwidthMeter()
            val window = Timeline.Window()
            val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
            val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
            val player = ExoPlayerFactory.newSimpleInstance(
                    context,
                    trackSelector)
            val mediaDataSourceFactory = DefaultDataSourceFactory(
                    context,
                    Util.getUserAgent(context, context.getString(R.string.app_name)),
                    bandwidthMeter)
            val mediaSource = ExtractorMediaSource.Factory(mediaDataSourceFactory)
                    .createMediaSource(Uri.parse(filePath))
            player.prepare(mediaSource, true, true)
            return player
        }
    }

}