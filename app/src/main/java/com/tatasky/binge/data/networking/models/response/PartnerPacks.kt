package com.tatasky.binge.data.networking.models.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.utils.PRIME_ACTIVATED
import com.tatasky.binge.utils.PRIME_SUSPENDED
import com.tatasky.binge.utils.SubscriptionPackStatusEnum
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
class PartnerPacks : Parcelable {
    @SerializedName("regionalAppNudge")
    val regionalAppNudge: RegionalAppNudge? = null

    @SerializedName("fibreDetails")
    val fibreDetails: FiberDetails? = null

    @SerializedName("segment")
    var segment: String? = null

    @SerializedName("acquisitionSource")
    var acquisitionSource: String? = null

    class RegionalAppNudge {
        @SerializedName("enableRegionalAppNudge")
        val enableRegionalAppNudge: Boolean? = null

        @SerializedName("regionalAppVerbiage")
        val regionalAppVerbiage: String? = null

        @SerializedName("regionalAppCTA")
        val regionalAppCTA: String? = null
    }

    class FiberDetails{
        @SerializedName("button")
        val button: String? = null
        @SerializedName("header")
        val header: String? = null
        @SerializedName("logo")
        val logo: String? = null
        @SerializedName("message1")
        val message1: String? = null
        @SerializedName("message2")
        val Message2: String? = null
        @SerializedName("fiberPackValidity")
        val fiberPackValidity: String? = null
        @SerializedName("fiberMoreAppsVerbiage")
        val fiberMoreAppsVerbiage: String? = null
    }

    @SerializedName("nammaFlixVerbiage")
    var nammaFlixVerbiage: String? = null

    @SerializedName("launchOfferVerbiage")
    var launchOfferVerbiage: String? = null

    @SerializedName("firstPaidPackSubscriptionDate")
    var firstPaidPackSubscriptionDate: String? = null

    @SerializedName("burnType")
    var burnRateType: String? = null
    @SerializedName("flexiPlan")
    var flexiPlan: Boolean = false

    @SerializedName("transactionID")
    var transactionID: String? = null /*In Current subscription latest txn id will be there including FDO Txns*/

    @SerializedName("promoCode")
    var promoCode: String? = null
    
    @SerializedName("paymentMethod")
    var paymentMethod: String? = null /*Expected values: "NB_BILLDESK", "TEST"*/

    @SerializedName("firstPaidPack")
    var userIsOnFirstPaidPack: Boolean = false

    @SerializedName("packDuration")
    var packDurationInDaysWithDSuffix: String? = null /*Base pack duration Eg. 30D, 180D*/

    @SerializedName("downgradeRequested")
    var downgradeRequested: Boolean? = null

    @SerializedName("showFibreMsg")
    var showFibreMsg: Boolean = true

    @SerializedName("fiberVerbiage")
    var fiberVerbiage: String? = null

    @SerializedName("downgradeRequestedMessage")
    var downgradeRequestedMessage: String? = null

    @SerializedName("paymentMode")
    var paymentMode: String? = null /*Expected values: "OPEL_ONE_TIME"*/

    @SerializedName("fdoRequested")
    var fdoRequested: Boolean? = null

    @SerializedName("sunnxtFooterMessage")
    var sunnxtFooterMessage : String ? = null

    @SerializedName("comboPlanName")
    var comboPlanName: String? = null

    @SerializedName("DTH")
    var DTH: Boolean = false

    @SerializedName("combo")
    var freemiumCombo:Boolean = false

    @SerializedName ("netflixCombo")
    var netflixCombo: Boolean = false
//
//    @SerializedName("subscriptionType")
//    var subscriptionType: String? = null

    @SerializedName("dthSubscriberId")
    var dthSubscriberId: String? = null

    @SerializedName("migrated")
    var migrated: Boolean = false

    @SerializedName("migratedVerbiage")
    var migratedVerbiage: String? = null


    @SerializedName("preSelectedPackId")
    var preSelectedPackId: String? = null

    @SerializedName("productId")
    var productId: String? = null

    @SerializedName("specialOfferVerbiage")
    var specialOfferVerbiage : String? = null

    @SerializedName("productName")
    var productName: String? = null

    @SerializedName("packValidity")
    var packValidity: String? = null

    @SerializedName("subscriptionStatus")
    var subscriptionStatus : String? = null

    @SerializedName("expiryFooterMessage")
    var expiryFooterMessage: String? = null

