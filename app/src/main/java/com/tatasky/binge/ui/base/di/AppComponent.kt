package com.tatasky.binge.ui.base.di

import android.app.Application
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.data.database.AppDatabase
import com.tatasky.binge.data.di.modules.NetworkModule
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.pubnub.PubnubHelper
import com.tatasky.binge.ui.base.di.modules.ActivityModule
import com.tatasky.binge.ui.base.di.modules.AppModule
import com.tatasky.binge.ui.base.di.modules.FragmentModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.android.support.DaggerApplication
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        (AndroidSupportInjectionModule::class),
        (AppModule::class),
        (ActivityModule::class),
        (FragmentModule::class)
    ]
)

interface AppComponent : AndroidInjector<DaggerApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun network(networkModule: NetworkModule): Builder
        fun build(): AppComponent
    }

    override fun inject(instance: DaggerApplication)

    fun sharedPreference(): PrefsRepo
    fun database(): AppDatabase
    fun pubnubHelper(): PubnubHelper
    fun moHelper(): MoEngageHelper
    fun mpHelper(): MixpanelHelper
    // It will provide instance of given type to let us instantiate the Appsflyer at App level
    fun appsflyerHelper(): AppsFlyerHelper
    fun localBroadCastHelper(): LocalBroadcastHelper
}