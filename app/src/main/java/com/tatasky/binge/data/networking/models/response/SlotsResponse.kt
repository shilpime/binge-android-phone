package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class SlotsResponse : BaseResponse() {
    @SerializedName("data")
    var data: SlotsData? = null
}

data class SlotsData(
    @SerializedName("slotSuggestions")
    var slotSuggestions: List<SlotSuggestion>? = null,
    @SerializedName("taskId")
    var taskId: TaskId? = null,
    @SerializedName("transMessage")
    var transMessage: String? = null,
    @SerializedName("transStatus")
    var transStatus: String? = null
)

data class TaskId(
    @SerializedName("clientId")
    val clientId: String? = null,
    @SerializedName("taskId")
    val taskId: String? = null
)