package com.tatasky.binge.app

import android.app.Application
import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tatasky.binge.data.di.modules.*
import com.tatasky.binge.di.FakeNetworkModule
import com.tatasky.binge.di.FakeSharedPrefsModule
import com.tatasky.binge.ui.base.di.modules.AppModule
import com.tatasky.binge.ui.base.di.modules.UseCaseModule
import com.tatasky.binge.ui.base.di.viewmodel.ViewModelModule
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [(FakeNetworkModule::class),
        (RepoModule::class),
        (UseCaseModule::class),
        (ViewModelModule::class),
        (FakeSharedPrefsModule::class),
        (DatabaseModule::class),
        (AnalyticsModule::class),
        (PubNubModule::class),
        (LocalBroadcastModule::class),
        (ServiceModule::class)
    ]
)
class AppModuleTest {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }
}