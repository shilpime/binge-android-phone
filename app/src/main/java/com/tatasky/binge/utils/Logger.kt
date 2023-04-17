package com.tatasky.binge.utils

import android.util.Log
import com.tatasky.binge.BuildConfig


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