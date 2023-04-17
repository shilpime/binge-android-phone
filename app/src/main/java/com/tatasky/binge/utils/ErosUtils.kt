package com.tatasky.binge.utils

import android.content.Context
import com.erosnow.partner.ENSDK
import com.erosnow.partner.`interface`.EnLoginListener
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.ttn.ttnplayer.R

fun erosnowLogin(context: Context, enListener : EnLoginListener, dsn : String, token : String) {
    if(!ENSDK.getLoggedIn()) {
        ENSDK.ssoLogin(
            context,
            dsn,
            token,
            enListener
        )
    }
}


fun buildDataSourceFactory(context: Context): DataSource.Factory {
    val BANDWIDTH_METER =  DefaultBandwidthMeter.Builder(context).build()
    val dataSourceFactory = DefaultDataSourceFactory(
        context,
        Util.getUserAgent(context, context.getString(R.string.app_name)),
        BANDWIDTH_METER
    )
    return DefaultDataSourceFactory(context, BANDWIDTH_METER, dataSourceFactory)
}
