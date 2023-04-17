package com.tatasky.binge.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.*
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.text.*
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.Format
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tatasky.binge.R
import com.tatasky.binge.analytics.DEAFULT_DATE_FORMAT_FROM_BE
import com.tatasky.binge.analytics.HOME_CONTENT
import com.tatasky.binge.analytics.SOURCE_DEEPLINK
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.databinding.LayoutOtpViewWithoutHintBinding
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.helper.imageLoadRounded
import com.tatasky.binge.helper.imageLoadWithPlaceHolder
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.ui.features.games.GamePlayerActivity
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.subscription_freemium.FreemiumSubscriptionActivity
import com.tatasky.binge.ui.features.subscription_freemium.PaymentJourneyActivity
import com.tatasky.binge.ui.features.subscription_freemium.WalletPaymentActivity
import com.tatasky.binge.ui.features.zee5.InAppBrowserActivity
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

import kotlin.collections.ArrayList
import kotlin.collections.HashSet

private const val SECOND: Long = 1000
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

private const val HOUR_LIMIT: Long = 48 * HOUR
private const val MINUTE_LIMIT: Long = 59 * MINUTE
private const val SECOND_LIMIT: Long = 59 * SECOND


fun changeMillisToMins(millis: Long): Long {
    var millis = millis
    var mins: Long = 0
    millis -= System.currentTimeMillis()
    if (millis > 0) {
        mins = millis / MINUTE
    }
    return mins
}

fun changeMillisToHours(millisOrg: Long): Long {
    var millis = millisOrg
    var hours: Long = 0
    millis -= System.currentTimeMillis()
    if (millis > 0) {
        hours = millis / HOUR
        val min = millis / MINUTE_LIMIT
        e("checkRental", "hours : $hours, min: $min")
    }
    return hours
}

fun isNetworkConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
}

fun isInternetAvailable(): Boolean {
    val runtime = Runtime.getRuntime()
    try {
        val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
        val exitValue = ipProcess.waitFor()
        return exitValue == 0
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

/*fun isInternetAvailable(): Boolean {
    return try {
        val ipAddr: InetAddress = InetAddress.getByName("google.com")
        //You can replace it with your name
        !ipAddr.equals("")
    } catch (e: java.lang.Exception) {
        false
    }
}*/

fun getContentType(contentType: String): String {
    return when {
        contentType.contains(TYPE_BRAND_CHILD) || contentType.contains(TYPE_BRAND) -> TYPE_BRAND
        contentType.contains(TYPE_SERIES_CHILD) || contentType.contains(TYPE_SERIES) -> TYPE_SERIES
        else -> contentType
    }
}


fun stopOtherApplicationAudio(context: Context?) {
    try {
        if (context != null) {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            if (am != null) {
                val result = am.requestAudioFocus(
                    { i -> e("onAudioFocusChange", "onAudioFocusChange$i") },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    e("result ", "result$result")
                    // other app had stopped playing song now , so u can do u stuff now .
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        e("Exception ", "Exception stopOtherApplicationAudio")
    }

}
fun incrementLaunchCount(sharedPrefs: PrefsRepo){
    sharedPrefs.getConfigResponse()?.data?.config?.heroBannerRotation?.let {
        //for every x launch of homepage hero banner will increment its position by y
        //will come from cms
        val x = it.homeLaunchValue?.takeIf { it!=0 } ?:1
        val y = it.heroBannerIncrementValue ?: 0

        // Example:
        // appLaunchValue or x = 2, heroBannerIncrementValue or y = 1
        // 1st -- 1,2,3,4,5

        // 2nd -- 2,3,4,5,1
        // 3rd -- 2,3,4,5,1

        // 4th -- 3,4,5,1,2
        // 5th -- 3,4,5,1,2

        // 6th -- 4,5,1,2,3
        // 7th -- 4,5,1,2,3
        if(x==1 && y==0){
            sharedPrefs.resetHeroBannerCounts()
        } else {
            var c = sharedPrefs.getAppLaunchCount()
            c++
            sharedPrefs.setAppLaunchCount(c)
            d("Home Launch", "Home Launch Count: $c")
            if (sharedPrefs.getAppLaunchCount() % x == 0) {
                // whenever u increment the indicator position save its position for the next rotation
                val increment = sharedPrefs.getCurrentStartingPosition() + y
                sharedPrefs.setCurrentStartingPosition(increment)
            }
        }
    } ?:run {
        sharedPrefs.resetHeroBannerCounts()
    }
}

fun releaseOtherApplicationAudio(context: Context?) {
    try {
        if (context != null) {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            if (am != null) {
                am.abandonAudioFocus {
                    e("onAudioFocusChange", "abandonAudioFocus : $it")
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        e("Exception ", "Exception stopOtherApplicationAudio")
    }

}

fun getRealDisplayPoint(context: Context): Point {
    val size = Point()
    try {
        val display = (context as Activity).windowManager.defaultDisplay
        display.getRealSize(size)
        return size
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return size
}

fun isTablet(view: View): Boolean {
    return false//view.context.resources.getBoolean(R.bool.portrait_only)
}

fun isTablet(view: Context): Boolean {
    return false//view.context.resources.getBoolean(R.bool.portrait_only)
}

fun dpToPx(iContext: Context, dp: Int): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), iContext.resources.displayMetrics)
        .toInt()
//    val density = iContext.resources.displayMetrics.density
//    return Math.round(dp.toFloat() * density)
}
/*

fun getCloudineryUrl(url: String, width: Int, height: Int, context: Context): String {
    val cloudineryUrl = SharedPrefs.getString(context, SharedPrefs.PREF_KEY_CLOUDINARY_URL)

    val w = "w_$width"
    val h = ",h_$height"
    val style = ",c_scale/"

    if (cloudineryUrl == null || cloudineryUrl.toString().isEmpty()) {
        return url
    }
    val curl: String
    if (width < 600) {
        curl = "$cloudineryUrl$w$h,f_webp,q_auto:eco$style$url"
    } else {
        curl = "$cloudineryUrl$w$h$,f_webp,q_auto$style$url"
    }
    //        Logger.d("image url", curl);
    return curl
}

fun getRoundedCloudnaryUrl(url: String, width: Int, height: Int, context: Context): String {
    val cloudineryUrl = SharedPrefs.getString(context, SharedPrefs.PREF_KEY_CLOUDINARY_URL)

    val w = "w_$width"
    val h = ",h_$height"
    val style = ",f_webp,q_auto:low/"
    val r = ",r_max"

    return if (cloudineryUrl == null || cloudineryUrl.toString().isEmpty()) {
        url
    } else "$cloudineryUrl$w$h$r$style$url"

//        Logger.d("image url logo", curl);
}*/

private val NUMBER_OF_NORMAL_COLUMN_GENRE = 2.5f
private val NUMBER_OF_NORMAL_COLUMN_CATEGORY = 3.5f
private val NUMBER_OF_NORMAL_COLUMN_PHONE = 2.5f
private val NUMBER_OF_NORMAL_COLUMN_PHONE_GRID = 2.3f
var NUMBER_OF_NORMAL_COLUMN_PHONE_KID = 1.175f
private val NUMBER_OF_PORTRAIT_COLUMN_PHONE = 3.75f
private const val NUMBER_OF_PORTRAIT_TOP_10_COLUMN_PHONE = 2.45f
private val NUMBER_OF_BUNDLE_COLUMN_PHONE = 2.75f
private val NUMBER_OF_GENRE_COLUMN_PHONE = 4.8f
private val NUMBER_OF_APPS_COLUMN_PHONE_LANDSCAPE = 5.0f
private val NUMBER_OF_PORTRAIT_COLUMN_PHONE_GRID = 2.15f
private val NUMBER_OF_PORTRAIT_COLUMN_PHONE_KID = 1.8f
private val NUMBER_OF_EPISODE_COLUMN = 1.6f
private val NUMBER_OF_COMPARE_PLAN_CARD_COLUMN = 3.25f

private val NUMBER_OF_NORMAL_COLUMN_TABLET = 5f
private val NUMBER_OF_NORMAL_COLUMN_TABLET_KID = 3.3f
private val NUMBER_OF_PORTRAIT_COLUMN_TABLET_RELATED = 6.5f
private val NUMBER_OF_PORTRAIT_COLUMN_TABLET = 6f
private const val NUMBER_OF_PORTRAIT_TOP_10_COLUMN_TABLET = 4f
const val THUMBNAIL_RATIO_LARGE_GRID = 1.55
const val THUMBNAIL_RATIO_NORMAL_GRID = .58
const val THUMBNAIL_RATIO_LARGE = 1.55
const val THUMBNAIL_RATIO_NORMAL = 0.58
const val THUMBNAIL_RATIO_APP_NORMAL = 1.0

fun getDeviceDimension(context: Context?): Point {
    if (context == null) {
        return Point()
    }
    val wm = context
        .getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val size = Point()
    display.getSize(size)
    if (size.x > size.y)
        size.set(size.y, size.x)
    return size
}

fun getSportsBgImageDimension(context: Context): Point {
    val deviceDimensionPoint = getDeviceDimension(context)
    val imageDimension = Point()
    imageDimension.x = (deviceDimensionPoint.x / 1.08 - (1.07 + 1)).toInt()
    imageDimension.y = (imageDimension.x * THUMBNAIL_RATIO_NORMAL).toInt()
    return imageDimension
}

fun getNormalThumbnailDimension(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = NUMBER_OF_NORMAL_COLUMN_PHONE
    if (isTablet(activity)) {
        columnNumber = NUMBER_OF_NORMAL_COLUMN_TABLET
    }
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_NORMAL).toInt()
    return point
}

fun getEpisodeThumbnailDimension(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = NUMBER_OF_EPISODE_COLUMN
    if (isTablet(activity)) {
        columnNumber = NUMBER_OF_NORMAL_COLUMN_TABLET
    }
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_NORMAL).toInt()
    return point
}

fun getComparePlanCardWidth(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = NUMBER_OF_COMPARE_PLAN_CARD_COLUMN
    if (isTablet(activity)) {
        columnNumber = NUMBER_OF_COMPARE_PLAN_CARD_COLUMN
    }
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_NORMAL).toInt()
    return point
}

fun getPackCardDimension(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = 1.2f
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * 1.75).toInt()
    return point
}

fun getMidscrollCardDimension(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = 1.18f
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * 0.56f).toInt()
    return point
}


fun getEpisodeThumbnailDimension2(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = 2.2f
    if (isTablet(activity)) {
        columnNumber = NUMBER_OF_NORMAL_COLUMN_TABLET
    }
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_NORMAL).toInt()
    return point
}

