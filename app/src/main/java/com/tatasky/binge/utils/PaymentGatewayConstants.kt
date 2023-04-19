package com.tatasky.binge.utils

/** SDK Events */
enum class PaymentGatewayEvent(val event: String){
    SHOW_LOADER("show_loader"),
    HIDE_LOADER("hide_loader"),
    INITIATE_RESULT("initiate_result"),
    PROCESS_RESULT("process_result")
}

enum class PaymentGatewayInitResult(val result: String){
    SUCCESS("success")
}

enum class TransactionStatus(val transactionStatus: String){
    CHARGED("charged") /*Successful transaction*/,
    NEW("new") /* Newly created order */,
    PENDING_VBV("pending_vbv") /*Authentication is in progress*/,
    AUTHENTICATION_FAILED("authentication_failed") /*User did not complete authentication*/,
    AUTHORIZATION_FAILED("authorization_failed") /*User completed authentication, but the bank refused the transaction*/,
    JUSPAY_DECLINED("juspay_declined") /*User input is not accepted by the underlying PG*/,
    AUTHORIZING("authorizing") /*Transaction status is pending from bank*/,
    BACKPRESSED("backpressed"),
    USER_ABORTED("user_aborted")
}

enum class TransactionErrorCode(val transactionErrorCode: String) {
    /* JusPay Error Codes. Ref.: https://developer.juspay.in/v2.0/docs/error-codes-1 */
    JP_000("JP_000"),
    JP_001("JP_001"),
    JP_002("JP_002"),
    JP_003("JP_003"),
    JP_004("JP_004"),
    JP_005("JP_005"),
    JP_006("JP_006"),
    JP_007("JP_007"),
    JP_008("JP_008"),
    JP_009("JP_009"),
    JP_010("JP_010"),
    JP_011("JP_011"),
    JP_012("JP_012"),
    JP_014("JP_014"),
    JP_015("JP_015"),
    JP_016("JP_016"),
    JP_017("JP_017"),
    JP_018("JP_018")
}

class PaymentGatewayConstants {
    companion object {

        const val KEY_LANGUAGE = "language"
        const val KEY_ORDER_DETAILS = "orderDetails"
        const val KEY_CUSTOMER_MOBILE = "customerMobile"
        const val KEY_CUSTOMER_EMAIL = "customerEmail"
        const val KEY_AMOUNT = "amount"
        const val KEY_ORDER_ID = "orderId"
        const val KEY_ENVIRONMENT = "environment"
        const val KEY_MERCHANT_KEY_ID = "merchantKeyId"
        const val KEY_SIGNATURE = "signature"
        const val KEY_SIGNATURE_PAYLOAD = "signaturePayload"
        const val KEY_CUSTOMER_ID = "customerId"
        const val KEY_MERCHANT_ID = "merchantId"
        const val KEY_ACTION = "action"
        const val KEY_REQUEST_ID = "requestId"
        const val KEY_ERROR_MESSAGE = "errorMessage"
        const val KEY_ERROR_CODE = "errorCode"
        const val KEY_PAYMENT_INSTRUMENT_GROUP = "paymentInstrumentGroup"
        const val KEY_PAYMENT_INSTRUMENT = "paymentInstrument"
        const val KEY_ERROR = "error"
        const val KEY_EVENT = "event"
        const val KEY_BETA_ASSETS = "betaAssets"
        const val KEY_SERVICE = "service"
        const val KEY_CLIENT_ID = "clientId"
        const val KEY_PAYLOAD = "payload"
        const val KEY_STATUS = "status"
    }
}