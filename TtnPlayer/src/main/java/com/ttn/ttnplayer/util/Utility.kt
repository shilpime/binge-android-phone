package com.ttn.ttnplayer.util

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import com.ttn.ttnplayer.BuildConfig
import kotlin.math.roundToInt

val L3_MAX_BITRATE = 800000
const val SECURITY_LEVEL_L3 = "L3"
const val SECURITY_LEVEL_L1 = "L1"
/*
 Print code log in console
 */
fun d(Tag: String, msg: String?) {
    if(BuildConfig.DEBUG)
        if (msg != null)
            Log.e(Tag, msg)
}
/*
 Print code log in console
 */
fun e(Tag: String, msg: String?) {
    if(BuildConfig.DEBUG)
        if (msg != null)
            Log.e(Tag, msg)
}

fun dpToPx(dp: Int, mContext:Context): Int {
    val displayMetrics: DisplayMetrics = mContext.resources.displayMetrics
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}