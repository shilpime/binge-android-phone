package com.tatasky.binge.ui.features.device_management

import com.tatasky.binge.utils.Properties
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import org.json.JSONException
import org.json.JSONObject

class DeviceListManagementAnalytics(
    private val mixpanelHelper: MixpanelHelper,
    private val moEngageHelper: MoEngageHelper
) {

    /**
     * @param sid added while unification
     */
    fun trackDeviceListInitiate(sid: String) {
        trackMixPanelDeviceListInitiate(sid)
        trackMoEngageDeviceListInitiate()
    }

    /**
     * @param sid added while unification
     */
    private fun trackMixPanelDeviceListInitiate(sid: String) {
        mixpanelHelper.trackEvent(EVENT_LIST_DEVICES, mixpanelHelper.mMixpanelAPI)

        val jsonObjectUnified = JSONObject().apply {
            put(PARA_SID, sid)
        }
        mixpanelHelper.trackEvent(EVENT_LIST_DEVICES, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMoEngageDeviceListInitiate() {
        moEngageHelper.trackEvent(EVENT_LIST_DEVICES)
    }

    fun trackRemoveDevices(source: String, deviceAlias: String) {
        trackMixPanelRemoveDevices()
        trackMoEngageRemoveDevices()
        trackDeviceLimitRemove(source,deviceAlias)
    }

    private fun trackMixPanelRemoveDevices() {
        mixpanelHelper.trackEvent(EVENT_REMOVE_DEVICES, mixpanelHelper.mMixpanelAPI)
        mixpanelHelper.trackEvent(EVENT_REMOVE_DEVICES, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMoEngageRemoveDevices() {
        moEngageHelper.trackEvent(EVENT_REMOVE_DEVICES)
    }


    fun trackDeviceLimitPopupShown(source: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SOURCE, source.uppercase())
            moEngageHelper.trackEvent(EVENT_DEVICE_LIMIT_POPUP, payloadBuilder)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SOURCE, source)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_LIMIT_POPUP, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
        }

    }

    fun trackDeviceLimitPopupReviewClick(source: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SOURCE, source.uppercase())
            moEngageHelper.trackEvent(EVENT_DEVICE_LIMIT_REVIEW, payloadBuilder)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SOURCE, source)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_LIMIT_REVIEW, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
        }
    }

    fun trackDeviceLimitPopupSkip(source: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SOURCE, source.uppercase())
            moEngageHelper.trackEvent(EVENT_DEVICE_LIMIT_SKIP, payloadBuilder)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SOURCE, source)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_LIMIT_SKIP, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
        }
    }

    fun trackDeviceLimitView(source: String, deviceAliases: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SOURCE, source)
            payloadBuilder.addAttribute(PARA_DEVICE_LIST, deviceAliases)
            moEngageHelper.trackEvent(EVENT_DEVICE_LISTING_VIEW, payloadBuilder)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            val jsonObject = JSONObject().apply {
                put(PARA_SOURCE, source)
                put(PARA_DEVICE_LIST, deviceAliases)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_LISTING_VIEW, jsonObject, mixpanelHelper.mMixpanelAPI)

            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SOURCE, source)
                put(PARA_DEVICE_LIST, deviceAliases)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_LISTING_VIEW, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
        }
    }

    fun trackDeviceLimitRemove(source: String, deviceAlias: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SOURCE, source)
            payloadBuilder.addAttribute(PARA_DEVICE, deviceAlias)
            moEngageHelper.trackEvent(EVENT_DEVICE_LISTING_REMOVE, payloadBuilder)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            val jsonObject = JSONObject().apply {
                put(PARA_SOURCE, source)
                put(PARA_DEVICE, deviceAlias)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_LISTING_REMOVE, jsonObject, mixpanelHelper.mMixpanelAPI)

            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SOURCE, source)
                put(PARA_DEVICE, deviceAlias)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_LISTING_REMOVE, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
        }
    }

    fun trackDeviceLimitExit(source: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SOURCE, source.uppercase())
            moEngageHelper.trackEvent(EVENT_DEVICE_LISTING_EXIT, payloadBuilder)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SOURCE, source)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_LISTING_EXIT, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
        }
    }

    fun trackDeviceLimitConfirmationPopupShown(source: String, deviceAlias: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SOURCE, source.uppercase())
            payloadBuilder.addAttribute(PARA_DEVICE, deviceAlias)
            moEngageHelper.trackEvent(EVENT_DEVICE_REMOVE_CONFIRMATION_POPUP, payloadBuilder)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SOURCE, source)
                put(PARA_DEVICE, deviceAlias)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_REMOVE_CONFIRMATION_POPUP, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
        }
    }

    public fun trackMixPanelDeviceRemoveConfirm(source: String, deviceAlias: String) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SOURCE, source)
                put(PARA_DEVICE, deviceAlias)
            }
            mixpanelHelper.trackEvent(
                EVENT_DEVICE_REMOVE_CONFIRM,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {

        }
    }

    /***
     * @param sid added while unification
     */
    fun trackDeviceLimitRemoveSkip(source: String, sid: String, deviceAlias: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SOURCE, source.uppercase())
            payloadBuilder.addAttribute(PARA_DEVICE, deviceAlias)
            moEngageHelper.trackEvent(EVENT_DEVICE_REMOVE_SKIP, payloadBuilder)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            val jsonObject = JSONObject().apply {
                put(PARA_SOURCE, source)
                put(PARA_DEVICE, deviceAlias)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_REMOVE_SKIP, jsonObject, mixpanelHelper.mMixpanelAPI)

            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SOURCE, source)
                put(PARA_SID, sid)
                put(PARA_DEVICE, deviceAlias)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_REMOVE_SKIP, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
        }
    }

    /**
     * @param source added while unification
     */
    fun trackDeviceLimitRemoveError(source: String, error: String, deviceAlias: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_ERROR_MESSAGE, error)
            payloadBuilder.addAttribute(PARA_DEVICE, deviceAlias)
            moEngageHelper.trackEvent(EVENT_DEVICE_REMOVE_ERROR, payloadBuilder)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            val jsonObject = JSONObject().apply {
                put(PARA_ERROR_MESSAGE, error)
                put(PARA_DEVICE, deviceAlias)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_REMOVE_ERROR, jsonObject, mixpanelHelper.mMixpanelAPI)

            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SOURCE, source)
                put(PARA_DEVICE, deviceAlias)
            }
            mixpanelHelper.trackEvent(EVENT_DEVICE_REMOVE_ERROR, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
        }
    }
}