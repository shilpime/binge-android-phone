package com.tatasky.binge.data.networking.models.response

data class TrialUpgradeResponse(val data : TrialUpgradeData?) : BaseResponse()

data class TrialUpgradeData(val basePackProviders: List<Providers>?,
                            val expiryDate: String?,
                            val upgradePackId: String?,
                            val planApplicableMessage: String?,
                            val upgradeTransitionMessage: String?,
                            val packName : String?,
                            val packPrice : String?,
                            val upgradedProviders: List<Providers>?)