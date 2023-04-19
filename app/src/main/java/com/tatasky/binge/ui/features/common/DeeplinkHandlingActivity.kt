package com.tatasky.binge.ui.features.common

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tatasky.binge.R
import com.tatasky.binge.ui.features.splash.AppSplashActivity

class DeeplinkHandlingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent()
        return
    }

    private fun handleIntent() {
        val i = Intent(this, AppSplashActivity::class.java)
        i.data = intent.data
        i.putExtras(intent)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}