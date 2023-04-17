package com.tatasky.binge.ui.features.player

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.navigation.fragment.NavHostFragment
import com.tatasky.binge.R
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.utils.PROVIDER_HUNGAMA
import com.tatasky.binge.utils.e
import com.tatasky.binge.utils.logoutApplication
import kotlinx.android.synthetic.main.activity_player.*

class PlayerActivity : BaseActivity<CommonSampleViewModel>() {
    override fun getContentViewId(): Int = R.layout.activity_player

    override fun getRootLayoutContainer(): View = player_container

    override fun onStart() {
        super.onStart()
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window?.decorView?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        super.onCreate(savedInstanceState)
    }

    override fun init(savedInstanceState: Bundle?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.nav_player)
        if (intent.extras?.getParcelable<PlayerModel>("playerData") == null)
            if (viewModel.isLoggedIn()) {
                startActivity(Intent(this, LandingActivity::class.java))
                finish()
            } else {
                logoutApplication(this)
            }
        else {
            if (!PROVIDER_HUNGAMA.equals(
                    intent.extras?.getParcelable<PlayerModel>("playerData")?.getProvider(),
                    true
                )
            )
                navGraph.startDestination = R.id.playerFragment
            navController.setGraph(navGraph, intent.extras)
        }
    }

    override fun onDestroy() {
        viewModel.enableOrientation()
        super.onDestroy()
    }

    override fun getViewModelClass(): Class<CommonSampleViewModel> =
        CommonSampleViewModel::class.java

    var allowBackPress = true

    override fun onBackPressed() {
        if(allowBackPress)
            super.onBackPressed()
    }
}