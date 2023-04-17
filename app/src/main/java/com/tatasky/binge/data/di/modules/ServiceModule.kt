package com.tatasky.binge.data.di.modules

import com.tatasky.binge.data.service.BingeNotificationService
import com.tatasky.binge.data.service.SubscriptionChangeNotifierService
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import javax.inject.Singleton

@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract fun contributeMyFirebaseMessagingService(): BingeNotificationService

    @ContributesAndroidInjector
    abstract fun contributeSubscriptionChangeNotifierService(): SubscriptionChangeNotifierService
}