    @SerializedName("amount")
    var amount: String? = ""

    @SerializedName("amountTimePeriod")
    var amountTimePeriod: String? = ""

    @SerializedName("amountWithoutTimePeriod")
    var amountWithoutTimePeriod: String? = ""

    @SerializedName("rupeesSymbol")
    var rupeesSymbol:String? = ""

    @SerializedName("amountValue")
    var amountValue: String? = ""

    @SerializedName("componentList")
    var componentList: List<ComponentList> = arrayListOf()

    @SerializedName("tenure")
    var tenure: List<Tenure>? = arrayListOf()

    @SerializedName("tenureFootageMessage")
    var tenureFootageMessage: String? = null

    @SerializedName("tenureMessage")
    var tenureMessage: String? = null

    /*Key to identify if user is/was on Freetrial if true*/
    @SerializedName("freeTrialStatus")
    var freeTrialStatus: Boolean? =null

    @SerializedName("currentTenure")
    var currentTenure: String? = null

    @SerializedName("productFooterMessage")
    var productFooterMessage: String? = null

    @SerializedName("packFooterMessage")
    var packFooterMessage: String? = null

    @SerializedName("sunnextFooterMessage")
    var sunnextFooterMessage: String? = null

    @SerializedName("packHeaderMessage")
    var packHeaderMessage: String? = null

    @SerializedName("appHeaderMessage")
    var appHeaderMessage: String? = null


    @SerializedName("packCycle")
    var packCycle: String?=null

    @SerializedName("largeScreenImage")
    var largeScreenImage: String? = null

    @SerializedName("comparePlanFooterMessage")
    var comparePlanFooterMessage: String? = null

    @SerializedName("highlightedPack")
    var highlightedPack: Boolean? = null

    val getSelectedComponentAppList: List<PartnerList>
        get() {
            return componentList.find { it.componentName.equals(productId, true) }?.partnerList
                ?: componentList.getOrNull(0)?.partnerList ?: emptyList()
        }

    val getSelectedComponent: PartnerPacks.ComponentList?
        get() {
            return componentList.find { it.componentName.equals(productId, true) } ?: componentList.getOrNull(0)
        }


    @SerializedName("planOption")
    var planCTADetails: PlanOptions? = null

    @SerializedName("deviceDetails" ,alternate = ["deviceDetailDTO"])
    var deviceDetails: DeviceDetails? = null

    @SerializedName("gameZopInfo")
    var gameZopInfo : GameZopInfo ? = null

    @SerializedName("regionalAppInfo")
    var regionalAppInfo : RegionalAppInfo ? = null

    @SerializedName("nonSubscribedPartnerList")
    var nonSubscribedPartnerList: List<PartnerList>? = null


    inner class GameZopInfo {
        @SerializedName("gameZopVerbiage")
        var gameZopVerbiage: String? = null

        @SerializedName("gameZopIcon")
        var gameZopIcon: String? = null
    }

    inner class RegionalAppInfo {
        @SerializedName("enableRegionalAppCTA")
        var enableRegionalAppCTA: Boolean? = false

        @SerializedName("enableRegionalAppCTAVerbiage")
        var enableRegionalAppCTAVerbiage: String? = null
    }

    inner class DeviceDetails{
        @SerializedName("url")
        var iconUrl: String? = null

        @SerializedName("platformName")
        var platformName: String? = null

        @SerializedName("deviceCount")
        var deviceCount: String? = null

        @SerializedName("mxPlatformName")
        var mxPlatformName: String? = null

        @SerializedName("enhancedPlatformName")
        var enhancedPlatformName: String? = null
    }

    inner class PlanOptions {
        @SerializedName("renewPlanVerbiage")
        var renewPlanVerbiage:String? = null

        @SerializedName("expiredVerbiage")
        var expiredVerbiage: Boolean ? =null

        @SerializedName("otherPlanOptionVerbiage")
        var otherPlanOptionVerbiage:String? = null

        @SerializedName("renewButtonOption")
        var renewPlanOption: Boolean = false

        @SerializedName("renewButtonMessage")
        var renewPlanText: String? = null

        @SerializedName("getPlanOption")
        var getPlanOption: Boolean = false

        @SerializedName("getPlanVerbiage")
        var getPlanVerbiage: String? = null

        @SerializedName("getPlanMessage")
        var getPlanText: String? = null

        @SerializedName("revokeButtonOption")
        var revokePlanOption: Boolean = false

        @SerializedName("revokeTextHighlighted")
        var revokeTextHighlighted: Boolean = false

        @SerializedName("revokeButtonMessage")
        var revokePlanText: String? = null

        @SerializedName("changePlanOption")
        var changePlanOption: Boolean = false

        @SerializedName("changePlanMessage")
        var changePlanText: String? = null

        @SerializedName("changeTenureOption")
        var changeTenureOption: Boolean = false

        @SerializedName("changeTenureMessage")
        var changeTenureText: String? = null

        @SerializedName( "cancellationOption")
        var cancellationOptions : CancellationPlanOptions ?= null
    }

