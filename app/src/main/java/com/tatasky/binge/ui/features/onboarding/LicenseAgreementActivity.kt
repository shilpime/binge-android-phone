package com.tatasky.binge.ui.features.onboarding

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
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
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.utils.navigateUpOrOpenHome
import kotlinx.android.synthetic.main.activity_license_agreement.*
import javax.inject.Inject

class LicenseAgreementActivity : BaseActivity<CommonSampleViewModel>() {

    override fun getContentViewId(): Int = R.layout.activity_license_agreement

    override fun getRootLayoutContainer(): View = root_container

    override fun init(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbar_layout.findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        navController.graph = navController.navInflater.inflate(R.navigation.nav_license_agreement)
        val appBarConfiguration = AppBarConfiguration(emptySet(), null) { onSupportNavigateUp() }
        findViewById<Toolbar>(R.id.toolbar)?.setupWithNavController(navController, appBarConfiguration)
    }

    override fun getViewModelClass(): Class<CommonSampleViewModel> = CommonSampleViewModel::class.java

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragment_container).navigateUpOrOpenHome(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        loginAnalytics.trackLoginLicAgreementBack()
    }

    override fun onBackPressed() {
        findNavController(R.id.fragment_container).navigateUpOrOpenHome(this)
    }

}