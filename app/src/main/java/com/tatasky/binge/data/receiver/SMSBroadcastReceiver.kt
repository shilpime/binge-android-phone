package com.tatasky.binge.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.tatasky.binge.interfaces.OTPReceiveListener
import com.tatasky.binge.utils.d
import java.util.regex.Pattern

class SMSBroadcastReceiver: BroadcastReceiver() {

    var isRegistered = false
    private var otpReceiver: OTPReceiveListener? = null

    fun registerSMSReceiver(context: Context?, receiver: SMSBroadcastReceiver) {
        if (isRegistered) return
        isRegistered = true
        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
        context?.registerReceiver(receiver, intentFilter)
        d("OTP_Autofill", "registerSMSReceiver")
    }

    fun unregisterSMSReceiver(context: Context?, receiver: SMSBroadcastReceiver) {
        if (!isRegistered) return
        isRegistered = false
        otpReceiver = null
        context?.unregisterReceiver(receiver)
        d("OTP_Autofill", "unregisterSMSReceiver")
    }

    fun initOTPListener(receiver: OTPReceiveListener?) {
        this.otpReceiver = receiver
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
            d("OTP_Autofill", "inside onReceive status.statusCode :${status?.statusCode}")
            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    d("OTP_Autofill", "inside onReceive SMS success")
                    // Get SMS message contents
                    val otpMessage: String? = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as? String
                    d("OTP_Autofill", otpMessage)
                    // Extract one-time code from the message and complete verification
                    if (otpReceiver != null) {
                        // Replace the old value text as per the message format to extract OTP only
                        val otp = extractOtpFromMsg(otpMessage)
                        if (otp != null)
                            otpReceiver!!.onOTPReceived(otp)
                    }
                }

                CommonStatusCodes.TIMEOUT ->
                    // Waiting for SMS timed out (5 minutes)
                    // Handle the error ...
                    if (otpReceiver != null)
                        otpReceiver?.onOTPTimeOut()
                else -> Unit
            }
        }
    }

    //Extract 6 digit OTP
    private fun extractOtpFromMsg(otpMessage: String?): String? {
        otpMessage?.let {
            val pattern = Pattern.compile("(\\d{6})")
            val matcher = pattern.matcher(otpMessage)
            if (matcher.find()) {
                return matcher.group(0)
            }
            return null
        }
        return null
    }
}