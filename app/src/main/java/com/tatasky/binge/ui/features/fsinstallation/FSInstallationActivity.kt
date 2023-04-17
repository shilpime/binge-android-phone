package com.tatasky.binge.ui.features.fsinstallation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.tatasky.binge.R
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.utils.logoutApplication
import com.tatasky.binge.utils.navigateUpOrFinish
import kotlinx.android.synthetic.main.activity_fs_installation.*

class FSInstallationActivity : BaseActivity<CommonSampleViewModel>()  {

    override fun getContentViewId(): Int = R.layout.activity_fs_installation

    override fun getRootLayoutContainer(): View = splash_container

    override fun getViewModelClass(): Class<CommonSampleViewModel> =
        CommonSampleViewModel::class.java

    override fun init(savedInstanceState: Bundle?) {
        if (!viewModel.isLoggedIn()) {
            logoutApplication(this)
        }
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.nav_fs_journey)
        val setOfTopLevelDestinations = setOf(R.id.fragment_fs_installation)

        navController.setGraph(navGraph, intent.extras)

        val appBarConfiguration = AppBarConfiguration(
            setOfTopLevelDestinations,
            null
        ) { onSupportNavigateUp() }
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)
    }
    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragment_container).navigateUpOrFinish(this)
    }

    override fun onBackPressed() {
        if (!findNavController(R.id.fragment_container).navigateUp())
            if (isTaskRoot) {
                startActivity(Intent(this, LandingActivity::class.java))
                finish()
            } else {
                super.onBackPressed()
                overridePendingTransition(R.anim.slide_right_out, R.anim.slide_left_in)
            }
    }
}