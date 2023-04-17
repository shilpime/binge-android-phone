package com.tatasky.binge.screens.onboarding

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.tatasky.binge.app.RxSchedulerRule
import com.tatasky.binge.base.BaseTest
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.LoginDTO
import com.tatasky.binge.data.networking.models.response.FaqResponse
import com.tatasky.binge.data.networking.models.response.GetOtpResponse
import com.tatasky.binge.data.networking.models.response.ValidateOTPResponse
import com.tatasky.binge.pubnub.PubnubHelper
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.extensions.getOrAwaitValue
import com.tatasky.binge.ui.features.onboarding.login.LoginViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.junit.MockitoJUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class LoginViewModelUnitTest : BaseTest() {
    @get:Rule
    val mockitoRule = MockitoJUnit.rule()
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var rule: TestRule = RxSchedulerRule()
    @Before
    fun setUpTest() = super.setUp()

    @Test
    fun `test get otp success from rmn`() {
        Assert.assertNotNull(commonUseCase)
        Assert.assertNotNull(prefsRepo)
        val viewModel = LoginViewModel(commonUseCase, prefsRepo, pubnubHelper)
        viewModel.loginDTO = LoginDTO("8373991979", actualRMN = "8373991979")
        val response = GetOtpResponse().apply {
            this.data = UserData().apply {
                this.rmn = "8373991979"
            }
        }
        val observer : Observer<SingleEvent<GetOtpResponse>> = spyk(Observer{})
        val loadingObserver : Observer<Boolean> = spyk(Observer{})
        viewModel.getOtpResponse().observeForever(observer)
        viewModel.progressListener.observeForever(loadingObserver)
        every { commonUseCase.generateOTP(viewModel.loginDTO, false) } returns Single.just(response)
        viewModel.generateOTP(false)
        verify { loadingObserver.onChanged(true) }
        assert(viewModel.getOtpResponse().getOrAwaitValue().getContentIfNotHandled()==response)
        verify { loadingObserver.onChanged(false) }
    }

    @Test
    fun `test get otp success from sid`() {
        Assert.assertNotNull(commonUseCase)
        Assert.assertNotNull(prefsRepo)
        val viewModel = LoginViewModel(commonUseCase, prefsRepo, pubnubHelper)
        viewModel.loginDTO = LoginDTO(sid = "3001363575")
        val response = GetOtpResponse().apply {
            this.data = UserData().apply {
                this.maskedNumber="xxxxx91979"
                this.sid="3001363575"
            }
        }
        val observer : Observer<SingleEvent<GetOtpResponse>> = spyk(Observer{})
        val loadingObserver : Observer<Boolean> = spyk(Observer{})
        viewModel.getOtpResponse().observeForever(observer)
        viewModel.progressListener.observeForever(loadingObserver)
        every { commonUseCase.generateOTP(viewModel.loginDTO, false) } returns Single.just(response)
        viewModel.generateOTP(false)
        verify { loadingObserver.onChanged(true) }
        assert(viewModel.getOtpResponse().getOrAwaitValue().getContentIfNotHandled()==response)
        verify { loadingObserver.onChanged(false) }
    }

    @Test
    fun `test get otp failed from rmn`() {
        Assert.assertNotNull(commonUseCase)
        Assert.assertNotNull(prefsRepo)
        val viewModel = LoginViewModel(commonUseCase, prefsRepo, pubnubHelper)
        viewModel.loginDTO = LoginDTO("8373991979", actualRMN = "8373991979")
        val response = GetOtpResponse().apply {
            this.code = 10
            this.message = "Message"
        }
        val observer : Observer<SingleEvent<GetOtpResponse>> = spyk(Observer{})
        val loadingObserver : Observer<Boolean> = spyk(Observer{})
        val errorObserver : Observer<SingleEvent<ErrorModel>> = spyk(Observer{})
        viewModel.getOtpResponseError().observeForever(errorObserver)
        viewModel.progressListener.observeForever(loadingObserver)
        every { commonUseCase.generateOTP(viewModel.loginDTO, false) } returns Single.just(response)
        viewModel.generateOTP(false)
        verify { loadingObserver.onChanged(true) }
        assert(viewModel.getOtpResponseError().getOrAwaitValue().getContentIfNotHandled()?.code!=0)
        verify { loadingObserver.onChanged(false) }
    }

    @Test
    fun `test get otp failed from sid`() {
        Assert.assertNotNull(commonUseCase)
        Assert.assertNotNull(prefsRepo)
        val viewModel = LoginViewModel(commonUseCase, prefsRepo, pubnubHelper)
        viewModel.loginDTO = LoginDTO("8373991979", actualRMN = "8373991979")
        val response = GetOtpResponse().apply {
            this.code = 10
            this.message = "Message"
        }
        val observer : Observer<SingleEvent<GetOtpResponse>> = spyk(Observer{})
        val loadingObserver : Observer<Boolean> = spyk(Observer{})
        val errorObserver : Observer<SingleEvent<ErrorModel>> = spyk(Observer{})
        viewModel.getOtpResponseError().observeForever(errorObserver)
        viewModel.progressListener.observeForever(loadingObserver)
        every { commonUseCase.generateOTP(viewModel.loginDTO, false) } returns Single.just(response)
        viewModel.generateOTP(false)
        verify { loadingObserver.onChanged(true) }
        assert(viewModel.getOtpResponseError().getOrAwaitValue().getContentIfNotHandled()?.code!=0)
        verify { loadingObserver.onChanged(false) }
    }

    @Test(expected = TimeoutException::class)
    fun `test get login with password failed`() {
        Assert.assertNotNull(commonUseCase)
        Assert.assertNotNull(prefsRepo)
        val viewModel = LoginViewModel(commonUseCase, prefsRepo, pubnubHelper)
        viewModel.loginAnalytics = mockk(relaxed =  true)
        viewModel.loginDTO = LoginDTO(sid ="3001363575")
        val response = ValidateOTPResponse().apply {
            this.code = 0
        }
        val loadingObserver : Observer<Boolean> = spyk(Observer{})
        viewModel.progressListener.observeForever(loadingObserver)
        every { commonUseCase.loginWithPassword(any())} returns Single.just(response)
        viewModel.loginWithPassword("test")
        verify { loadingObserver.onChanged(true) }
        assert(viewModel.getValidateResponse().getOrAwaitValue().getContentIfNotHandled()!=null)
        assert(viewModel.errorMessage.getOrAwaitValue().getContentIfNotHandled()!=null)
        verify { loadingObserver.onChanged(false) }
    }
}