fun getNormalThumbnailDimensionGrid(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = NUMBER_OF_NORMAL_COLUMN_PHONE_GRID
    if (isTablet(activity)) {
        columnNumber = NUMBER_OF_NORMAL_COLUMN_TABLET
    }
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_NORMAL_GRID).toInt()
//    e("UtilsKt","point x: "+point.x)
//    e("UtilsKt","point y: "+point.y)
    return point
}

fun getLargeThumbnailDimension(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = NUMBER_OF_PORTRAIT_COLUMN_PHONE
    if (isTablet(activity)) {
        columnNumber = NUMBER_OF_PORTRAIT_COLUMN_TABLET
    }
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_LARGE).toInt()
    e("UtilsKt","Portrait point x: "+point.x)
    e("UtilsKt","Portrait point y: "+point.y)
    return point
}

fun getLargeThumbnailTop10Dimension(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = NUMBER_OF_PORTRAIT_TOP_10_COLUMN_PHONE
    if (isTablet(activity)) {
        columnNumber = NUMBER_OF_PORTRAIT_TOP_10_COLUMN_TABLET
    }
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_LARGE).toInt()
//    e("UtilsKt","point x: "+point.x)
//    e("UtilsKt","point y: "+point.y)
    return point
}

fun getBundleDimension(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = NUMBER_OF_BUNDLE_COLUMN_PHONE
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_NORMAL_GRID).toInt()
    return point
}

fun getLargeThumbnailDimensionGrid(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = NUMBER_OF_PORTRAIT_COLUMN_PHONE_GRID
    if (isTablet(activity)) {
        columnNumber = NUMBER_OF_PORTRAIT_COLUMN_TABLET
    }
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_LARGE_GRID).toInt()
//    e("UtilsKt","point x: "+point.x)
//    e("UtilsKt","point y: "+point.y)
    return point
}


fun getCloudinaryUrl(cloudinaryUrl: String?, url: String): String {
    return cloudinaryUrl +"e_trim,f_webp,q_auto:good/"+ url
}

fun getCloudinaryUrl(cloudinaryUrl: String?, width: Int, height: Int, url: String): String {
    return if (null != cloudinaryUrl) {
        val encodedUrl = URLEncoder.encode(url, "utf-8")
        cloudinaryUrl + "w_" + width + ",h_" + height + ",f_webp,q_auto:good/" + encodedUrl
    }
    else
        url
}

fun getDateForHomeTile(timeStamp: Long): String {
    try {
        val sdf = SimpleDateFormat("dd MMM yy")
        val netDate = Date(timeStamp)
        //            Date netDate = (sdf.parse(date));
        return sdf.format(netDate)
    } catch (e: Exception) {
        e.printStackTrace()
        return "xx"
    }

}

/**
 * @return Time in "dd-MMM" format
 */
fun getTimeCatchUp(milliSeconds: Long): String {
    return getTime(milliSeconds, "dd-MMM")
}


fun getTime(milliSeconds: Long, pattern: String): String {
    if (milliSeconds == 0L) return ""

    val date = Date(milliSeconds)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}

fun getTimeInUTC(milliSeconds: Long, pattern: String): String {
    if (milliSeconds == 0L) return ""

    val date = Date(milliSeconds)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(date)
}

fun isValidContent(it: ContentItem): Boolean {
    var contentType = it.contentType
    if(
//        (PROVIDER_TATA_SKY.equals(it.provider, true) &&
//                !RENTAL.equals(it.contractName, true) ||
//                RENTAL_PURCHASED_NOTEXPIRED.equals(it.rentalStatus, ignoreCase = true)) ||
        !(PROVIDER_CURIOSITY_STREAM.equals(it.provider, true) ||
                PROVIDER_HOTSTAR.equals(it.provider, true) ||
                PROVIDER_HUNGAMA.equals(it.provider, true) ||
                PROVIDER_ZEE5.equals(it.provider, true) ||
                PROVIDER_SHEMAROO.equals(it.provider, true) ||
                PROVIDER_VOOTKIDS.equals(it.provider, true) ||
                PROVIDER_VOOTSELECT.equals(it.provider, true) ||
                PROVIDER_PRIME.equals(it.provider, true) ||
                PROVIDER_EROSNOW.equals(it.provider, true) ||
                PROVIDER_SONYLIV.equals(it.provider, true) ||
                PROVIDER_EPIC_ON.equals(it.provider, true) ||
                PROVIDER_DOCU_BAY.equals(it.provider, true) ||
                PROVIDER_HOICHOI.equals(it.provider, true) ||
                PROVIDER_GAMEZOP.equals(it.provider, true) ||
                PROVIDER_MXPLAYER.equals(it.provider, true) ||
                PROVIDER_CHAUPAL.equals(it.provider, true) ||
                PROVIDER_PLANET_MARATHI.equals(it.provider, true) ||
                PROVIDER_NAMMAFLIX.equals(it.provider, true) ||
                PROVIDER_LIONSGATE.equals(it.provider, true) ||
                PROVIDER_TATA_SKY.equals(it.provider, true) ||
                ProvidersCache.allowedProviderList.contains(it.provider.lowercase())
                /*(PROVIDER_TATA_SKY.equals(it.provider, true) &&
                        !RENTAL.equals(it.contractName, true) ||
                        RENTAL_PURCHASED_NOTEXPIRED.equals(it.rentalStatus, ignoreCase = true))*/
                )
    )
        return false

    if(contentType.contains(TYPE_MOVIES, true))
        it.contentType = TYPE_MOVIES
    else if(contentType.contains(TYPE_SUB_PAGE, true))
        it.contentType = TYPE_SUB_PAGE
    else if(contentType.contains(TYPE_SERIES, true))
        it.contentType = TYPE_SERIES
    else if(contentType.contains(TYPE_BRAND, true))
        it.contentType = TYPE_BRAND
    else if(contentType.contains(TYPE_WEB_SHORTS, true))
        it.contentType = TYPE_WEB_SHORTS
    else if(contentType.contains(TYPE_TV_SHOWS, true))
        it.contentType = TYPE_TV_SHOWS
    else if(contentType.contains(TYPE_GAMES, true))
        it.contentType = TYPE_GAMES


    if (contentType.contains(TYPE_MOVIES, ignoreCase = true)
        || contentType.contains(TYPE_WEB_SHORTS, ignoreCase = true)
        || contentType.contains(TYPE_TV_SHOWS, ignoreCase = true)
        || contentType.contains(TYPE_BRAND_CHILD, ignoreCase = true)
        || contentType.contains(TYPE_SERIES_CHILD, ignoreCase = true)
        || contentType.contains(TYPE_BRAND, ignoreCase = true)
        || contentType.contains(TYPE_SERIES, ignoreCase = true)
        || contentType.contains(TYPE_SUB_PAGE, ignoreCase = true)
        || contentType.contains(TYPE_GAMES, ignoreCase = true)
    ){
        return (!RENTAL.equals(it.contractName, true)
                || !RENTAL_PURCHASED_EXPIRED.equals(it.rentalStatus, ignoreCase = true))
    }

    return false
}


