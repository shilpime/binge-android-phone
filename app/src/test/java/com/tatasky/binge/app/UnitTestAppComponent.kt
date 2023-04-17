package com.tatasky.binge.app

import com.tatasky.binge.base.BaseTest
import com.tatasky.binge.ui.base.di.AppComponent
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        (AndroidSupportInjectionModule::class),
        (AppModuleUnitTest::class)
    ]
)
interface UnitTestAppComponent : AppComponent {
    fun into(baseTest : BaseTest)
}