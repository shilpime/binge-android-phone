package com.tatasky.binge.shemaroo.helper

import com.tatasky.binge.BuildConfig
import java.security.NoSuchAlgorithmException


class ShemarooHelper {

    companion object {
        @JvmStatic
        fun decryptMd5(smartURL: String): String {
            val smarturlAccesskey = "ywVXaTzycwZ8agEs3ujx"
            val smarturlParameters = "service_id=${BuildConfig.SHEMAROOME_SERVICE_ID}&play_url=yes&protocol=hls&us="
            val encryptedSmartURL = "$smarturlAccesskey$smartURL?$smarturlParameters"
            val MD5 = "MD5"
            try {
                // Create MD5 Hash
                val digest = java.security.MessageDigest
                    .getInstance(MD5)
                digest.update(encryptedSmartURL.toByteArray())
                val messageDigest = digest.digest()

                // Create Hex String
                val hexString = StringBuilder()
                for (aMessageDigest in messageDigest) {
                    var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                    while (h.length < 2)
                        h = "0$h"
                    hexString.append(h)
                }
                return "$smartURL?$smarturlParameters$hexString"

            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }

            return ""
        }
    }
}