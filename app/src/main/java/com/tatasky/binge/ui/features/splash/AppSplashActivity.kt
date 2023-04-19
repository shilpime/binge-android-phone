package com.tatasky.binge.ui.features.splash

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.tatasky.binge.R
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.utils.d
import com.tatasky.binge.utils.isTablet
import kotlinx.android.synthetic.main.activity_app_splash.*


class AppSplashActivity : BaseActivity<BaseViewModel>() {

    override fun getContentViewId(): Int = R.layout.activity_app_splash

    override fun getRootLayoutContainer(): View = splash_container

    override fun init(savedInstanceState: Bundle?) {
//        window.statusBarColor = Color.WHITE
        if(!isTablet(this))
            requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
//                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            // Log and toast
            val token = task.result
            d(this.localClassName, token)
        })


    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun getViewModelClass(): Class<BaseViewModel> = BaseViewModel::class.java

}
