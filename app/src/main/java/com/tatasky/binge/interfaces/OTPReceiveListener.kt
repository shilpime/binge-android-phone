package com.tatasky.binge.interfaces

interface OTPReceiveListener {
    fun onOTPReceived(otp: String)
    fun onOTPTimeOut()
}