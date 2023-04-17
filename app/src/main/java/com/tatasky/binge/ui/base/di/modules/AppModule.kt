package com.tatasky.binge.ui.base.di.modules

import android.app.Application
import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tatasky.binge.data.di.modules.*
import com.tatasky.binge.ui.base.di.viewmodel.ViewModelModule
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        (NetworkModule::class),
        (RepoModule::class),
        (UseCaseModule::class),
        (ViewModelModule::class),
        (SharedPrefsModule::class),
        (DatabaseModule::class),
        (AnalyticsModule::class),
        (PubNubModule::class),
        (LocalBroadcastModule::class),
        (ServiceModule::class),
        (DataStorePrefsModule::class),
        (CoachMarkModule::class)
    ]
)
open class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }
}