fun isValidItems(items: HomeResponse.Items, dthStatus: String?): Boolean {
    e("getNormalItemViewType","dthStatus : $dthStatus, items.sectionSource: ${items.sectionSource}," +
            "ItemViewType.LANGUAGE_SECTION.name: ${ItemViewType.LANGUAGE_SECTION.name}")
    items.dthStatus = dthStatus
    if (items.sectionSource.equals(RECOMMENDATION, ignoreCase = true)
        || items.sectionSource.equals(CONTINUE_WATCHING, ignoreCase = true)
        || items.sectionSource.equals(INTENT_LANGUAGE, ignoreCase = true)
        || items.sectionSource.equals(INTENT_GENRE, ignoreCase = true)
        || items.sectionSource.equals(TVOD, ignoreCase = true)
        || items.sectionSource.equals(EDITORIAL, ignoreCase = true) //TODO DRP CONFIRM
        || items.sectionSource.equals(PROVIDER, ignoreCase = true) //TODO DRP CONFIRM
        || items.sectionSource.equals(ItemViewType.BACKGROUND_BANNER_RAIL.name,ignoreCase = true) //TODO DRP CONFIRM
        || items.sectionSource.equals(ItemViewType.BINGE_TOP_10_RAIL.name,ignoreCase = true) //TODO DRP CONFIRM
        || items.sectionSource.equals(ItemViewType.PROVIDER_BROWSE_APPS.name,ignoreCase = true) //TODO DRP CONFIRM
        || items.sectionSource.equals(ItemViewType.HB_SEE_ALL.name,ignoreCase = true) //TODO DRP CONFIRM
        || items.sectionSource.equals(ItemViewType.LIVE_EVENT_RAIL.name,ignoreCase = true) //TODO DRP CONFIRM
        || items.sectionSource.equals(ItemViewType.FAVOURITES.name, ignoreCase = true)
        || items.sectionSource.equals(ItemLayoutType.POPULAR_CHARACTER.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.LANGUAGE_SECTION.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.LANGUAGE_NUDGE.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.SHUFFLE_RAIL.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.PROVIDER_UNSUBSCRIBED.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.MID_BANNER_RAIL.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.MID_BANNER_GAMES.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.MID_BANNER_PROMO.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.MID_SCROLL_BANNER.name, ignoreCase = true)
        || items.sectionSource.equals(WATCHLIST, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.GENRE_RAIL_FOR_GAMES.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.GAMES.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.NEWLY_ADDED_GAMES.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.GAME_OF_THE_WEEK.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.CATEGORY.name, ignoreCase = true)
        || items.sectionSource.equals(ItemViewType.LIVE_EVENT_BANNER.name, ignoreCase = true)
    ) {
        return true
    }
    return items.filteredContentItems.isNotEmpty()
}

fun isFreeContent(
    contractName: String?,
    subscribedEntitlements: Set<String>?,
    partnerId: String?,
    subcriptionStatus: String?
): Boolean {
    when (contractName) {
        RENTAL -> return true
        FREE, CLEAR, SUBSCRIPTION -> {
            if ((SubscriptionPackStatusEnum.ACTIVE.status.equals(
                    subcriptionStatus,
                    true
                )) && !subscribedEntitlements.isNullOrEmpty()) {

                if (subscribedEntitlements.contains(partnerId)) {
                    return true
                }
            }
        }
        null -> return true
    }
    return false
}

fun calculateDuration(minutes: Int): String {
    val minutes = minutes / 60
    val hour = minutes.div(60)
    val minute = minutes.rem(60)
    return if(hour > 0 && minute > 0)
        "" + hour + "h " + minute + "m"
    else if(hour > 0)
        "" + hour + "h"
    else "" + minute + "m"
}

fun bitmapToByte(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}

fun getCharcterGenrePoint(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = NUMBER_OF_GENRE_COLUMN_PHONE
    if (isTablet(activity)) {
        columnNumber = NUMBER_OF_GENRE_COLUMN_PHONE
    }
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_APP_NORMAL).toInt()
    return point
}

fun getProviderIconLandscapePoint(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = NUMBER_OF_APPS_COLUMN_PHONE_LANDSCAPE
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * .78).toInt()
    return point
}

fun getFormattedMobile(mobileNumber: String): String {
    if (mobileNumber.length == 10) {
        val number = mobileNumber.replaceFirst("(\\d{2})(\\d{6})(\\d+)".toRegex(), "$1XXXXXX$3")
        val encryptedMobile =
            number.replaceFirst("(\\w{2})(\\w{4})(\\w+)".toRegex(), "$1 $2 $3")
        return encryptedMobile
    }
    return ""
}

/** Returns the seconds as in clock format 02:24  */
fun formatMinuteSeconds(totalSeconds: Long): String {
    if (totalSeconds == null || "".equals(totalSeconds)) {
        return "0:0"
    }
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return String.format("%02d:%02d", minutes, seconds)
}

fun checkForValidEventName(name: String):String {

    if(TextUtils.isEmpty(name)){
        return name
    }
    var eventName=name.trim()
    if (eventName.contains("_")) {
        eventName= eventName.replace("_", "-")
    }
    if (eventName.toCharArray().any(Character::isLowerCase)) {
        eventName=eventName.toUpperCase()
    }
    if(eventName.contains("-\\s+".toRegex())){
        eventName=eventName.replace("-\\s+".toRegex(), "-")
    }
    if(eventName.contains("\\s-+".toRegex())){
        eventName=eventName.replace("\\s-+".toRegex(), "-")
    }
    if(eventName.contains("\\s+".toRegex())){
        eventName=eventName.replace("\\s+".toRegex(), " ")
        eventName=eventName.replace("\\s+".toRegex(), "-")
    }

    return eventName
}

fun checkForValidFirebaseEventName(name: String):String {

    if(TextUtils.isEmpty(name)){
        return name
    }
    var eventName=name.trim()
    if (eventName.contains("-")) {
        eventName= eventName.replace("-", "_")
    }
    if (eventName.toCharArray().any(Character::isLowerCase)) {
        eventName=eventName.toUpperCase()
    }
    if(eventName.contains("_\\s+".toRegex())){
        eventName=eventName.replace("_\\s+".toRegex(), "_")
    }
    if(eventName.contains("\\s_+".toRegex())){
        eventName=eventName.replace("\\s_+".toRegex(), "_")
    }
    if(eventName.contains("\\s+".toRegex())){
        eventName=eventName.replace("\\s+".toRegex(), "_")
    }

    return eventName
}

/**Firebase don't accept (-) as Parameter name/key, So replace - with _ */
fun checkForValidFirebaseParamName(bundle: Bundle?): Bundle? {
    var paramBundle: Bundle? = null
    if (bundle != null) {
        paramBundle = Bundle()
        for (key in bundle.keySet()) {
            paramBundle.putString(checkForValidFirebaseEventName(key), bundle.get(key)?.toString())
        }
    }
    return paramBundle
}

fun isRentalContent(it: ArrayList<ContentItem>): Boolean {
    for (i in 0 until it.size) {
        val contentItem = it[i]
        if(RENTAL.equals(contentItem.contractName, true)){
            return true
        }
    }
    return false
}

fun filterRentalExpiry(
    cwResponse: ArrayList<ContentItem>?,
    mRentalContentList: List<ContentItem>?
): ArrayList<ContentItem>?{
    if (cwResponse!= null) {
        val newContentresult = ArrayList<ContentItem>()
        for (cwContentResult in cwResponse) {
            if (RENTAL.equals(cwContentResult.contractName, ignoreCase = true)) {
                if(mRentalContentList != null){
                    for (rentalContentResult in mRentalContentList) {
                        e(
                            "filterRantal",
                            "rentalContentResult.title : ${rentalContentResult.title}"
                        )
                        if (rentalContentResult.id.equals(cwContentResult.contentId)) {
                            cwContentResult.rentalExpiry = rentalContentResult.rentalExpiry
                            cwContentResult.rentalStatus = rentalContentResult.rentalStatus
                            cwContentResult.rentalPrice = rentalContentResult.rentalPrice
                            newContentresult.add(cwContentResult)
                            e(
                                "filterRantal",
                                "added rentalContentResult.title : ${rentalContentResult.title}"
                            )
                            break
                        }
                        else if (rentalContentResult.id.equals(cwContentResult.id)) {
                            cwContentResult.rentalExpiry = rentalContentResult.rentalExpiry
                            cwContentResult.rentalStatus = rentalContentResult.rentalStatus
                            cwContentResult.rentalPrice = rentalContentResult.rentalPrice
                            newContentresult.add(cwContentResult)
                            e(
                                "filterRantal",
                                "added rentalContentResult.title : ${rentalContentResult.title}"
                            )
                            break
                        }
                        e(
                            "filterRantal",
                            "outside added rentalContentResult.title : ${rentalContentResult.title}"
                        )
                    }
                }
            }
            else if(isValidContent(cwContentResult)){
                e("filterRantal", "valid content cwContentResult.title : ${cwContentResult.title}")
                newContentresult.add(cwContentResult)
            }
        }
        return newContentresult
    }
    return cwResponse
}

fun filterProviderOnly(provider: String) : Boolean {
    if(!(PROVIDER_CURIOSITY_STREAM.equals(provider, true) ||
                PROVIDER_HOTSTAR.equals(provider, true) ||
                PROVIDER_HUNGAMA.equals(provider, true) ||
                PROVIDER_ZEE5.equals(provider, true) ||
                PROVIDER_SHEMAROO.equals(provider, true) ||
                PROVIDER_VOOTKIDS.equals(provider, true) ||
                PROVIDER_PRIME.equals(provider, true) ||
                PROVIDER_VOOTSELECT.equals(provider, true) ||
                PROVIDER_EROSNOW.equals(provider, true) ||
                PROVIDER_SONYLIV.equals(provider, true) ||
                PROVIDER_EPIC_ON.equals(provider, true) ||
                PROVIDER_DOCU_BAY.equals(provider, true) ||
                PROVIDER_HOICHOI.equals(provider, true) ||
                PROVIDER_GAMEZOP.equals(provider, true) ||
                PROVIDER_MXPLAYER.equals(provider, true) ||
                PROVIDER_CHAUPAL.equals(provider, true) ||
                PROVIDER_PLANET_MARATHI.equals(provider, true) ||
                PROVIDER_NAMMAFLIX.equals(provider, true) ||
                PROVIDER_TATA_SKY.equals(provider, true)||
                PROVIDER_LIONSGATE.equals(provider, true) ||
                ProvidersCache.allowedProviderList.contains(provider.toLowerCase())
                ))
        return false
    return true
}

