package com.tatasky.binge.utils

import com.tatasky.binge.data.networking.models.response.PartnerPacks
import java.util.*

object ContentUtil {

    fun isLiveContent(contentType: String?, isLiveContent: Boolean?) =
        TYPE_LIVE.equals(contentType, true) || isLiveContent == true

    fun isAppleCodeRedeemed(provider: String?, appleRedemptionStatus: String?) =
        PROVIDER_APPLE.equals(
            provider,
            true
        ) && CONSUMED.equals(
            appleRedemptionStatus,
            true
        )

    fun isContentAuth(
        contentContractName: String,
        currentSubscribedPack: PartnerPacks?,
        isLoggedIn: Boolean,
        provider: String?,
        partnerSubscriptionType: String?,
        appleRedemptionStatus: String?
    ): Boolean {
        return !(!RENTAL.equals(contentContractName, true)
                && isShowCrownOnContent(
            isPartnerSubscribedForSelectedContent(currentSubscribedPack, provider ?: ""),
            !isLoggedIn,
            provider,
            partnerSubscriptionType,
            appleRedemptionStatus
        ))
    }

    private fun isPackAvailed(subscribedPack: PartnerPacks?) = subscribedPack != null

    private fun getNonSubscribedPartnerList(subscribedPack: PartnerPacks?): HashSet<String>? {
        subscribedPack?.nonSubscribedPartnerList?.let { partnerList ->
            val nonSubscribedPartnerList = HashSet<String>()
            for (partner in partnerList) {
                nonSubscribedPartnerList.add(
                    (partner.partnerName ?: "").lowercase(Locale.getDefault())
                )
            }
            return nonSubscribedPartnerList
        }
        return null
    }

    private fun isPartnerSubscribedForSelectedContent(
        subscribedPack: PartnerPacks?,
        provider: String
    ) =
        isPackAvailed(subscribedPack) && getNonSubscribedPartnerList(subscribedPack)?.contains(
            provider.lowercase(Locale.getDefault())
        ) != true

    fun isFreeContent(partnerSubscriptionType: String?) =
        !(PREMIUM.equals(
            partnerSubscriptionType,
            true
        ))
}