    inner class CancellationPlanOptions {
        @SerializedName("cancelButtonOption")
        var cancelPlanOption: Boolean = false

        @SerializedName("cancelButtonMessage")
        var cancelPlanText: String? = null

        @SerializedName("bingeCancelHeaderMessage")
        var cancelHeaderText: String? = null

        @SerializedName("bingeCancelFooterMessage")
        var cancelFooterMessage: String? = null

        @SerializedName("bingeCancelExpiryVerbiage")
        var cancelExpiryVerbiage: String? = null

        @SerializedName("primeCancelHeaderMessage")
        var addOnCancelHeaderText: String? = null

        @SerializedName("primeCancelFooterMessage")
        var addOnCancelFooterMessage: String? = null

        @SerializedName("primeCancelExpiryVerbiage")
        var addOnCancelExpiryVerbiage: String? = null
    }

    inner class PartnerList {

        @SerializedName("partnerId")
        var partnerId: String? = null

        @SerializedName("partnerName")
        var partnerName: String? = null

        @SerializedName("iconUrl")
        var iconUrl: String? = null

        @SerializedName("included")
        var included: Boolean? = null

        @SerializedName("premiumPartner")
        var premiumPartner: Boolean? = null

        @SerializedName("starterPackHighlightApp")
        var starterPackHighlightApp : Boolean? = null

        @SerializedName("footerMessage")
        var footerMessage: String? = null

        @SerializedName("squareImageUrl")
        var squareImageUrl: String? = null
        @SerializedName("fibrePosition")
        var fibrePosition: String? = null



//        getPlanVerbiage
    }

    inner class ComponentList {

        @SerializedName("numberOfApps")
        var numberOfApps: String? = null

        @SerializedName("componentId")
        var componentId: String? = null

        @SerializedName("componentName")
        var componentName: String? = null

        @SerializedName("partnerList")
        private var _partnerList: List<PartnerList>? = null

        val partnerList: List<PartnerList>
            get() = _partnerList ?: emptyList()

        @SerializedName("appCount")
        var appCount: String? = null

        @SerializedName("appsVerbiage")
        var appsVerbiage: String? = null


    }


