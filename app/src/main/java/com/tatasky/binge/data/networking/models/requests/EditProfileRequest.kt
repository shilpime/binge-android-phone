package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class EditProfileRequest(@SerializedName("email") val email: String,
                         @SerializedName("firstName") val firstName: String,
                         @SerializedName("lastName")val lastName: String,
                         @SerializedName("rmn") val rmn: String)