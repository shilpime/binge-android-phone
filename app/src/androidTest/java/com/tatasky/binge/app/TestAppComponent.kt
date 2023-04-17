package com.tatasky.binge.app

import android.app.Application
import com.tatasky.binge.base.BaseViewModelTest
import com.tatasky.binge.di.FakeNetworkModule
import com.tatasky.binge.screens.onboarding.LoginViewModelTest
import com.tatasky.binge.screens.splash.AppSplashViewModelTest
import com.tatasky.binge.ui.base.di.AppComponent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.ui.features.splash.SplashViewModel
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import dagger.android.support.DaggerApplication
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        (AndroidSupportInjectionModule::class),
        (AppModuleTest::class)
    ]
)
interface TestAppComponent : AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun network(application: FakeNetworkModule): Builder
        fun build(): TestAppComponent
    }

    override fun inject(instance: DaggerApplication)
    fun inject(baseViewModelTest: AppSplashViewModelTest)
    fun inject(baseViewModelTest: LoginViewModelTest)
}