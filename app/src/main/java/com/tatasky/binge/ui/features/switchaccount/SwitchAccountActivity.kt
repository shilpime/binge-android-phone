package com.tatasky.binge.ui.features.switchaccount

import android.app.Activity
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
import com.tatasky.binge.ui.features.prime.PrimeAnalytics
import com.tatasky.binge.utils.logoutApplication
import com.tatasky.binge.utils.navigateUpOrOpenHome
import kotlinx.android.synthetic.main.activity_switch_account.*
import javax.inject.Inject

class SwitchAccountActivity : BaseActivity<CommonSampleViewModel>() {
	companion object{
		val DEVICE_LIMIT_REQUEST_CODE = 1201
	}
	override fun getContentViewId(): Int = R.layout.activity_switch_account

	override fun getRootLayoutContainer(): View = root_container

	override fun getViewModelClass(): Class<CommonSampleViewModel> = CommonSampleViewModel::class.java


	override fun init(savedInstanceState: Bundle?) {
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)
		val navHostFragment =
			supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
		val navController = navHostFragment.navController
		val graphInflater = navController.navInflater
		val navGraph = graphInflater.inflate(R.navigation.nav_switch_account)
		if(intent.getBooleanExtra("isDeviceReviewOnMaxLimitReached", false)){
			navGraph.startDestination = R.id.deviceListFragment
		}
		navController.setGraph(navGraph, intent.extras)
		val appBarConfiguration = AppBarConfiguration(emptySet(), null) { onSupportNavigateUp() }
		findViewById<Toolbar>(R.id.toolbar)?.setupWithNavController(navController, appBarConfiguration)
	}

	override fun onSupportNavigateUp(): Boolean {
		setResult(Activity.RESULT_CANCELED)
		return findNavController(R.id.fragment_container).navigateUpOrOpenHome(this)
	}
}