    fun getFormattedPrice(): Int {
        if (packPrice.isNullOrEmpty()) return 0
        return try {
            val number = BigDecimal(packPrice)
            val formattedBalance: String = number.stripTrailingZeros().toPlainString()
            formattedBalance.toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun getPackNameWithPrice(): String {
        return "${packName ?: ""} ${getFormattedPrice()}"
    }

    fun getPaidPackSelectionNudge(): PaidPackSelectionNudge? {
        return nudges?.paidPackSelectionNudge
    }

    // Subscribed Binge Product/Current subscribed product
    @SerializedName("bingeProduct")
    var subscribedBingeProduct: String? = null

    @SerializedName("mobileUpgradable")
    var mobileUpgradable : Boolean = false

    @SerializedName("ocsFlag")
    var ocsFlag : String = "N"

    @SerializedName("deviceStatus")
    var deviceStatus :String? = null

    @SerializedName("deviceCancellationFlag")
    var deviceCancellationFlag: Boolean = false

    @SerializedName("expiryMessage")
    var expiryMessage: String? = null

    @SerializedName("providers")
    private var _appList: List<Providers>? = null

    val appList : List<Providers>
        get() = _appList?: emptyList()

    @SerializedName("packId", alternate = ["adonPackId"])
    var packId: String? = null

    @SerializedName("irdetoPackId")
    var irdetoPackId: String? = null

    @SerializedName("title", alternate = ["packName"])
    var packName: String? = null

    @SerializedName("price", alternate = ["packPrice"])
    var packPrice: String? = null

    @SerializedName("renewalCycle")
    var renewalCycle: String? = null

    @SerializedName("iconUrl")
    var iconUrl: String? = null

    @SerializedName("offerEligibility")
    var listOfConditionalApps: List<OfferEligiblePacks>? = null

    @SerializedName("packOffers")
    var offersList: List<OffersInPack>? = null

    @SerializedName("packCreatedDate", alternate = ["packCreated"])
    var packCreatedDate: String = ""


    @SerializedName("packCreationDate")
    var packCreationDate: String? = null

    @SerializedName("expirationDate", alternate = ["packExpiry", "expiryDate"])
    var expirationDate: String = ""

    @SerializedName("rechargeDue")
    var rechargeDue: String? = null

    @SerializedName("expiryDateWithTime")
    var expiryDateWithTime: String? = null

    //
    @SerializedName("amountDue")
    var amountDue: String? = null

    @SerializedName("status")
    var status = SubscriptionPackStatusEnum.ACTIVE.status

    @SerializedName("expiredWithinSixtyDays")
    var expiredWithinSixtyDays: Boolean = false

    @SerializedName("message")
    var modificationMessage: String? = null

    //
    @SerializedName("downgrade")
    var isDowngrade: Boolean = false

    @SerializedName("packType")
    var packType: String? = null

    @SerializedName("alternatePackId")
    var alternatePaidPackId: String? = null

    @SerializedName("cancelled")
    var isCancelled: Boolean = false

    @SerializedName("fsEligibility")
    var eligibleFirestick: Boolean = false

    @SerializedName("largeScreenEligibility")
    var largeScreenEligibility: Boolean = false

    @SerializedName("fsTaken")
    var fsTaken: Boolean = false

    @SerializedName("fSRequestRaised")
    var isFSRequestRaised: Boolean = false

    val doNotConsiderThePack
        get() = true == subscriptionDetailInfo?.bingeAccountStatus?.equals(
            SubscriptionPackStatusEnum.WRITTEN_OFF.status,
            true
        )
    //    val isInactive
//        get() = !doNotConsiderThePack && true == subscriptionDetailInfo?.bingeAccountStatus?.equals(
//            SubscriptionPackStatusEnum.DEACTIVE.status,
//            true
//        )
    val isInactive
        get() = (SubscriptionPackStatusEnum.DEACTIVE.status.equals(subscriptionStatus, true)
                || SubscriptionPackStatusEnum.WRITTEN_OFF.status.equals(subscriptionStatus, true))

    val isPaid
        get() = packType.equals("Paid", true)

    @SerializedName("isCombo")
    var isCombo: Boolean = false

    @SerializedName("comboInfo")
    val comboPackChannelsInfo : ComboChannelsInfo?=null


    @SerializedName("netflixData")
    var netflixData : NetflixData?=null

    @SerializedName("partnerUniqueId")
    val partnerUniqueId : String? = null

    @SerializedName("primePackDetails")
    var primePackDetails: PrimePack? = null

    @SerializedName("recommendedAmount")
    var recommendedAmount: String? = null

    @SerializedName("subscriptionType")
    var subscriptionType: String? = null


    @SerializedName("rmn")
    var rmn: String? = ""

    @SerializedName("knowMore")
    var knowMoreDetails: KnowMore? = null

    @SerializedName("footerVerbiage")
    var footerVerbiage : String ?= null

    @SerializedName("verbiage")
    var verbiage:Verbiage? = null

    @SerializedName("upgradeFDOMessage", alternate = ["cancelationFdoMessage"])
    var upgradeFDOMessage: String? = null

    @SerializedName("upgradeFDOCheck", alternate = ["cancelationFdoRequested"])
    var upgradeFDOCheck: Boolean? = null

    @SerializedName("upgradeFDOHeader")
    var upgradeFDOHeader: String? = null

    inner class Verbiage {
        @SerializedName("headerMessage")
        var headerMessage : String? = null

        @SerializedName("appCount")
        var appsGridHeader : String?=null

        @SerializedName("subsTitle")
        var subsTitle: String? = null

        @SerializedName("footerMessage")
        var footerMessage: String? = null

        @SerializedName("button")
        var button: String? = null

        @SerializedName("expiryMessage")
        var expiryMessage: String? = null

        @SerializedName("cancelSubs")
        var cancelSubs: CancelSubs? = null

        @SerializedName("revokeSubs")
        var revokeSubs: TitleMessageModel? = null

        @SerializedName("footerVerbiage")
        var footerVerbiage: TitleMessageModel? = null

        @SerializedName("contentSubs")
        var contentSubs: TitleMessageModel? = null

        inner class CancelSubs {
            @SerializedName("selectionDialogBox")
            var selectionDialogBox: SelectionDialogBox? = null

            @SerializedName("confirmationDialogBox")
            var confirmationDialogBox: CancelConfirmationDialogBox? = null
        }

        inner class SelectionDialogBox {
            @SerializedName("title")
            var title: String? = null

            @SerializedName("message")
            var message: String? = null

            @SerializedName("chkbox1")
            var chkbox1: String? = null

            @SerializedName("chkbox2")
            var chkbox2: String? = null
        }

        inner class CancelConfirmationDialogBox {
            @SerializedName("title")
            var title: String? = null

            @SerializedName("subTitle")
            var subTitle: String? = null

            @SerializedName("primeTitle")
            var primeTitle: String? = null

            @SerializedName("message")
            var message: String? = null
        }
    }

    @SerializedName("dummyUser")
    var isDummyUser:Boolean? = false

    @SerializedName("fdoOrderRaised")
    var isFDRRaised : Boolean? = false

    inner class KnowMore {
        @SerializedName("imageUrl")
        var imageUrl: String? = null

        @SerializedName("title")
        var title: String = ""

        @SerializedName("value")
        var value: String = ""
    }

    inner class NetflixData {

        @SerializedName("imageUrl")
        var imageUrl: String? = null

        @SerializedName("verbiage")
        var verbiage: String? = null

        @SerializedName("deviceVerbiage")
        var deviceVerbiage: String? = null
    }

    inner class ComboChannelsInfo {
        @SerializedName("verbiage")
        var verbiage: String? = null

        @SerializedName("ottChannels")
        var ottChannelsVerbiage: String? = null

        @SerializedName("channels")
        var channelsBreakdownInfo: List<Channels>? = null

        @SerializedName("footerVerbiageFirst")
        var footerVerbiageFirst : String? = null

        @SerializedName("footerVerbiageSecond")
        var footerVerbiageSecond : String? = null
    }

    data class Channels(
        @SerializedName("name") val name: String? = null,
        @SerializedName("count") val count: String
    ){
        val colorCode : String
            get() = if(name.equals("HD", true)) "#C9123E" else "#A3A6C2"
    }

//    @SerializedName("partnerDesc")
//    var extraInfo : String?=null

    @SerializedName("partnerDescHybrid", alternate = ["partnerDesc"])
    var extraInfo : String?=null

    @SerializedName("contentPlayBack")
    var contentPlayback : Boolean = false

    @SerializedName("logout")
    var forceLogout : Boolean = false

    @SerializedName("accountSubStatus")
    var accountSubStatus : String ? =null

    @SerializedName("contentPlayBackHybrid")
    var contentPlayBackHybrid : Boolean = true

    @Transient
    @SerializedName("dontUseNow")
    var forceLogoutHybrid : Boolean = false

    @SerializedName("dthStatus")
    var dthStatus : String? = null

    @SerializedName("modifyLink")
    var modifyBtn : Boolean? = false

    @SerializedName("cancelLink")
    var cancelBtn : Boolean? = false

    @SerializedName("modifyLinkMessage")
    var modifyBtnMessage : String? = null

    @SerializedName("cancelLinkMessage")
    var cancelBtnMessage : String? = null

    @SerializedName("subscriptionInformationDTO")
    var subscriptionDetailInfo : CommonSubscriptionDetailsModel?=null

    @SerializedName("nudges")
    var nudges: Nudges? = null

    @SerializedName("subscriptionExpiryMessage")
    var expiryDateToDisplay : String ? =null

    @SerializedName("accountScreenExpiryMessage")
    var accountScreenExpiryMessage : String ? =null

    @SerializedName("partnerUniqueIdInfo")
    val partnerUniqueIdInfo : ProviderInfo? = null

    @SerializedName("analyticsInfo")
    val analyticsInfo : ProviderInfo?= null

    @SerializedName("atvCancelled")
    val atvCancelled : Boolean? = null

    @SerializedName("atvCancelledMessage")
    val atvCancelledMessage : String ?= null

    @SerializedName("primeAutoSelect")
    val primeAutoSelect : Boolean? = null

    @SerializedName("subscriptionNudgesDetails")
    val subscriptionNudgeDetails: NudgeDetails? = null

    @SerializedName("freeTrialNudgesDetails")
    val freeTrialNudgeDetails: NudgeDetails? = null

    @SerializedName("setupNudgeDetails")
    val setupNudgeDetails: SiNudgeDetails?= null

    data class NudgeDetails(
        @SerializedName("nudgeToShowOnWhichDayBeforeExpiry", alternate = ["nudgeToShowOnWhichDayBeforeExpiryTrial"])
        val nudgeToShowOnWhichDayBeforeExpiry: Int? = null,

        @SerializedName("nudgeToShowOnWhichDayAfterExpiry", alternate = ["nudgeToShowOnWhichDayAfterExpiryTrial"])
        val nudgeToShowOnWhichDayAfterExpiry: Int? = null,

        @SerializedName("currentDay")
        val currentDay: Int? = null,

        @SerializedName("expiryDaysLeft")
        val expiryDaysLeft: Int? = null,

        @SerializedName("renewNudgeTitle", alternate=["freeNudgeTitle"])
        val nudgeTitle: String? = null,

        @SerializedName("renewNudgeMessage", alternate = ["freeNudgeMessage"])
        val nudgeMessage: String? = null,

        @SerializedName("renewNudgeButton", alternate=["freeNudgeButton"])
        val nudgeButton: String? = null
    )

    data class SiNudgeDetails(
        @SerializedName("setupNudgeMessage")
        val setupNudgeMessage: String? = null,

        @SerializedName("setupNudgeButton")
        val setupNudgeButton: String? = null,

        @SerializedName("setupNudgeOption")
        val setupNudgeOption: Boolean? = null
    )
}

/*class PartnerUniqueInfo {

    @SerializedName("partnerUniqueId")
    var partnerUniqueId: String = ""

    @SerializedName("partnerName")
    var partnerName: String = ""
}*/

class Nudges {
    @SerializedName("paidPackSelection")
    var paidPackSelectionNudge: PaidPackSelectionNudge? = null

}

class PaidPackSelectionNudge {
    @SerializedName("title")
    var title: String? = null

    @SerializedName("desc")
    var desc: String? = null

    @SerializedName("button")
    var button: String? = null

    @SerializedName("availableDays")
    var availableDays: Int = 0

    @SerializedName("startDay")
    var startDay: Int = 0

    @SerializedName("totalFreeTrialDuration")
    var totalFreeTrialDuration: Int = 0
}



class PrimePack {
    @SerializedName("title")
    var title: String? = ""

    @SerializedName("imageUrl")
    var imageUrl: String? = ""

    @SerializedName("squareImageUrl")
    var squareImageUrl: String? = ""

    @SerializedName("packAmount")
    var packAmount: String? = null

    @SerializedName("packStatus")
    var packStatus: Boolean = false

    @SerializedName("bundleState")
    var state: String? = null

    @SerializedName("expiryDate")
    var expiryDate: String? = ""

    @SerializedName("packType")
    private var packType: String? = null

    @SerializedName("primeMessageDetails")
    var primeMessageDetails:PrimeMessageDetails? = null

    @SerializedName("subscriptionExpiryMessage")
    var expiryDateToDisplay : String ? =null

    @SerializedName("accountScreenExpiryMessage")
    var accountScreenExpiryMessage : String ? =null

    @SerializedName("renewalCycle")
    var renewalCycle : String ? =null

    @SerializedName("platform")
    var platform : String ? = null

    @SerializedName("cancelled")
    var primeCancellationRaised : Boolean = false

    val isPaid
        get() = packType.equals("Paid", true)

    val isActive
        get() = state.equals(PRIME_ACTIVATED, true)

    val isSuspended
        get() = state.equals(PRIME_SUSPENDED, true)

    fun getFormattedPrice(): Int {
        if (packAmount.isNullOrEmpty()) return 0
        return try {
            val number = BigDecimal(packAmount)
            val formattedBalance: String = number.stripTrailingZeros().toPlainString()
            formattedBalance.toInt()
        } catch (e: Exception) {
            0
        }
    }
}
class PrimeMessageDetails{

    @SerializedName("primeTitle1")
    var primeTitle1:String? = null

    @SerializedName("primeTitle2")
    var primeTitle2:String? = null


    @SerializedName("primeMessage1")
    var primeMessage1:String? = null

    @SerializedName("primeMessage2")
    var primeMessage2:String? = null

    @SerializedName("statusType")
    var statusType : String?=null

}
