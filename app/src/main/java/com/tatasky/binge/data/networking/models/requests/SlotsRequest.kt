package com.tatasky.binge.data.networking.models.requests

import com.tatasky.binge.data.networking.models.response.SlotSuggestion

data class SlotsRequest(
    val contactPoint: ContactPoint,
    val locationAddress: LocationAddress,
    val slot: SlotSuggestion,
    val teamId: String,
    val taskId: String?=null
)

data class ContactPoint(
    val name: String,
    val number: String
)

data class LocationAddress(
    var city: String?=null,
    var formattedAddress: String?=null,
    var id: String="",
    var pincode: String?=null,
    var state: String?=null
)