fun isValidRentalContent(
    it: ContentItem,
    dthStatus: String?,
    sectionSource: String,
    sectionType: String,
    refId : String
): Boolean {

    it.refId = refId
    if(sectionSource == ItemViewType.MID_BANNER_RAIL.name
        ||sectionSource == ItemViewType.MID_BANNER_GAMES.name
        ||sectionSource == ItemViewType.BACKGROUND_BANNER_RAIL.name
        ||sectionSource == ItemViewType.PROVIDER_BROWSE_APPS.name
        ||sectionSource == ItemViewType.SHUFFLE_RAIL.name)
        it.railCategory = sectionSource
    else/* if(sectionType.equals(ItemViewType.RAIL.name, ignoreCase = true) ||
        sectionType == ItemViewType.HERO_BANNER.name
        || sectionType == ItemViewType.CONTINUE_WATCHING.name)*/
        it.railCategory = sectionType
    e("FilterItems","it.provider : ${it.provider}, sectionSource : $sectionSource")

    if(it.contentType == null || "null".equals(it.contentType, true) ||
        (it.provider.isBlank() && it.heroBannerType.equals(HB_SEE_ALL, true))) {
        it.contentType = ""
        return true
    }
    if(//HERO_BANNER
        !sectionSource.equals(ItemViewType.HERO_BANNER.name, true) &&
        !sectionSource.equals(ItemViewType.PRIME.name, true)
        && PROVIDER_PRIME.equals(it.provider, true)
        &&(dthStatus.isNullOrEmpty() || NON_DTH_USER.equals(dthStatus, true))
    ) {
        return false
    }
    if(!(PROVIDER_CURIOSITY_STREAM.equals(it.provider, true) ||
                PROVIDER_HOTSTAR.equals(it.provider, true) ||
                PROVIDER_HUNGAMA.equals(it.provider, true) ||
                PROVIDER_ZEE5.equals(it.provider, true) ||
                PROVIDER_SHEMAROO.equals(it.provider, true) ||
                PROVIDER_VOOTKIDS.equals(it.provider, true) ||
                PROVIDER_PRIME.equals(it.provider, true) ||
                PROVIDER_VOOTSELECT.equals(it.provider, true) ||
                PROVIDER_EROSNOW.equals(it.provider, true) ||
                PROVIDER_SONYLIV.equals(it.provider, true) ||
                PROVIDER_EPIC_ON.equals(it.provider, true) ||
                PROVIDER_DOCU_BAY.equals(it.provider, true) ||
                PROVIDER_HOICHOI.equals(it.provider, true) ||
                PROVIDER_GAMEZOP.equals(it.provider, true) ||
                PROVIDER_MXPLAYER.equals(it.provider, true) ||
                PROVIDER_CHAUPAL.equals(it.provider, true) ||
                PROVIDER_PLANET_MARATHI.equals(it.provider, true) ||
                PROVIDER_NAMMAFLIX.equals(it.provider, true) ||
                PROVIDER_LIONSGATE.equals(it.provider, true) ||
                PROVIDER_TATA_SKY.equals(it.provider, true) ||
                ProvidersCache.allowedProviderList.contains(it.provider.lowercase())
                /*(PROVIDER_TATA_SKY.equals(it.provider, true) &&
                        RENTAL.equals(it.contractName, true))*/
                ))
        return false
    if(it.seriesvrId?.isNotEmpty() == true) {
        it.id = it.seriesvrId?: it.id
        it.title = it.seriesTitle ?: it.title
        it.contentType = it.seriescontentType ?: it.contentType
        it.image = it.seriesimage ?: it.image
    }
    var contentType = it.contentType
    if(contentType.contains(TYPE_MOVIES, true))
        it.contentType = TYPE_MOVIES
    else if(contentType.contains(TYPE_SUB_PAGE, true))
        it.contentType = TYPE_SUB_PAGE
    else if(contentType.contains(TYPE_SERIES, true))
        it.contentType = TYPE_SERIES
    else if(contentType.contains(TYPE_BRAND, true))
        it.contentType = TYPE_BRAND
    else if(contentType.contains(TYPE_WEB_SHORTS, true))
        it.contentType = TYPE_WEB_SHORTS
    else if(contentType.contains(TYPE_TV_SHOWS, true))
        it.contentType = TYPE_TV_SHOWS
    else if(contentType.contains(TYPE_GAMES, true))
        it.contentType = TYPE_GAMES

    if (contentType.contains(TYPE_MOVIES, ignoreCase = true)
        || contentType.contains(TYPE_WEB_SHORTS, ignoreCase = true)
        || contentType.contains(TYPE_TV_SHOWS, ignoreCase = true)
        || contentType.contains(TYPE_BRAND_CHILD, ignoreCase = true)
        || contentType.contains(TYPE_SERIES_CHILD, ignoreCase = true)
        || contentType.contains(TYPE_BRAND, ignoreCase = true)
        || contentType.contains(TYPE_SERIES, ignoreCase = true)
        || contentType.contains(TYPE_SUB_PAGE, ignoreCase = true)
        || contentType.contains(TYPE_GAMES, ignoreCase = true)
    ){
        return (!RENTAL.equals(it.contractName, true)
                || RENTAL_PURCHASED_NOTEXPIRED.equals(it.rentalStatus, ignoreCase = true))
    }

    return false
}


fun updateCircularProviderLogo(
    img: ImageView,
    provider: String,
    providerLogos: ProviderLogo,
    @DrawableRes placeHolder: Int,
    cloudinaryUrl: String?
) {
    val point = getCharcterGenrePoint(img.context!!)
    val dimen = point.x
    val appName = getAppForProvider(provider, providerLogos)
    if(PROVIDER_TATA_SKY.equals(provider, ignoreCase = true)||
        appName?.logoCircular.isNullOrEmpty())
        img.setPartnerLogo(provider)
    else {
        val url = /*cloudinaryUrl + */appName?.logoCircular
        url?.let { transparentImageLoad(img, it) }
        //imageLoadRounded(img, url, dimen / 2, placeHolder)
    }
}
fun updateProviderLogo(
    img: ImageView,
    provider: String,
    providerLogos: ProviderLogo,
    @DrawableRes placeHolder: Int,
    cloudinaryUrl: String?
) {
    val point = getCharcterGenrePoint(img.context!!)
    val dimen = point.x
    val appName = getAppForProvider(provider, providerLogos)
    if(appName == null){
        val url = cloudinaryUrl + ProvidersCache.availableProviders[provider.lowercase()]?.logoCircular
        imageLoadRounded(img, url, dimen / 2, placeHolder)
    }else {
        if (appName?.logoCircular.isNullOrEmpty())
            img.setPartnerLogo(provider)
        else {
            val url = cloudinaryUrl + appName?.logoCircular
            imageLoadRounded(img, url, dimen / 2, placeHolder)
//        imageLoad(img, url)
        }
    }
}

fun updateProviderImage(
    ivBrand: ImageView,
    provider: String,
    providerLogos: ProviderLogo,
    @DrawableRes placeHolder: Int
) {
    val appName = getAppForProvider(provider, providerLogos)
    if(appName==null) imageLoadWithPlaceHolder(ivBrand, ProvidersCache.availableProviders[provider.lowercase()]?.logoRectangular ?: "", placeHolder)
    else {
        if (appName?.logoRectangular.isNullOrEmpty()
            ||
            PROVIDER_TATA_SKY.equals(provider, ignoreCase = true)
        )
            ivBrand.setPartnerLogo(provider)
        else
            imageLoadWithPlaceHolder(ivBrand, appName?.logoRectangular ?: "", placeHolder)
    }
}
fun updateCircularProviderImage(
    ivBrand: ImageView,
    provider: String,
    providerLogos: ProviderLogo,
    @DrawableRes placeHolder: Int
) {
    val appName = getAppForProvider(provider, providerLogos)
    if(appName==null) imageLoadWithPlaceHolder(ivBrand, ProvidersCache.availableProviders[provider.lowercase()]?.logoCircular ?: "", placeHolder)
    else {
        if (appName?.logoCircular.isNullOrEmpty()
            ||
            PROVIDER_TATA_SKY.equals(provider, ignoreCase = true)
        )
            ivBrand.setPartnerLogo(provider)
        else
            imageLoadWithPlaceHolder(ivBrand, appName?.logoCircular ?: "", placeHolder)
    }
}

fun ImageView.setPartnerLogo(provider: String){
    when (provider?.lowercase(Locale.getDefault())) {
        PROVIDER_HUNGAMA.lowercase(Locale.getDefault()) -> setImageResource(
            R.drawable.logo_hungama_details
        )
        PROVIDER_SUN_NEXT.lowercase(Locale.getDefault()) -> setImageResource(
            R.drawable.ic_sunnxt_logo
        )
        PROVIDER_HOTSTAR.lowercase(Locale.getDefault()) -> setImageResource(
            R.drawable.ic_hotstar_logo
        )
        PROVIDER_EROSNOW.lowercase(Locale.getDefault()) -> setImageResource(
            R.drawable.ic_eros_logo
        )
        PROVIDER_ZEE5.lowercase(Locale.getDefault()) -> setImageResource(
            R.drawable.ic_zee_details_pi
        )
        PROVIDER_PRIME.lowercase(Locale.getDefault()) -> setImageResource(
            R.drawable.ic_prime_video_logo
        )
        PROVIDER_SHEMAROO.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.shemaroo_detail_logo)

        PROVIDER_VOOTSELECT.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_voot_select_logo)

        PROVIDER_VOOTKIDS.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_voot_kids_logo)
        PROVIDER_CURIOSITY_STREAM.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_curiosity)
        PROVIDER_SONYLIV.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_sony_liv)
        PROVIDER_EPIC_ON.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_epicon)
        PROVIDER_DOCU_BAY.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_docubay)
        PROVIDER_HOICHOI.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_hoichoi)
        PROVIDER_GAMEZOP.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_gamezop_transparent)
        PROVIDER_CHAUPAL.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_chaupal_logo)
        PROVIDER_MXPLAYER.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_mx_player)
        PROVIDER_PLANET_MARATHI.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_planetmarathi)
        PROVIDER_NAMMAFLIX.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_nammaflix_logo)
        PROVIDER_LIONSGATE.lowercase(Locale.getDefault()) -> setImageResource(R.drawable.ic_account)
        else -> {
            setImageResource(R.drawable.logo_tatasky_details)
        }
    }
}


