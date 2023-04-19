package com.tatasky.binge.ui.features.subscription.model

import com.tatasky.binge.data.networking.models.response.OfferEligiblePacks
import com.tatasky.binge.data.networking.models.response.PartnerPacks

data class PackOfferModel(var type: Int, val list: List<PartnerPacks>?, val additionalApps: OfferEligiblePacks?)