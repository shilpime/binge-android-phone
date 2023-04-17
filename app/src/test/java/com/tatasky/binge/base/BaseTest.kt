package com.tatasky.binge.base

import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.app.DaggerUnitTestAppComponent
import com.tatasky.binge.app.FakeUnitTestModule
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.pubnub.PubnubHelper
import javax.inject.Inject

open class BaseTest {
    @Inject
    lateinit var commonUseCase: CommonUseCase
    @Inject
    lateinit var prefsRepo: PrefsRepo
    @Inject
    lateinit var pubnubHelper : PubnubHelper

    fun setUp() {
        val component = DaggerUnitTestAppComponent.builder().fakeUnitTestModule(FakeUnitTestModule()).build()
        component.into(this)
    }
}