package com.tatasky.binge.ui.features.onboarding.login.select_baid

import com.tatasky.binge.data.networking.models.response.LoginResponse

interface BAIDSelector {
    fun onSidSelect(baid: LoginResponse.BingeSubscription)
}