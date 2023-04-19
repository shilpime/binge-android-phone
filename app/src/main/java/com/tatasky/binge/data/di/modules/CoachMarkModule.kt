package com.tatasky.binge.data.di.modules

import android.content.Context
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.data.datastore.DataStorePrefs
import com.tatasky.binge.domain.repositories.DataStorePrefsRepo
import com.tatasky.binge.ui.features.coachmark.CoachMark
import com.tatasky.binge.ui.features.coachmark.CoachMarkAnalytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CoachMarkModule {

    @Singleton
    @Provides
    fun provideCoachMark(coachMarkAnalytics: CoachMarkAnalytics): CoachMark {
        return CoachMark(coachMarkAnalytics)
    }
}