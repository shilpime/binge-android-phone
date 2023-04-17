package com.tatasky.binge.ui.features.subscription_freemium

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.tatasky.binge.R
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.home.LandingActivity
import kotlinx.android.synthetic.main.activity_wallet_payment.*

class WalletPaymentActivity : BaseActivity<CommonSampleViewModel>() {
    private var navGraph: NavGraph? = null
    private var navController: NavController? = null

    override fun getContentViewId(): Int = R.layout.activity_wallet_payment

    override fun getRootLayoutContainer(): View = splash_container

    override fun getViewModelClass(): Class<CommonSampleViewModel> =
        CommonSampleViewModel::class.java

    override fun init(savedInstanceState: Bundle?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        val graphInflater = navHostFragment.navController.navInflater
        navGraph = graphInflater.inflate(R.navigation.nav_freemium_payment)

        navGraph?.startDestination = R.id.walletpaymentFragment
        navGraph?.let {
            navController?.graph = it
        }
    }


    override fun onBackPressed() {
        if (!findNavController(R.id.fragment_container).navigateUp())
            if (isTaskRoot) {
                startActivity(Intent(this, LandingActivity::class.java))
                // using finish() is optional, use it if you do not want to keep currentActivity in stack
                finish()
            } else {
                super.onBackPressed()
                overridePendingTransition(R.anim.slide_right_out, R.anim.slide_left_in)
            }
    }
}