package com.tatasky.binge.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.tatasky.binge.analytics.SOURCE_LOGOUT
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.ui.base.MyApp
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.onboarding.marketing.MarketingActivity

const val   PRIMARY = "PRIMARY"

fun setSelectedAccountDetail(selectedProfile: LoginResponse.BingeSubscription,sharedPrefs: PrefsRepo) {
    selectedProfile?.let {
        sharedPrefs.clearSwitchAccountInfo()
        sharedPrefs.setSelectedProfile(it)
        sharedPrefs.setBaId(
            it.baId ?: ""
        )
        sharedPrefs.setProfileId(
            it.profileId ?: ""
        )
    }
}

fun getPrimaryBaID(profileList: List<LoginResponse.BingeSubscription>):String {
    val model= profileList.find {it.accountType.equals( PRIMARY)}
    return model?.baId!!
}

fun logoutApplication(context: Context){
    ((context as Activity).application as MyApp).clearAllData()
    context.startActivity(Intent(context, LandingActivity::class.java).apply {
//        this.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        this.putExtra(KEY_FROM_SCREEN, SOURCE_LOGOUT)
    })
    context.finishAffinity()
}

fun logoutMarketingApplication(context: Context){
    ((context as Activity).application as MyApp).clearAllData()
    context.startActivity(Intent(context, MarketingActivity::class.java).apply {
        this.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    })
    (context as Activity).finishAffinity()
}

fun startHomeScreen(activity:Activity?,
                    passURI : Boolean = false,
                    isCancelledUser:Boolean = false,
                    checkPaymentStatus:Boolean = false,
                    bundle: Bundle? = null,
                    clearTop:Boolean=true,
                    silentLogin : Boolean = false){
    activity?.let { callingActivity ->
        val intent = Intent(callingActivity, LandingActivity::class.java)
        if(passURI){
            intent.data = callingActivity.intent.data
        }
        intent.putExtra("silentLogin", silentLogin)
        intent.putExtra("isCancelledUser",isCancelledUser)
        intent.putExtra("checkPaymentStatus",checkPaymentStatus)
        intent.putExtra("bundle",bundle)
        if(checkPaymentStatus || !clearTop)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        else
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        callingActivity.setResult(Activity.RESULT_CANCELED)
        callingActivity.startActivity(intent)
        callingActivity.finishAffinity()
    }
}
