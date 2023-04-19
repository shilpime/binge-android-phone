package com.tatasky.binge.data.networking.models.response

/**
 * Created by Srikant Karnani on 16/1/20.
 */
data class ForgetPasswordResponse(val data: Any?, val error: String, val timestamp: Long) :
    BaseResponse()