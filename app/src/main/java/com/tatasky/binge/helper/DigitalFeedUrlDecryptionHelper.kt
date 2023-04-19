package com.tatasky.binge.helper

import android.text.TextUtils
import android.util.Base64
import com.tatasky.binge.utils.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class DigitalFeedUrlDecryptionHelper {

    companion object{

        private const val TAG = "DigitalFeedUrlDecryptionHelper"
        private const val DIGITAL_DELIVERY_SECRET_KEY_V1 = "v1"
        private const val DIGITAL_DELIVERY_SECRET_KEY_V2 = "v2"
        private const val AES_ENCRYPTION = "AES"
        private const val AESEncryptionSecretKeyV1 = "YWVzRW5jcnlwdGlvbktleQ=="
        private const val AESEncryptionSecretKeyV2 = "YWVzRW5jcnlwdGlvbktleQ=="
        private const val DIGITAL_DELIVERY_FAILED = 8000
        val ENCRYPTED_URL_KEY_SEPARATOR = "#"


        @JvmStatic
        fun getSecretKey(
            encryptedUrl: String,
            aesEncryptionSecretKeyV1: String?,
            aesEncryptionSecretKeyV2: String?,
        ): String {
            return try {
                val lastIndex: Int = encryptedUrl.lastIndexOf(ENCRYPTED_URL_KEY_SEPARATOR)
                val keyName = encryptedUrl.substring(lastIndex + 1)
                var secretKey = ""
                if (keyName.equals(
                        DIGITAL_DELIVERY_SECRET_KEY_V1,
                        ignoreCase = true
                    )
                ) {
                    secretKey = aesEncryptionSecretKeyV1 ?: AESEncryptionSecretKeyV1
                } else if (keyName.equals(
                        DIGITAL_DELIVERY_SECRET_KEY_V2,
                        ignoreCase = true
                    )
                ) {
                    secretKey = aesEncryptionSecretKeyV2 ?: AESEncryptionSecretKeyV2
                }
                secretKey
            } catch (e: Exception) {
                e.printStackTrace()
                e(TAG, e.message)
                ""
            }
        }

        private fun prepareSecretKey(myKey: String): SecretKeySpec? {
            return try {
                val key = Base64.decode(myKey, Base64.NO_WRAP)
                SecretKeySpec(key, 0, key.size, AES_ENCRYPTION)
            } catch (e: Exception) {
                e.printStackTrace()
                d(TAG, e.message)
                null
            }
        }

        @JvmStatic
        fun decryptDigitalDeliveryUrl(encryptedUrl: String, secretKey: String): String {
            return try {
                if (!TextUtils.isEmpty(secretKey)) {
                    val secretKeySpec = prepareSecretKey(secretKey)
                    if (secretKeySpec != null) {
                        val cipher = Cipher.getInstance(AES_ENCRYPTION)
                        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
                        String(cipher.doFinal(Base64.decode(encryptedUrl, Base64.NO_WRAP)))
                    } else {
                        ""
                    }
                } else ""
            } catch (e: Exception) {
                e.printStackTrace()
                d(TAG, e.message)
                ""
            }
        }
    }
}