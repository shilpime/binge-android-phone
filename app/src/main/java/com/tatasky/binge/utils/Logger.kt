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

val Any.classNameTag: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }