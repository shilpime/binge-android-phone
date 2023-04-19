package com.tatasky.binge.ui.features.recharge

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.tatasky.binge.analytics.PARA_SOURCE
import com.tatasky.binge.ui.features.prime.view.PrimeActivationActivity


fun launchRechargeActivity(fragment: Fragment, uri: Uri?, sid: String? = null) {
    val rechargeIntent = Intent(fragment.context, RechargeActivity::class.java)
    rechargeIntent.data = uri
    rechargeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    rechargeIntent.putExtra(RechargeActivity.RECHARGE_SID, sid)
    fragment.startActivityForResult(rechargeIntent, RechargeActivity.RECHARGE_REQUEST_CODE)
}
fun launchRechargeActivity(activity: Activity, uri: Uri?, sid: String? = null) {
    val rechargeIntent = Intent(activity, RechargeActivity::class.java)
    rechargeIntent.data = uri
    rechargeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    rechargeIntent.putExtra(RechargeActivity.RECHARGE_SID, sid)
    activity.startActivityForResult(rechargeIntent, RechargeActivity.RECHARGE_REQUEST_CODE)
}

fun launchAmazonActivationActivity(fragment: Fragment, uri: Uri?, amazonPackType : String, source: String) {
    val rechargeIntent = Intent(fragment.context, PrimeActivationActivity::class.java)
    rechargeIntent.data = uri
    rechargeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    rechargeIntent.putExtra(PrimeActivationActivity.AMAZON_TYPE, amazonPackType)
    rechargeIntent.putExtra(PARA_SOURCE, source)
    fragment.startActivityForResult(
        rechargeIntent,
        PrimeActivationActivity.RECHARGE_REQUEST_CODE
    )
}