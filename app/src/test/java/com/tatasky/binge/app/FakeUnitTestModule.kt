package com.tatasky.binge.app

import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.data.database.AppDatabase
import com.tatasky.binge.domain.repositories.CommonRepository
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.pubnub.PubnubHelper
import dagger.Module
import dagger.Provides
import io.mockk.mockk
import io.mockk.spyk
import javax.inject.Singleton

@Module
class FakeUnitTestModule {
    @Singleton
    @Provides
    fun providePubnubModule() : PubnubHelper = mockk()

    @Singleton
    @Provides
    fun provideLocalBroadcastHelper() : LocalBroadcastHelper = mockk()

    @Singleton
    @Provides
    fun provideRepo() : CommonRepository = mockk()

    @Singleton
    @Provides
    fun provideCommonUseCase() : CommonUseCase = mockk()

    @Singleton
    @Provides
    fun provideDataBase() : AppDatabase = mockk()

    @Singleton
    @Provides
    fun provideSharedPrefs() : PrefsRepo = FakeUnitSharedPrefs()

    @Singleton
    @Provides
    fun provideMixpanelHelper() : MixpanelHelper = mockk()

    @Singleton
    @Provides
    fun provideMoEngageHelper() : MoEngageHelper = mockk()

    @Singleton
    @Provides
    fun provideAppsFlyerHelper() : AppsFlyerHelper = spyk()
}