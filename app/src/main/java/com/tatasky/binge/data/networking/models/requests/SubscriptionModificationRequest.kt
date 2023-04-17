package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

class SubscriptionModificationRequest {
    @SerializedName("addPackIdList")
    var newAddedPackList: List<AddPackId> = emptyList()

    @SerializedName("dropPackIdList")
    var droppedPackList: List<DropPackId> = emptyList()

    @SerializedName("baId")
    var baid: String? = null

    @SerializedName("sid")
    var sid: String? = null

    @SerializedName("packId")
    var packId: String? = null

    data class AddPackId(@SerializedName("addPackId") var addedPackId: String)
    data class DropPackId(@SerializedName("dropPackId") var droppedPackId: String)
}