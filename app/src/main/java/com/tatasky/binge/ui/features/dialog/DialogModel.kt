package com.tatasky.binge.ui.features.dialog

import androidx.annotation.Keep

/**
 * Created by Srikant Karnani on 20/12/19.
 */
@Keep
data class DialogModel(
    var cancelable: Boolean = true,
    val imageId: Int?=null,
    val title: String?,
    val primaryButtonText: String?,
    val secondaryButtonText: String?,
    val text: String?=null,
    val statusCode:Int?=null,
    val imageIdBig : Int? = null,
    val subText: String?=null
    )