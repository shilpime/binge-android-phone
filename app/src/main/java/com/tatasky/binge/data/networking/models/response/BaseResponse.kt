package com.tatasky.binge.data.networking.models.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.utils.CODE_SUCCESS
import kotlinx.android.parcel.Parcelize

@Parcelize
open class BaseResponse : Parcelable{


    @SerializedName("code")
    var code: Int = 0
    @SerializedName("title")
    var title: String? = null
    @SerializedName("message")
    var message: String? = null

    @SerializedName("status")
    var status: Int = CODE_SUCCESS

    object CODE{
        const val INVALID_409 = 409
        const val ERROR_8 = 8
        const val OK_200 = 200
        const val OK_0 = 0
        const val INVALID_OTP = 1
    }
}