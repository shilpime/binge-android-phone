package com.tatasky.binge.data.di.modules

import android.content.Context
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.pubnub.PubnubHelper
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PubNubModule {
    @Singleton
    @Provides
    fun providePubnubEvent(
        context: Context,
        sharedPrefs: PrefsRepo,
        mixpanelHelper: MixpanelHelper,
        subscriptionAnalytics: SubscriptionAnalytics,
        localBroadcastHelper: LocalBroadcastHelper,
        commonUseCase: CommonUseCase
    ): PubnubHelper{
        return PubnubHelper(context,sharedPrefs, mixpanelHelper, subscriptionAnalytics, localBroadcastHelper, commonUseCase)
    }
}