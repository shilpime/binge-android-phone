package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class WatchRequest(@SerializedName("subscriberId") val subscriberId: String,
                        @SerializedName("profileId") val profileId: String,
                        @SerializedName("pagingState") val pagingState: String?,
                        @SerializedName("offSet") val offset: Int,
                        @SerializedName("forceRefresh") val isForceRefresh: Boolean)