fun updateProviderSeeAll(
    ivBrand: ImageView,
    provider: String,
    providerLogos: ProviderLogo,
    cloudinaryUrl: String?
) {

    val appName = getAppForProvider(provider, providerLogos)
    val url:String?
    if(appName == null){
        url = cloudinaryUrl + ProvidersCache.availableProviders[provider.lowercase()]?.logoCircular
    }else url = cloudinaryUrl + appName?.logoCircular
    imageLoad(ivBrand, url)
}

fun updateProviderBanner(ivBrand: ImageView, provider: String, providerLogos: ProviderLogo) {

    val appName = getAppForProvider(provider, providerLogos)
    imageLoad(ivBrand, appName?.unsubscribedMob ?: "")
}

fun getAppForProvider(provider: String, providerLogos: ProviderLogo) : APP_NAME?{
    when (provider.lowercase(Locale.getDefault())) {
        PROVIDER_HUNGAMA.lowercase(Locale.getDefault()) -> return providerLogos.HUNGAMA

        PROVIDER_SUN_NEXT.lowercase(Locale.getDefault()) -> return providerLogos.SUNNXT

        PROVIDER_HOTSTAR.lowercase(Locale.getDefault()) -> return providerLogos.HOTSTAR

        PROVIDER_EROSNOW.lowercase(Locale.getDefault()) -> return providerLogos.EROSNOW

        PROVIDER_ZEE5.lowercase(Locale.getDefault()) -> return providerLogos.ZEE5

        PROVIDER_PRIME.lowercase(Locale.getDefault()) -> return providerLogos.PRIME

        PROVIDER_SHEMAROO.lowercase(Locale.getDefault()) -> return providerLogos.SHEMAROOME

        PROVIDER_VOOTSELECT.lowercase(Locale.getDefault()) -> return providerLogos.VOOTSELECT

        PROVIDER_VOOTKIDS.lowercase(Locale.getDefault()) -> return providerLogos.VOOTKIDS

        PROVIDER_CURIOSITY_STREAM.lowercase(Locale.getDefault()) -> return providerLogos.CuriosityStream

        PROVIDER_SONYLIV.lowercase(Locale.getDefault()) -> return providerLogos.SONYLIV
        PROVIDER_EPIC_ON.lowercase(Locale.getDefault()) -> return providerLogos.EPIC_ON
        PROVIDER_DOCU_BAY.lowercase(Locale.getDefault()) -> return providerLogos.DOCU_BAY
        PROVIDER_HOICHOI.lowercase(Locale.getDefault()) -> return providerLogos.HOICHOI
        PROVIDER_MXPLAYER.lowercase(Locale.getDefault()) -> return providerLogos.MXPLAYER
        PROVIDER_CHAUPAL.lowercase(Locale.getDefault()) -> return providerLogos.CHAUPAL
        PROVIDER_PLANET_MARATHI.lowercase(Locale.getDefault()) -> return providerLogos.PLANETMARATHI
        PROVIDER_NAMMAFLIX.lowercase(Locale.getDefault()) -> return providerLogos.NAMMAFLIX
        PROVIDER_TATA_SKY.lowercase(Locale.getDefault()) -> return providerLogos.TATASKY
        PROVIDER_GAMEZOP.lowercase(Locale.getDefault()) -> return providerLogos.GAMEZOP
        PROVIDER_LIONSGATE.lowercase(Locale.getDefault()) -> return providerLogos.LIONSGATE
        else -> {
            return null
        }
    }
}


fun checkWatchedReplay(totalDuration: Int, watchDuration: Int): Boolean {
    if (totalDuration > 0) {
        val x = totalDuration * 99 / 100
        if (watchDuration >= x)
            return true
    }
    return false
}

fun checkPartnerSubscription(
    selectedPartners: Set<String>?,
    contentItem: List<ContentItem>
): List<ContentItem> {
//    if(selectedPartners == null || selectedPartners.isEmpty()) return contentItem
//    val providers = selectedPartners.appList
    for (content in contentItem) {
        content.isSubscribed = selectedPartners != null && selectedPartners.contains(content.partnerId)
    }
    return contentItem
}

fun filterSubscribedUnsubscribedContents(
    selectedPartners: Set<String>?,
    contentItem: List<ContentItem>
): AppResponse {
    e("Utility", "filterSubscribedUnsubscribedContents : $selectedPartners")
    val appResponse = AppResponse()
    val subscribedContent: ArrayList<ContentItem> = ArrayList()
    val unsubscribedContent: ArrayList<ContentItem> = ArrayList()
    appResponse.subscribedContent = subscribedContent
    appResponse.unsubscribedContent = unsubscribedContent
    if(selectedPartners != null && selectedPartners.isNotEmpty()) {
        for (content in contentItem) {
            content.isSubscribed = false
//            for (partner in providers) {
            //if (content.provider.equals(partner.providerName, ignoreCase = true)) {
            e("Utility", "content.provider : ${content.partnerId}")
            if(selectedPartners.contains(content.partnerId)){
                content.isSubscribed = true
                subscribedContent.add(content)
            }
//            }
            if (!content.isSubscribed) {
                unsubscribedContent.add(content)
            }
        }
    }
    else{
        unsubscribedContent.addAll(contentItem)
    }
    return appResponse
}


fun checkSubscription(selectedPartners: Set<String>?): Boolean {
    e("Utility", "checkSubscription : $selectedPartners")
    return selectedPartners != null && selectedPartners.isNotEmpty()
}
fun removeSpecialChar(searchQuery: String): String {
    val re = Regex("[^A-Za-z0-9 ]")
    val query = re.replace(searchQuery, "")
    return query
}

