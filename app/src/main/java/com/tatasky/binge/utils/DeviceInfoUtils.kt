package com.tatasky.binge.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import java.net.NetworkInterface
import java.util.*

object DeviceInfoUtils {

    val macAddress: String
        get() {
            var mac = getMACAddress("wlan0")
            if (TextUtils.isEmpty(mac)) {
                mac = getMACAddress("eth0")
            }
            if (TextUtils.isEmpty(mac)) {
                mac = "DU:MM:YA:DD:RE:SS"
            }
            return mac
        }

    val locale: String
        get() = Locale.getDefault().isO3Country

    val getRandomUUID: String
        get() =UUID.randomUUID().toString()

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context) : String{
        val id = Settings.Secure.getString(context.contentResolver,
            Settings.Secure.ANDROID_ID) ?: ""
        return id
    }

    /**
     * Returns MAC address of the given interface name.
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return  mac address or empty string
     */
    private fun getMACAddress(interfaceName: String?): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (interfaceName != null) {
                    if (!intf.name.equals(interfaceName, ignoreCase = true)) continue
                }
                val mac = intf.hardwareAddress ?: return ""
                val buf = StringBuilder()
                for (aMac in mac) buf.append(String.format("%02X:", aMac))
                if (buf.isNotEmpty()) buf.deleteCharAt(buf.length - 1)
                return buf.toString()
            }
        } catch (ignored: Exception) {
        }
        // for now eat exceptions
        return ""
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4   true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    fun getIPAddress(useIPv4: Boolean): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':') < 0

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.toUpperCase(Locale.getDefault()) else sAddr.substring(
                                    0,
                                    delim
                                ).toUpperCase(Locale.getDefault())
                            }
                        }
                    }
                }
            }
        } catch (ignored: Exception) {
        }
        // for now eat exceptions
        return ""
    }

    fun getConnectionType(context: Context): String {
        var result =
            NetworkTypeEnum.NO_NETWORK.desc // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = NetworkTypeEnum.NETWORK_WIFI.desc
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = getDataType(context)
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = NetworkTypeEnum.NETWORK_WIFI.desc
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = getDataType(context)
                    }
                }
            }
        }
        return result
    }

    fun getNetworkType(activity: Context): String {
        var networkStatus = ""

        val connMgr = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // check for wifi
        val wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        // check for mobile data
        val mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        if (wifi!!.isAvailable) {
            networkStatus = "Wifi"
        } else if (mobile!!.isAvailable) {
            networkStatus = getDataType(activity)
        } else {
            networkStatus = "noNetwork"
        }
        return networkStatus
    }

    fun getDataType(activity: Context): String {
        var type = NetworkTypeEnum.NETWORK_MOBILE_DATA.desc
        val tm = activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        when (tm.networkType) {
            TelephonyManager.NETWORK_TYPE_HSDPA -> type =
                NetworkTypeEnum.NETWORK_MOBILE_DATA_3G.desc
            TelephonyManager.NETWORK_TYPE_HSPAP -> type =
                NetworkTypeEnum.NETWORK_MOBILE_DATA_4G.desc
            TelephonyManager.NETWORK_TYPE_GPRS -> type =
                NetworkTypeEnum.NETWORK_MOBILE_DATA_GPRS.desc
            TelephonyManager.NETWORK_TYPE_EDGE -> type = NetworkTypeEnum.NETWORK_MOBILE_DATA_2G.desc
        }// for 3g HSDPA networktype will be return as
        // per testing(real) in device with 3g enable
        // data
        // and speed will also matters to decide 3g network type
        // No specification for the 4g but from wiki
        // i found(HSPAP used in 4g)

        return type
    }

    fun hasNetwork(context: Context): Boolean {
        var result = false // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = true
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = true
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }

    fun getNetworkCarrier(context: Context): String {
        var operatorName = ""
        try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            operatorName = telephonyManager.networkOperatorName
        } catch (ex: Exception) {
        }

        return operatorName
    }

    fun getDeviceName(): String? {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true
        var phrase = ""
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c)
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase += c
        }
        return phrase
    }
}
