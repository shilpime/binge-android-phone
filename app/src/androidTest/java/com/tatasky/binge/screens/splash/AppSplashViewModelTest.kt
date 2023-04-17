package com.tatasky.binge.screens.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.tatasky.binge.app.TestMyApp
import com.tatasky.binge.base.BaseViewModelTest
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.extensions.getOrAwaitValue
import com.tatasky.binge.ui.features.splash.SplashViewModel
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class AppSplashViewModelTest : BaseViewModelTest<SplashViewModel>() {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var mockUseCase: CommonUseCase

    @Inject
    lateinit var mockSharedPrefs: PrefsRepo


    @Before
    override fun setUp() {
        super.setUp()
        val app: TestMyApp =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestMyApp
        app.appComponent.inject(this)
        viewModel = SplashViewModel(mockUseCase, mockSharedPrefs)
    }

    @Test
    fun check_app_version() {
        mockNetworkResponseWithFileContent("config.json", 200)

        println(getMockWebServerUrl())
        val response = viewModel.commonUseCase.executeConfig()
            .test().assertNoErrors()
            .assertValue { configResponse -> configResponse.data != null }
            .assertValue {
                it.data?.app?.appUpgrade?.android?.recommendedVersion.equals(
                    "1.4.0",
                    true
                )
            }
    }

    @Test
    fun check_force_app_version() {
        mockNetworkResponseWithFileContent("config.json", 200)
        viewModel.fetchConfigResponse()
        assert(viewModel.configResponse.getOrAwaitValue()?.data?.app?.appUpgrade?.android?.forceUpgradeVersion.equals("1.4.0", true))
    }
}