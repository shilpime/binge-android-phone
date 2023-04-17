package com.tatasky.binge.ui.features.onboarding.login.select_sid

import com.tatasky.binge.data.networking.models.response.SubscriberIdListResponse

interface SIDSelector {
    fun onSidSelect(sid: SubscriberIdListResponse.SubscriberDetail)
}