fun isValidEmail(target: CharSequence?): Boolean {
    return if (target == null) {
        false
    } else {
        android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}

fun convertPackDate(dateStr: String, currentPattern: String, required: String) : String{
    try {
        val simpleDateFormat = SimpleDateFormat(currentPattern, Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getDefault()
        val date = simpleDateFormat.parse(dateStr)
        val sdf = SimpleDateFormat(required, Locale.getDefault())
        return sdf.format(date)
    }catch (e: Exception){
        //e.printStackTrace()
    }
    return dateStr
}


fun checkDateExpired(dateStr: String, currentPattern: String) : Boolean{
    try {
        val simpleDateFormat = SimpleDateFormat(currentPattern, Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getDefault()
        val date = simpleDateFormat.parse(dateStr)
        date.hours = 23
        date.minutes = 59
        date.seconds = 59
        if(date.before(Date())) return true
        else return date == Date()

    }catch (e: Exception){
        e.printStackTrace()
    }
    return false
}

//Before using this intent Please make sure the user is login else open login sheet
fun getGamesActivityIntent(
    context: Context?,
    contentItem: ContentItem,
    gamesMixpanelInfoModel: GamesMixpanelInfoModel? = null
): Intent? {
    if(!NetworkUtil.checkInternetBeforeNavigate()) {
        showToast(context,context?.getString(R.string.network_title)?:"", R.drawable.ic_internet_small)
        return null
    } else{
        return Intent(context, GamePlayerActivity::class.java).apply {
            putExtra("contentItem", contentItem)
            putExtra("gamesMixpanelInfoModel", gamesMixpanelInfoModel)
        }
    }
}

fun getSubscriptionActivityIntent(
    context: Context?,
    fromLogin: Boolean = false,
    selectedAppId: String? = null,
    fromScreen: String = SOURCE_NOTIFICATION,
    initiateRecharge: Boolean = false,
    fromDialog: Boolean = false,
    isFromNudge : Boolean = false,
    startPackListing : Boolean = false,
    startComparePlan : Boolean = false,
    selectedPackId : String? = null,
    partnerId : String? = null,
    packId: String? = null,
    packName: String? = null,
    journeySource: String? = null,
    journeySourceRefId: String? = null
):Intent{
    return Intent(context, FreemiumSubscriptionActivity::class.java).apply {
        putExtra("fromLogin", fromLogin)
        putExtra("selectedAppId", selectedAppId)
        putExtra("fromScreen", fromScreen)
        putExtra("initiateRecharge", initiateRecharge)
        putExtra("fromDialog", fromDialog)
        putExtra("isFromNudge", isFromNudge)
        putExtra("startPackListing",startPackListing)
        putExtra("startComparePlan",startComparePlan)
        putExtra("partnerId",partnerId)
        putExtra("packId",packId)
        putExtra("packName", packName)
        putExtra(ACTION, journeySource)
        putExtra(KEY_MANAGED_APP_JOURNEY_SOURCE_REFID, journeySourceRefId)
    }
}


fun getPaymentActivityIntent(
    context: Context?,
    packId: String?,
    selectedTenureId: String?,
    selectedTenureAmount: String?,
    isMigrated: Boolean?,
    migratedVerbiage: String?,
    proratedAmount: String?,
    fromScreen: String? = SOURCE_DEEPLINK,
    sharedPrefs: PrefsRepo,
    newUserDelay : Boolean? = null,
    cartId: String? = null
): Intent {
    if (!NON_DTH_USER.equals(
            sharedPrefs.getDthStatusFreemium(),
            true
        ) && !sharedPrefs.getSubscriptionType().equals(
            subscriptionTypeFreemium, true
        )
    ) {
        //TODO: TICK-TICK, Need to confirm the journey and balance, prorata amount etc for UI of Old stack users
        return Intent(context, WalletPaymentActivity::class.java).apply {
            putExtra("packID", packId)
            putExtra("selectedTenureID", selectedTenureId)
            putExtra("selectedTenureAmount", selectedTenureAmount)
            putExtra("isMigrated", isMigrated)
            putExtra("migratedVerbiage", migratedVerbiage)
            putExtra("proratedAmount", proratedAmount)
            putExtra(KEY_FROM_SCREEN, fromScreen) /*Source for tracking*/
            putExtra("newUserDelay",newUserDelay)
            putExtra(CART_ID, cartId)
        }
    } else {
        return Intent(context, PaymentJourneyActivity::class.java).apply {
            putExtra("packID", packId)
            putExtra("selectedTenureID", selectedTenureId)
            putExtra("selectedTenureAmount", selectedTenureAmount)
            putExtra("isMigrated", isMigrated)
            putExtra("migratedVerbiage", migratedVerbiage)
            putExtra("proratedAmount", proratedAmount)
            putExtra(KEY_FROM_SCREEN, fromScreen) /*Source for tracking*/
            putExtra("newUserDelay",newUserDelay)
            putExtra(CART_ID, cartId)
        }
    }
}

fun getNormalThumbnailForGenreDimension(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = NUMBER_OF_NORMAL_COLUMN_GENRE
    if (isTablet(activity)) {
        columnNumber = NUMBER_OF_NORMAL_COLUMN_TABLET
    }
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_NORMAL).toInt()
//    e("UtilsKt","point x: "+point.x)
//    e("UtilsKt","point y: "+point.y)
    return point
}


fun getNormalThumbnailForCategoryDimension(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = NUMBER_OF_NORMAL_COLUMN_CATEGORY
    if (isTablet(activity)) {
        columnNumber = NUMBER_OF_NORMAL_COLUMN_TABLET
    }
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_NORMAL).toInt()
//    e("UtilsKt","point x: "+point.x)
//    e("UtilsKt","point y: "+point.y)
    return point
}


fun checkShowsAndUpdate(it: ContentItem): Boolean {
    if(it.seriesvrId.isNullOrBlank()) return true
    else{
        it.id = it.seriesvrId?: it.id
        it.title = it.seriesTitle ?: it.title
        it.contentType = it.seriescontentType ?: it.contentType
        it.image = it.seriesimage ?: it.image
    }
    return true
}

const val DATE_TIME_FORMATE = "dd/MM/yyyy HH:mm"
@SuppressLint("SimpleDateFormat")
fun compareHrsWithOldTime(compareDate: String?, hrs: Int): Boolean {
    try {
        val fomator = SimpleDateFormat(DATE_TIME_FORMATE)
        val calendar = Calendar.getInstance()
        val currentDateTime = fomator.format(calendar.time)
        val currentDate = fomator.parse(currentDateTime)
        val oldDate = fomator.parse(compareDate)
        val mills = currentDate.time - oldDate.time
        val hours = (mills / (1000 * 60 * 60)).toInt()
        val minuets = (mills / (1000 * 60)).toInt() % 60
        e("200 Date Time======hours== ", "$hours : mins$minuets")
        return hours >= hrs
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}
fun getCurrentDate(): String {
    return try {
        val sdf = SimpleDateFormat(DATE_TIME_FORMATE)
        val netDate = Date()
        sdf.format(netDate)
    } catch (ex: java.lang.Exception) {
        "xx"
    }
}

fun openInAppBrowserActivityWithoutCustomTab(context: Context, uri: Uri){
    val rechargeIntent = Intent(context, InAppBrowserActivity::class.java)
    rechargeIntent.data = uri
    rechargeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    context.startActivity(rechargeIntent)
}

fun openInAppBrowserActivity(activity: Activity, uri: Uri){
    val CHROME_PACKAGE_NAME = "com.android.chrome"
    val packageName = CustomTabsClient.getPackageName(activity, listOf(uri.toString()))
    if (packageName == null) {
        val rechargeIntent = Intent(activity, InAppBrowserActivity::class.java)
        rechargeIntent.data = uri
        rechargeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        activity.startActivityForResult(rechargeIntent, 1011)
    } else {
        val builder = CustomTabsIntent.Builder()
        builder.setShowTitle(false)
        builder.setCloseButtonIcon(
            BitmapFactory.decodeResource(
                activity.resources,
                R.drawable.ic_back
            )
        )
        builder.setUrlBarHidingEnabled(true)
        builder.setDefaultColorSchemeParams(
            CustomTabColorSchemeParams.Builder().setToolbarColor(
                ContextCompat.getColor(
                    activity,
                    R.color.darkBackground
                )
            ).build()
        )
        val customTabsIntent = builder.build()
        if (packageName != CHROME_PACKAGE_NAME && getCustomTabsPackages(activity)?.find { it.activityInfo.packageName == CHROME_PACKAGE_NAME } != null)
            customTabsIntent.intent.setPackage(CHROME_PACKAGE_NAME)
        customTabsIntent.launchUrl(activity, uri)
    }
}
private fun getCustomTabsPackages(activity: Activity): ArrayList<ResolveInfo>? {
    val pm: PackageManager = activity.packageManager
    // Get default VIEW intent handler.
    val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))

    // Get all apps that can handle VIEW intents.
    val resolvedActivityList: List<ResolveInfo> = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        pm.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL)
    } else {
        pm.queryIntentActivities(activityIntent, 0)
    }
    val packagesSupportingCustomTabs = ArrayList<ResolveInfo>()
    for (info in resolvedActivityList) {
        val serviceIntent = Intent()
        serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION)
        serviceIntent.setPackage(info.activityInfo.packageName)
        // Check if this package also resolves the Custom Tabs service.
        if (pm.resolveService(serviceIntent, 0) != null) {
            packagesSupportingCustomTabs.add(info)
        }
    }
    return packagesSupportingCustomTabs
}

