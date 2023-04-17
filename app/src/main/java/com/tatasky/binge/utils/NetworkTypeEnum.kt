package com.tatasky.binge.utils

/**
 * Created by Srikant Karnani on 11/11/19.
 */
enum class NetworkTypeEnum(val desc: String) {
    NETWORK_WIFI("Wifi"),
    NETWORK_MOBILE_DATA("Mobile_Data"),
    NETWORK_MOBILE_DATA_3G("Mobile_Data_3G"),
    NETWORK_MOBILE_DATA_4G("Mobile_Data_4G"),
    NETWORK_MOBILE_DATA_GPRS("Mobile_Data_GPRS"),
    NETWORK_MOBILE_DATA_2G("Mobile_Data_EDGE_2G"),
    NO_NETWORK("No Network")
}