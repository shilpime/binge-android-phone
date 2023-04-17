package com.tatasky.binge.data.database.model

import android.os.Parcelable
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.data.networking.models.response.Tenure
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubscriptionInfoModel(
    var packId: String,
    var selectedTenureId: String,
    var selectedTenureAmount: String,
    var proratedAmount: String,
    val selectedPack: PartnerPacks?,
    val selectedTenure: Tenure?,
    var cartId: String? = null
) : Parcelable