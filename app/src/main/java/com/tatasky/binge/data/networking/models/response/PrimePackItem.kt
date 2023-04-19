package com.tatasky.binge.data.networking.models.response

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

class PrimePackItem() : Parcelable {
	fun getFormattedPrice(): Int {
		if (price.isNullOrEmpty()) return 0
		return try {
			val number = BigDecimal(price)
			val formattedBalance: String = number.stripTrailingZeros().toPlainString()
			formattedBalance.toInt()
		} catch (e: Exception) {
			0
		}
	}
	@SerializedName("packId")
	@Expose
	var packId: String? = null

	@SerializedName("title")
	@Expose
	var title: String? = null

	@SerializedName("primeButtonKey")
	@Expose
	var buttonTitle : String? = null

	@SerializedName("joinPrimeButtonMessage")
	@Expose
	var buttonText : String? = null

	@SerializedName("packName")
	@Expose
	var packName: String? = null

	@SerializedName("price")
	@Expose
	var price: String? = null

	@SerializedName("packType")
	@Expose
	var packType: String? = null

	@SerializedName("iconUrl")
	@Expose
	var iconUrl: String? = null

	@SerializedName("renewalCycle")
	@Expose
	var renewalCycle: String? = null

	@SerializedName("recommendedAmount")
	@Expose
	var recommendedAmount: String? = null

	@SerializedName("minimumRecharge")
	@Expose
	var minimumRecharge: String? = null

	constructor(parcel: Parcel) : this() {
		packId = parcel.readString()
		title = parcel.readString()
		buttonTitle = parcel.readString()
		packName = parcel.readString()
		price = parcel.readString()
		packType = parcel.readString()
		iconUrl = parcel.readString()
		renewalCycle = parcel.readString()
		recommendedAmount = parcel.readString()
		minimumRecharge = parcel.readString()
	}

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(packId)
		parcel.writeString(title)
		parcel.writeString(buttonTitle)
		parcel.writeString(packName)
		parcel.writeString(price)
		parcel.writeString(packType)
		parcel.writeString(iconUrl)
		parcel.writeString(renewalCycle)
		parcel.writeString(recommendedAmount)
		parcel.writeString(minimumRecharge)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<PrimePackItem> {
		override fun createFromParcel(parcel: Parcel): PrimePackItem {
			return PrimePackItem(parcel)
		}

		override fun newArray(size: Int): Array<PrimePackItem?> {
			return arrayOfNulls(size)
		}
	}
}