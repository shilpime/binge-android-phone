package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class WorkOrderRequest(@SerializedName("slotEndTime")
                            val slotEndTime: String? = null,
                            @SerializedName("slotStartTime")
                            val slotStartTime: String? = null,
                            @SerializedName("subscriberId")
                            val subscriberId: String? = null,
                            @SerializedName("dyi")
                            val isDIY: Boolean = false
)