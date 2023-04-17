package com.tatasky.binge.di

import android.app.Application
import android.content.Context
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.data.database.AppDatabase
import com.tatasky.binge.data.di.modules.*
import com.tatasky.binge.pubnub.PubnubHelper
import com.tatasky.binge.ui.base.di.modules.UseCaseModule
import com.tatasky.binge.ui.base.di.viewmodel.ViewModelModule
import dagger.Module
import dagger.Provides
import org.mockito.Mockito
import javax.inject.Singleton

@Module(
    includes = [
        (FakeSharedPrefsModule::class)
    ]
)
class TestAppModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }
//
//    @Provides
//    @Singleton
//    fun provideFakeDatabase(): AppDatabase {
//        return Mockito.mock(AppDatabase::class.java)
//    }
//
//    @Provides
//    @Singleton
//    fun provideFakePubnub(): PubnubHelper {
//        return Mockito.mock(PubnubHelper::class.java)
//    }
//
//    @Provides
//    @Singleton
//    fun provideFakeMoEngage(): MoEngageHelper {
//        return Mockito.mock(MoEngageHelper::class.java)
//    }
//
//    @Provides
//    @Singleton
//    fun provideFakeMixPanel(): MixpanelHelper {
//        return Mockito.mock(MixpanelHelper::class.java)
//    }
}