fun PackageManager.isPackageInstalled(packageName: String): Boolean {
    return try {
        getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}


fun taAndEditorialMergeData(vrContent: ArrayList<ContentItem>,
                           taContent: List<ContentItem>) : ArrayList<ContentItem> {
    val arrUniqeItem = ArrayList<ContentItem>()
    for (i in taContent.indices) {
        var id1: String
        var contentType1: String
        var isDuplicate = false
        var contentType2: String = taContent[i].contentType
        var id2: String = taContent[i].id
        for (k in vrContent.indices) {
            id1 = vrContent[k].id
            contentType1 = vrContent[k].contentType
            if (id1 == id2 && contentType1 == contentType2) {
                isDuplicate = true
                break
            }
        }
        if (!isDuplicate) {
            arrUniqeItem.add(taContent[i])
        }
        if (taContent.size == arrUniqeItem.size)
            return ArrayList(arrUniqeItem.map {
                it.apply {
                    it.contentConfigType = RECOMMENDATION
                }
            })
    }
    return ArrayList(arrUniqeItem.map { it.apply { it.contentConfigType = RECOMMENDATION } })
}

fun removeDuplicateContent(vrContent: ArrayList<ContentItem>,
                           taContent: List<ContentItem>) : ArrayList<ContentItem> {
    val arrUniqeItem = ArrayList<ContentItem>()
    for (i in taContent.indices) {
        var id1: String
        var contentType1: String
        var id2: String
        var contentType2: String
        var isDuplicate = false
        contentType2 = taContent[i].contentType
    id2 = taContent[i].id
        for (k in vrContent.indices) {
            id1 = vrContent[k].id
            contentType1 = vrContent[k].contentType
            if (id1 == id2 && contentType1 == contentType2) {
                isDuplicate = true
                break
            }
        }
        if (!isDuplicate) {
            arrUniqeItem.add(taContent[i])
        }
        if (taContent.size == arrUniqeItem.size)
            return ArrayList(arrUniqeItem.map {
                it.apply {
                    it.contentConfigType = RECOMMENDATION
                }
            })
    }
    return ArrayList(arrUniqeItem.map { it.apply { it.contentConfigType = RECOMMENDATION } })
}

fun isGood(videoBitRateFormat: Format) = videoBitRateFormat.bitrate > 0 && (videoBitRateFormat.height >= 437 && videoBitRateFormat.width >= 768) || (videoBitRateFormat.bitrate / 1000 > 800)
fun isBetter(videoBitRateFormat: Format) = videoBitRateFormat.bitrate > 0 && (videoBitRateFormat.height >= 1024 && videoBitRateFormat.width >= 576) || (videoBitRateFormat.bitrate / 1000 > 1200)
fun isBest(videoBitRateFormat: Format) = videoBitRateFormat.bitrate > 0 && (videoBitRateFormat.height >= 1080 && videoBitRateFormat.width >= 1920) || (videoBitRateFormat.bitrate / 1000 > 4000)

enum class TimeLevel(val value: Int) {
    DAY(3), HOUR(2), MINUTE(1), SECOND(0);

}
fun getExpiryTime(
    millis: Long,
    max: TimeLevel,
    min: TimeLevel
): String {
    var millis = millis
    millis -= System.currentTimeMillis()
    return if ((millis / HOUR_LIMIT > 0 || min == TimeLevel.DAY) && max == TimeLevel.DAY) {
        val value: Long = if (millis < 1) 0 else calculateTime(
            millis,
            DAY
        )
        value.toString() + if (value != 1L) " days" else " day"
    } else if ((millis / MINUTE_LIMIT > 0 || min == TimeLevel.HOUR) && max.value >= TimeLevel.HOUR.value) {
        val value: Long = if (millis < 1) 0 else calculateTime(
            millis,
            HOUR
        )
        value.toString() + if (value != 1L) " hours" else " hour"
    } else if ((millis / SECOND_LIMIT > 0 || min == TimeLevel.MINUTE) && max.value >= TimeLevel.MINUTE.value) {
        val value: Long = if (millis < 1) 0 else calculateTime(
            millis,
            MINUTE
        )
        value.toString() + if (value != 1L) " minutes" else " minute"
    } else {
        val value: Long = if (millis < 1) 0 else calculateTime(
            millis,
            SECOND
        )
        value.toString() + if (value != 1L) " seconds" else " second"
    }
}
private fun calculateTime(millis: Long, timeUnit: Long): Long {
    return if (millis % timeUnit > 0) millis / timeUnit + 1 else millis / timeUnit
}

fun getDisplayMatics(): DisplayMetrics {
    return Resources.getSystem().displayMetrics
}


fun getProviderInfo(provider: String, providerInfo: ProviderInfo) : PartnerUniqueInfo?{
    when (provider.lowercase(Locale.getDefault())) {
        PROVIDER_HUNGAMA.lowercase(Locale.getDefault()) -> return providerInfo.HUNGAMA

        PROVIDER_SUN_NEXT.lowercase(Locale.getDefault()) -> return providerInfo.SUNNXT

        PROVIDER_HOTSTAR.lowercase(Locale.getDefault()) -> return providerInfo.HOTSTAR

        PROVIDER_EROSNOW.lowercase(Locale.getDefault()) -> return providerInfo.EROSNOW

        PROVIDER_ZEE5.lowercase(Locale.getDefault()) -> return providerInfo.ZEE5

        PROVIDER_PRIME.lowercase(Locale.getDefault()) -> return providerInfo.PRIME

        PROVIDER_SHEMAROO.lowercase(Locale.getDefault()) -> return providerInfo.SHEMAROOME

        PROVIDER_VOOTSELECT.lowercase(Locale.getDefault()) -> return providerInfo.VOOTSELECT

        PROVIDER_VOOTKIDS.lowercase(Locale.getDefault()) -> return providerInfo.VOOTKIDS

        PROVIDER_CURIOSITY_STREAM.lowercase(Locale.getDefault()) -> return providerInfo.CuriosityStream

        PROVIDER_SONYLIV.lowercase(Locale.getDefault()) -> return providerInfo.SONYLIV
        PROVIDER_EPIC_ON.lowercase(Locale.getDefault()) -> return providerInfo.Epicon
        PROVIDER_LIONSGATE.lowercase(Locale.getDefault()) -> return providerInfo.Lionsgate
//        PROVIDER_DOCU_BAY.lowercase(Locale.getDefault()) -> return providerInfo.DOCU_BAY
        PROVIDER_TATA_SKY.lowercase(Locale.getDefault()) -> return providerInfo.TATASKY
        else -> {
            return null
        }
    }
}

fun isHideSelectPackButton(currentPack: PartnerPacks?): Boolean {
    return currentPack?.isFDRRaised == true ||
            currentPack?.subscriptionDetailInfo?.bingeAccountStatus.equals(SubscriptionPackStatusEnum.WRITTEN_OFF.status, true) ||
            currentPack?.subscriptionDetailInfo?.bingeAccountStatus.equals(SubscriptionPackStatusEnum.DEACTIVE.status, true)
}

fun getDateObject(date : String, currentFormat : String) : Date? {
    return try {
        SimpleDateFormat(currentFormat, Locale.getDefault()).parse(date)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getUtcDateObject(date : String, currentFormat : String) : Date? {
    var newdate: Date?=null
    try {
        val sdf= SimpleDateFormat(currentFormat, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
//        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val utcDate = sdf.parse(date)
//         newdate = sdf.parse(date)

        val calendar = Calendar.getInstance()
        calendar.setTime(utcDate)
        calendar.add(Calendar.HOUR, 12)
        newdate=calendar.time

    } catch (e: Exception) {
        e.printStackTrace()
        newdate= null
    }
    return newdate
}

fun getUtcDateWithOneSecObject(date : String, currentFormat : String) : Date? {
    var newdate: Date?=null
   try {
       val sdf= SimpleDateFormat(currentFormat, Locale.getDefault())
//       sdf.timeZone = TimeZone.getTimeZone("UTC")
      val date1 = sdf.parse(date)

       val calendar = Calendar.getInstance()
       calendar.setTime(date1)
       calendar.add(Calendar.SECOND, 1)
       newdate=calendar.time
    } catch (e: Exception) {
        e.printStackTrace()
       newdate= null
    }
    return newdate
}

fun getLanguageWidgetWidth(context: Context?): Point {
    val point = getDeviceDimension(context)
    val columnNumber = 2.32

    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_LARGE_GRID).toInt()
    e("UtilsKt","point x: "+point.x)
//    e("UtilsKt","point y: "+point.y)
    return point
}

fun String.maskPhoneNumber(): String {
    val bullet = "\u2022\u2022\u2022\u2022\u2022"
    return this.replaceRange(0, 5, bullet)
}

fun String.maskLast5PhoneNumberDigits(): String {
    val bullet = "xxxxx"
    if (this.length == 10)
        return this.replaceRange(5, 10, bullet)
    return ""
}

// Adds single space (NBSP) around the masked bullets (Left and Right side)
fun String.maskPhoneNumberWithSingleSpace(): String {
    val bullet = "\u00A0\u2022\u2022\u2022\u00A0"
    return this.replaceRange(4, 7, bullet)
}
fun String.maskPhoneNumberWithx(): String {
    val bullet = "xxxxx"
    return this.replaceRange(0, 5, bullet)
}

fun getPortraitMixedThumbnailDimension(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = 4.8
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_LARGE).toInt()
    e("UtilsKt","PortraitMixed point x: "+point.x)
    e("UtilsKt","PortraitMixed point y: "+point.y)
    return point
}


fun getPortraitThumbnailDimensionGrid(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = 3.55
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_LARGE_GRID).toInt()
    e("UtilsKt","portrait point x: "+point.x)
    e("UtilsKt","portrait point y: "+point.y)
    return point
}

fun getRoundAppGridSizeMyPlan(view: View?, spanCount : Float) : Point{
    if (view == null) {
        return Point()
    }
    val point = getViewPoints(view)
    point.x = (point.x / spanCount - (spanCount + 1)).toInt()
    point.y = (point.x).toInt()
    return point
}

fun getViewPoints(view: View): Point {
    return Point(view.width, view.height)
}

fun getGridManagerLastRowCenter(ctx: Context, spanCount: Int, itemsCount: Int,
                                @RecyclerView.Orientation orientation :Int, reverseLayout: Boolean)
        : GridLayoutManager {

    // get number of items in last row
    val lastRowCount = itemsCount % spanCount
    if (lastRowCount == 0) {
        return GridLayoutManager(ctx, spanCount, orientation, reverseLayout)
    }

    // number of rows with all items
    val fullRows = itemsCount / spanCount
    // "span" counter for whole row (as minimum divider)
    val rowSpan = spanCount * lastRowCount
    // span value for item in the first rows
    val baseRowItemSpan = rowSpan / spanCount
    // span value for item in the last row
    val lastRowItemSpan = rowSpan / lastRowCount

    // return generated manager
    return GridLayoutManager(ctx, rowSpan, orientation, reverseLayout).apply {
        spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {

            override fun getSpanSize(position: Int): Int {
                return if (position / spanCount < fullRows) {
                    baseRowItemSpan
                } else {
                    lastRowItemSpan
                }
            }
        }
    }
}

fun expandBottomSheet(dialog : Dialog?){
    dialog?.setOnShowListener {
        val d = dialog as BottomSheetDialog
        val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
        bottomSheet?.let{
            val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior.skipCollapsed = true
        }
    }
}

fun paintPremiumGradient(tv: TextView,width:Float){
    val paint = tv.paint
    val textShader: Shader = LinearGradient(
        0f, 0f, width, tv.textSize, intArrayOf(
            Color.parseColor("#FFA800"),
            Color.parseColor("#FFF389"),
            Color.parseColor("#FFA800")
        ), null, Shader.TileMode.REPEAT
    )
    tv.paint.shader = textShader
}


fun getCircularProviderIconPoint(activity: Context): Point {
    val point = getDeviceDimension(activity)
    val columnNumber = 5.5
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x * THUMBNAIL_RATIO_APP_NORMAL).toInt()
    return point
}

fun isShowCrownOnContent(isPartnerSubscribed : Boolean, isGuestUser:Boolean,
                         provider: String?, partnerSubscriptionType : String?): Boolean {
    var partnerSubscriptionType = partnerSubscriptionType
    if(provider.equals(PROVIDER_GAMEZOP,true) || provider.equals(PROVIDER_TATA_SKY,true))
        return false
    if(partnerSubscriptionType == null) partnerSubscriptionType = PREMIUM
    if (!isPartnerSubscribed
        && (PROVIDER_PRIME.equals(provider, true)
                || PREMIUM.equals(partnerSubscriptionType,
            true))
    )
        return true
    return false
}

