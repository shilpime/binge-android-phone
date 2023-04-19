package com.tatasky.binge.utils

import android.content.Intent
import com.google.gson.Gson
import com.tatasky.binge.data.networking.models.response.AddPackResponse
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.ui.features.recharge.JuspayInitiationResponse

import org.json.JSONObject
import java.lang.Exception

/**
 * Payment related utility should belong here
 */
object PaymentUtility {
    fun String?.getPgPaymentStatus() =
        when (this?.lowercase()) {
            TransactionStatus.CHARGED.transactionStatus.lowercase() -> SUCCESS
            TransactionStatus.BACKPRESSED.transactionStatus.lowercase() -> NOT_ATTEMPTED
            TransactionStatus.USER_ABORTED.transactionStatus.lowercase() -> NOT_ATTEMPTED
            TransactionStatus.PENDING_VBV.transactionStatus.lowercase() -> PENDING
            else -> FAILURE
        }

    fun PartnerPacks?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers() =
        if (this?.isInactive == true)
            this.tenure?.find { tenure -> tenure.lastActiveTenureForInactiveUser == true }
        else
            this?.tenure?.find { tenure -> tenure.currentTenure == true }

    fun isFirstPaidPack(
        currentPack: PartnerPacks?,
        previousPack: PartnerPacks?,
        firstPaidPackSubscriptionDate: String?
    ) = (currentPack == null && firstPaidPackSubscriptionDate == null)
            || (currentPack == null && previousPack == null && firstPaidPackSubscriptionDate == null)

    enum class SubscriptionModificationType(val modificationType: String){
        RENEWAL("Renewal"),
        UPGRADE("Upgrade"),
        DOWNGRADE("Downgrade")
    }

    fun getModificationType(
        modificationType: String?
    ) =
        if (modificationType != null)
            when {
                modificationType.equals(
                    SubscriptionModificationType.RENEWAL.modificationType,
                    true
                ) -> SubscriptionModificationType.RENEWAL.modificationType
                modificationType.equals(
                    SubscriptionModificationType.UPGRADE.modificationType,
                    true
                ) -> SubscriptionModificationType.UPGRADE.modificationType
                modificationType.equals(
                    SubscriptionModificationType.DOWNGRADE.modificationType,
                    true
                ) -> SubscriptionModificationType.DOWNGRADE.modificationType
                else -> modificationType
            }
        else
            null

    fun createAndGetInitiatePayload(
        juspayInitiationResponse: JuspayInitiationResponse,
        createManually: Boolean = false
    ): JSONObject {
        if (createManually) {
            //Create the Json Object manually when want to debug or change the value at FE side
            val juspayInitiatePayload = JSONObject()
            try {
                juspayInitiatePayload.put(
                    PaymentGatewayConstants.KEY_REQUEST_ID,
                    juspayInitiationResponse.data.requestId
                )
                juspayInitiatePayload.put(
                    PaymentGatewayConstants.KEY_SERVICE,
                    juspayInitiationResponse.data.service
                )

                val innerPayload = JSONObject()
                innerPayload.put(
                    PaymentGatewayConstants.KEY_ACTION,
                    juspayInitiationResponse.data.payload?.action
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_MERCHANT_ID,
                    juspayInitiationResponse.data.payload?.merchantId
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_CLIENT_ID,
                    juspayInitiationResponse.data.payload?.clientId
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_CUSTOMER_ID,
                    juspayInitiationResponse.data.payload?.customerId
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_SIGNATURE_PAYLOAD,
                    juspayInitiationResponse.data.payload?.signaturePayload
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_SIGNATURE,
                    juspayInitiationResponse.data.payload?.signature
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_MERCHANT_KEY_ID,
                    juspayInitiationResponse.data.payload?.merchantKeyId
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_ENVIRONMENT,
                    juspayInitiationResponse.data.payload?.environment
                )

                juspayInitiatePayload.put(PaymentGatewayConstants.KEY_PAYLOAD, innerPayload)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return juspayInitiatePayload
        } else
            return JSONObject(Gson().toJson(juspayInitiationResponse.data))
    }

    fun createAndGetProcessPayload(
        juspayProcessPayload: AddPackResponse.PaymentPayload,
        createManually: Boolean = false
    ): JSONObject {
        if (createManually) {
            //Create the Json Object manually when want to debug or change the value at FE side
            val processPayload = JSONObject()
            try {
                processPayload.put(
                    PaymentGatewayConstants.KEY_REQUEST_ID,
                    juspayProcessPayload.requestId
                )
                processPayload.put(
                    PaymentGatewayConstants.KEY_SERVICE,
                    juspayProcessPayload.service
                )
                val innerPayload = JSONObject()
                innerPayload.put(
                    PaymentGatewayConstants.KEY_ACTION,
                    juspayProcessPayload.payload?.action
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_MERCHANT_ID,
                    juspayProcessPayload.payload?.merchantId
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_CLIENT_ID,
                    juspayProcessPayload.payload?.clientId
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_ORDER_ID,
                    juspayProcessPayload.payload?.orderId
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_AMOUNT,
                    juspayProcessPayload.payload?.amount
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_CUSTOMER_ID,
                    juspayProcessPayload.payload?.customerId
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_CUSTOMER_EMAIL,
                    juspayProcessPayload.payload?.customerEmail
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_CUSTOMER_MOBILE,
                    juspayProcessPayload.payload?.customerMobile
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_ORDER_DETAILS,
                    juspayProcessPayload.payload?.orderDetails
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_SIGNATURE,
                    juspayProcessPayload.payload?.signature
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_MERCHANT_KEY_ID,
                    juspayProcessPayload.payload?.merchantKeyId
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_LANGUAGE,
                    juspayProcessPayload.payload?.language
                )
                innerPayload.put(
                    PaymentGatewayConstants.KEY_ENVIRONMENT,
                    juspayProcessPayload.payload?.environment
                )
                processPayload.put(PaymentGatewayConstants.KEY_PAYLOAD, innerPayload)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return processPayload
        } else
            return JSONObject(Gson().toJson(juspayProcessPayload))
    }
}
