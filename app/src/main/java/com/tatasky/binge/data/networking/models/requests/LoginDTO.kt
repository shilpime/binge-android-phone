package com.tatasky.binge.data.networking.models.requests

data class LoginDTO(
    var rmn: String = "",
    var sid: String = "",
    var pwd: String? = null,
    var email: String? = null,
    var name: String? = null,
    var otp: String? = null,
    var dsn: String?=null,
    var maskedNumber : String?=null,
    @Transient
    var actualRMN : String = ""
)