fun changeDaysToMillis(days: Int?): Long {
    return (days?:0) * 24 * 60 * 60 * 1000L
}

abstract class SingleClickListener(private val intervalMillis: Long = THRESHOLD_MILLIS) : View.OnClickListener {
    private var lastClickMillis: Long = 0
    override fun onClick(v: View) {
        val now: Long = SystemClock.elapsedRealtime()
        if (now - lastClickMillis > intervalMillis) {
            onClicked(v)
        }
        lastClickMillis = now
    }

    abstract fun onClicked(v: View?)

    companion object {
        private const val THRESHOLD_MILLIS = 400L
    }
}

fun handleOTPKey(clEtOtpContainer: LayoutOtpViewWithoutHintBinding,p0: View?, keyCode: Int, keyEvent: KeyEvent?) {
    if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent?.action == KeyEvent.ACTION_UP)
        clEtOtpContainer.apply {
            when(p0) {
                etOtpDig1 -> clearFieldAndFocus(etOtpDig1)
                etOtpDig2 -> {
                    if (etOtpDig2.text.isNotEmpty())
                        clearFieldAndFocus(etOtpDig2)
                    else
                        clearFieldAndFocus(etOtpDig1)
                }
                etOtpDig3 -> {
                    if (etOtpDig3.text.isNotEmpty())
                        clearFieldAndFocus(etOtpDig3)
                    else
                        clearFieldAndFocus(etOtpDig2)
                }
                etOtpDig4 -> {
                    if (etOtpDig4.text.isNotEmpty())
                        clearFieldAndFocus(etOtpDig4)
                    else
                        clearFieldAndFocus(etOtpDig3)
                }
                etOtpDig5 -> {
                    if (etOtpDig5.text.isNotEmpty())
                        clearFieldAndFocus(etOtpDig5)
                    else
                        clearFieldAndFocus(etOtpDig4)
                }
                etOtpDig6 -> {
                    if (etOtpDig6.text.isNotEmpty())
                        clearFieldAndFocus(etOtpDig6)
                    else
                        clearFieldAndFocus(etOtpDig5)
                }
            }
        }
}

//Handling for deleting OTP and regaining the focus after deletion
private fun clearFieldAndFocus(etOtpDig: EditText) {
    etOtpDig.text.clear()
    etOtpDig.requestFocus()
}

fun isValidContentForTickTick(
    filteredContentItems: List<ContentItem>,
    partnerIdsList: Set<String>) : List<ContentItem>{
    val list  = ArrayList<ContentItem>()
    for(content in filteredContentItems){
        if(!partnerIdsList.contains(content.provider.lowercase()))
            list.add(content)

    }
    return list/*filteredContentItems.filter {
        partnerIdsList.contains(it.provider)
    }*/
}

fun getSquareGameThumbnailDimension(activity: Context): Point {
    val point = getDeviceDimension(activity)
    var columnNumber = 3.0F
    if (isTablet(activity)) {
        columnNumber = NUMBER_OF_PORTRAIT_COLUMN_TABLET
    }
    point.x = (point.x / columnNumber - (columnNumber + 1)).toInt()
    point.y = (point.x)
    e("UtilsKt","GameSquare point x: "+point.x)
    return point
}


fun vibratePhone(context : Context, timeInMs : Long) {

    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (vibrator.hasVibrator()) { // Vibrator availability checking
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(timeInMs, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
        }else {
            vibrator.vibrate(timeInMs) // Vibrate method for below API Level 26
        }
    }

}

/*
   Get Days Count from Last vibrate
* */
fun getDifference(startDate: Long, endDate: Long): Long {

    var different = endDate- startDate
    val secondsInMilli: Long = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60
    val daysInMilli = hoursInMilli * 24
    val elapsedDays = different / daysInMilli
    different = different % daysInMilli
    val elapsedHours = different / hoursInMilli
    different = different % hoursInMilli
    val elapsedMinutes = different / minutesInMilli
    different = different % minutesInMilli
    val elapsedSeconds = different / secondsInMilli
    System.out.printf(
        "%d days, %d hours, %d minutes, %d seconds%n",
        elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds
    )

    return elapsedDays
}

fun setPackageNameFromResolveInfoList(context: Context, launchIntent: Intent) {
    val resolveInfoList = context.packageManager.queryIntentActivities(launchIntent, 0)
    if (resolveInfoList != null) {
        val appPackageName = context.packageName
        for (resolveInfo in resolveInfoList) {
            if (appPackageName == resolveInfo.activityInfo.packageName) {
                launchIntent.setPackage(appPackageName)
                break
            }
        }
    }
}

var hashMap: HashMap<String, Typeface> = HashMap()
fun getMoreTypeFace(context: Context, key: String): Typeface {
    return when {
        hashMap.containsKey(key) -> {
            hashMap.getValue(key)
        }
        else -> {
            hashMap[key] = Typeface.createFromAsset(context.assets, "fonts/VoltePlay-Regular.otf")
            hashMap.getValue(key)
        }
    }
}

fun mapToString(map: Map<String, Any>): String {
    val stringBuilder = StringBuilder()
    for (key in map.keys) {
        if (stringBuilder.isNotEmpty()) {
            stringBuilder.append("&")
        }
        val value = map[key]
        try {
            stringBuilder.append(if (key != null) URLEncoder.encode(key, "UTF-8") else "")
            stringBuilder.append("=")
            stringBuilder.append(if (value != null) URLEncoder.encode("$value", "UTF-8") else "")
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("This method requires UTF-8 encoding support", e)
        }
    }
    return stringBuilder.toString()
}

fun moveToManagedApp(
    sharedPrefs: PrefsRepo,
    activity: FragmentActivity?,
    partnerId: String,
    context: Context?,
    checkFdo: Boolean=false
) {
    val currentSubscription = sharedPrefs.getSubscribedPack()
    if (currentSubscription == null || !sharedPrefs.getLoginStatus())
        if (sharedPrefs.getConfigResponse()?.data?.config?.enableTickTickJourney == false)
        //Added journeyRef so that directly open managed app by skipping drawer
        {
            (activity as? LandingActivity)?.showMiniDrawer(
                journeyRef = HOME_CONTENT,
                journeyRefKey = partnerId,
                skipDrawer = true
            )

        } else {
            (activity as? LandingActivity)?.showMiniDrawer(
                journeyRefKey = partnerId
            )

        }
    else {

        if (checkFdo && currentSubscription?.fdoRequested == true) {
            activity?.startActivity(
                getSubscriptionActivityIntent(
                    context = context,
                    startPackListing = false
                )
            )
        } else
            (activity as? LandingActivity)?.showMiniDrawer(
                journeyRef = HOME_CONTENT,
                journeyRefKey = partnerId,
                skipDrawer = true
            )
    }
}


fun setStringWithDifferentFont(firstString:String, fontSize:Int):String{
    Log.d("TAG", "setStringWithDifferentFont: $firstString")
     var span1=SpannableString(firstString)
     span1.setSpan(AbsoluteSizeSpan(fontSize),1,5,Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    return span1.toString()

}

fun setSectionOfTextSize(
    text: String,
    textToChangeSize: String,
    size: Int,
    isBold: Boolean
): SpannableStringBuilder? {
    val builder = SpannableStringBuilder()
    if (textToChangeSize.length > 0 && textToChangeSize.trim { it <= ' ' } != "") {

        //for counting start/end indexes
        val testText = text.lowercase(Locale.US)
        val testTextToBold = textToChangeSize.lowercase(Locale.US)
        val startingIndex = testText.indexOf(testTextToBold)
        val endingIndex = startingIndex + testTextToBold.length
        //for counting start/end indexes
        if (startingIndex < 0 || endingIndex < 0) {
            return builder.append(text)
        } else if (startingIndex >= 0 && endingIndex >= 0) {
            builder.append(text)
            if(size>0)
            builder.setSpan(
                AbsoluteSizeSpan(size),
                startingIndex,
                endingIndex,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            if(isBold)
                builder.setSpan(StyleSpan(Typeface.BOLD), startingIndex, endingIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    } else {
        return builder.append(text)
    }
    return builder
}

fun spToPx(iContext: Context, sp: Int): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), iContext.resources.displayMetrics)
        .toInt()
}

fun isHideRailWithPackName(
    item: HomeResponse.Items?,
    subscribedPack: PartnerPacks?,
    packName: String?,
    mNonSubscribedPartnerList: HashSet<String>,
    loginStatus: Boolean
): Boolean {
    e("isHideRailWithPackName","PackName: ${packName}" +
            ",productName: ${subscribedPack?.productName}," +
            ",subscriptionStatus : ${subscribedPack?.subscriptionStatus}")
    val productName = subscribedPack?.productName?:"hfjkd"
    if(loginStatus &&
        (SubscriptionPackStatusEnum.ACTIVE.status.equals(
            subscribedPack?.subscriptionStatus,
            true)) &&
        subscribedPack?.flexiPlan == true &&
        packName?.contains(productName, true) == true)
    {
        //filter content on the basis of subscribed partner list
        //list - zee5, sony, hotstar, voot( keep these partner's content)
        //packName = mega - all partner
        // other partner content should be removed from the list
        item?.contentItem =
            item?.filteredContentItems?.let { isValidContentForTickTick(it, mNonSubscribedPartnerList) } as ArrayList<ContentItem>
        return true
    }
    return false
}

