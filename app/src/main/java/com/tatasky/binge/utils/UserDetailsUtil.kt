package com.tatasky.binge.utils

import com.tatasky.binge.analytics.USER_TYPE_GUEST
import com.tatasky.binge.analytics.USER_TYPE_NON_DTH
import com.tatasky.binge.analytics.USER_TYPE_TP

object UserDetailsUtil {
    fun getUserType(dthStatus: String?) =
        when (dthStatus) {
            DTH_W_BINGE_OLD_USER, DTH_W_BINGE_NEW_USER, DTH_WO_BINGE_USER ->
                USER_TYPE_TP
            NON_DTH_USER ->
                USER_TYPE_NON_DTH
            else ->
                USER_TYPE_GUEST
        }
}