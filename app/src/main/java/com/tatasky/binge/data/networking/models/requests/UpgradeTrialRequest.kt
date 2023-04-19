package com.tatasky.binge.data.networking.models.requests

data class UpgradeTrialRequest(
    val basePackId: String,
    val subscriberId: String,
    val baId : String
)