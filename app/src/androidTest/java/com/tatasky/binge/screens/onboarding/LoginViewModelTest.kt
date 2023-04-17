package com.tatasky.binge.screens.onboarding

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.tatasky.binge.app.TestMyApp
import com.tatasky.binge.base.BaseViewModelTest
import com.tatasky.binge.data.networking.models.requests.LoginDTO
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.pubnub.PubnubHelper
import com.tatasky.binge.ui.base.frameworks.extensions.getOrAwaitValue
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.onboarding.login.LoginViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class LoginViewModelTest : BaseViewModelTest<LoginViewModel>() {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var mockUseCase: CommonUseCase

    @Inject
    lateinit var pubnubHelper: PubnubHelper

    @Inject
    lateinit var mockSharedPrefs: PrefsRepo

    @Inject
    lateinit var loginAnalytics: LoginAnalytics


    @Before
    override fun setUp() {
        super.setUp()
        val app: TestMyApp =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestMyApp
        app.appComponent.inject(this)
        viewModel = LoginViewModel(mockUseCase, mockSharedPrefs, pubnubHelper)
        viewModel.loginAnalytics = loginAnalytics
    }

    @Test
    fun testGenerateOTPInvalidData() {
        mockNetworkResponseWithFileContent("generate_otp_rmn.json", 200)
        viewModel.loginDTO = LoginDTO("8373991979")
        viewModel.generateOTP(false)
        assert(viewModel.progressListener.getOrAwaitValue())
        assert(viewModel.errorMessage.getOrAwaitValue().getContentIfNotHandled()?.statusCode==-1)
    }

    @Test
    fun testGenerateOTPWithRmn() {
        mockNetworkResponseWithFileContent("generate_otp_rmn.json", 200)
        viewModel.loginDTO = LoginDTO("8373991979",actualRMN = "8373991979")
        viewModel.generateOTP(false)
        assert(viewModel.progressListener.getOrAwaitValue())
        assert(viewModel.getOtpResponse().getOrAwaitValue().getContentIfNotHandled()?.code == 0)
        assert(!viewModel.progressListener.getOrAwaitValue())
    }

    @Test
    fun testGenerateOTPWithNonTsRmn() {
        mockNetworkResponseWithFileContent("generate_otp_non_ts_rmn.json", 200)
        viewModel.loginDTO = LoginDTO("8373991979",actualRMN = "8373991979")
        viewModel.generateOTP(false)
        assert(viewModel.progressListener.getOrAwaitValue())
        assert(viewModel.getOtpResponseError().getOrAwaitValue().getContentIfNotHandled()?.code == 20013)
        assert(!viewModel.progressListener.getOrAwaitValue())
    }

    @Test
    fun testGenerateOTPWithNonTsSID() {
        mockNetworkResponseWithFileContent("generate_otp_non_ts_sid.json", 200)
        viewModel.loginDTO = LoginDTO(sid = "3021565132")
        viewModel.generateOTP(false)
        assert(viewModel.progressListener.getOrAwaitValue())
        assert(viewModel.getOtpResponseError().getOrAwaitValue().getContentIfNotHandled()?.code == 20012)
        assert(!viewModel.progressListener.getOrAwaitValue())
    }

    @Test
    fun testGenerateOTPWithSID() {
        mockNetworkResponseWithFileContent("generate_otp_sid.json", 200)
        viewModel.loginDTO = LoginDTO(sid ="3001363575")
        viewModel.generateOTP(false)
        assert(viewModel.progressListener.getOrAwaitValue())
        assert(viewModel.getOtpResponse().getOrAwaitValue().getContentIfNotHandled()?.code == 0)
        assert(!viewModel.progressListener.getOrAwaitValue())
    }

    @Test
    fun testPasswordLoginWithSID() {
        mockNetworkResponseWithFileContent("generate_otp_sid.json", 200)
        viewModel.loginDTO = LoginDTO(sid ="3001363575")
        viewModel.generateOTP(true)
        assert(viewModel.progressListener.getOrAwaitValue())
        assert(viewModel.getOtpResponse().getOrAwaitValue().getContentIfNotHandled()?.code == 0)
        assert(!viewModel.progressListener.getOrAwaitValue())
    }

    @Test
    fun testLoginWithSIDAndOTP() {
        mockNetworkResponseWithFileContent("login_with_sid_otp.json", 200)
        viewModel.loginDTO = LoginDTO(sid ="3001363575")
        viewModel.loginWithOTP("000000")
        assert(viewModel.progressListener.getOrAwaitValue())
        assert(viewModel.getValidateResponse().getOrAwaitValue().getContentIfNotHandled()?.code == 0)
        assert(!viewModel.progressListener.getOrAwaitValue())
    }

    @Test
    fun testLoginWithSIDAndInvalidOTP() {
        mockNetworkResponseWithFileContent("login_with_sid_invalid_otp.json", 200)
        viewModel.loginDTO = LoginDTO(sid ="3001363575")
        viewModel.loginWithOTP("123456")
        assert(viewModel.progressListener.getOrAwaitValue())
        assert(viewModel.getError().getOrAwaitValue().getContentIfNotHandled().equals("The OTP entered is incorrect. Please try again."))
        assert(!viewModel.progressListener.getOrAwaitValue())
    }

    @Test
    fun testLoginWithRMNAndOTP() {
        mockNetworkResponseWithFileContent("login_with_rmn_otp.json", 200)
        viewModel.loginDTO = LoginDTO(rmn = "8373991979")
        viewModel.loginWithOTP("000000")
        assert(viewModel.progressListener.getOrAwaitValue())
        assert(viewModel.getValidateResponse().getOrAwaitValue().getContentIfNotHandled()?.code == 0)
        assert(!viewModel.progressListener.getOrAwaitValue())
    }

    @Test
    fun testLoginWithRMNAndInvalidOTP() {
        mockNetworkResponseWithFileContent("login_with_rmn_invalid_otp.json", 200)
        viewModel.loginDTO = LoginDTO(rmn = "8373991979")
        viewModel.loginWithOTP("123456")
        assert(viewModel.progressListener.getOrAwaitValue())
        assert(viewModel.getError().getOrAwaitValue().getContentIfNotHandled().equals("The OTP entered is incorrect. Please try again."))
        assert(!viewModel.progressListener.getOrAwaitValue())
    }

    // Working SID with Password needed to test this case
    /*@Test
    fun testLoginWithSIDAndPassword() {
        mockNetworkResponseWithFileContent(".json", 200)
        viewModel.loginDTO = LoginDTO(sid ="3001363575")
        viewModel.loginWithPassword()
        assert(viewModel.progressListener.getOrAwaitValue())
        assert(viewModel.getValidateResponse().getOrAwaitValue().getContentIfNotHandled()?.code == 0)
        assert(!viewModel.progressListener.getOrAwaitValue())
    }*/

    @Test
    fun testLoginWithSIDAndInvalidPassword() {
        mockNetworkResponseWithFileContent("login_with_sid_invalid_password.json", 200)
        viewModel.loginDTO = LoginDTO(sid ="3001363575")
        viewModel.loginWithPassword("testPassword@123")
        assert(viewModel.progressListener.getOrAwaitValue())
        assert(viewModel.getError().getOrAwaitValue().getContentIfNotHandled().equals("The Password entered is incorrect. Please try again."))
        assert(!viewModel.progressListener.getOrAwaitValue())
    }
}