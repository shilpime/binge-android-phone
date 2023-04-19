package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class CommonSubscriptionDetailsModel {
	@SerializedName("subscriptionType")
	val subscriptionType: String? = null

	@SerializedName("migrated")
	var migrated: Boolean = false

	@SerializedName("bingeAccountStatus")
	var bingeAccountStatus: String? = null

	@SerializedName("planType")
	val planType: String? = null

	@SerializedName("complementaryPlan")
	val complementaryPlanLinkedMessage : String? = null

	val isPaid
		get() = planType.equals("Paid", true)
}