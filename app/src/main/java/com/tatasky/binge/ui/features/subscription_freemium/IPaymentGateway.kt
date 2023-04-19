package com.tatasky.binge.ui.features.subscription_freemium

import `in`.juspay.services.HyperServices
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.tatasky.binge.databinding.LayoutTsWalletBalanceBinding
import org.json.JSONObject

/**
 * All the payment related methods which needs to be
 * implemented for Payment journey
 **/
internal interface IPaymentGateway {
    var payByDthBalanceSelected: Boolean
    var hyperInstance: HyperServices?
    var payByBalanceBinding: LayoutTsWalletBalanceBinding?
    var juspayProcessPayload: JSONObject?
    /**
     * Note: Don't move Juspay SDK Initialise to fragment as this will lead to stuck loader or black UI issue
     */
    fun setupPaymentSDK(
        paymentClientId: String,
        paymentServiceId: String,
        paymentBetaAssets: Boolean,
        shouldCallPrefetch: Boolean = false
    )
    fun preFetchJusPay(
        paymentClientId: String,
        paymentServiceId: String,
        paymentBetaAssets: Boolean
    )
    fun fetchPayloadAndInitiateJuspay(showLoader: Boolean = true) {}
    fun initiateJusPay(
        fragmentActivity: FragmentActivity,
        juspayInitiatePayload: JSONObject,
        dthStatus: String?,
        shouldShowInfoOnUI: Boolean = false,
        alsoCallProcess: Boolean = false
    )
    fun processJuspay(juspayProcessPayload: JSONObject?) {} //Properties/Methods which are implemented in Base class or in all child class don't need empty impl here
    fun bindPayByBalanceView(parent: ViewGroup?): View? = null
    fun handleJusPayProcessResult(status: String)
    fun handleJusPayError(status: String)
    /**
     * Terminate HyperService when not needed
     */
    fun terminateJuspayService(activity: FragmentActivity)
}