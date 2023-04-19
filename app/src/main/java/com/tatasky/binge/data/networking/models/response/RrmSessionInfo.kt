package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RrmSessionInfo {

    @SerializedName("sessionId")
    @Expose
    var sessionId: String? = null

    @SerializedName("sessionToken")
    @Expose
    var sessionToken: String? = null

    @SerializedName("ticket")
    @Expose
    var ticket: String? = null

    @SerializedName("nonce")
    @Expose
    var nonce: String? = null

    @SerializedName("expiryTime")
    @Expose
    var expiryTime: Long = 0

    @SerializedName("heartbeatInterval")
    @Expose
    var heartbeatInterval: Int = 0

    @SerializedName("maxMissedHeartbeats")
    @Expose
    var maxMissedHeartbeats: Int = 0

}
