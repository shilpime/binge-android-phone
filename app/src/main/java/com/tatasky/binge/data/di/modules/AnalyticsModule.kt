package com.tatasky.binge.data.di.modules

import android.content.Context
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.facebook.FacebookAnalyticsHelper
import com.tatasky.binge.analytics.firebase.FirebaseAnalyticsHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.ui.features.MiscAnalytics
import com.tatasky.binge.ui.features.coachmark.CoachMarkAnalytics
import com.tatasky.binge.ui.features.common.ForceUpdateAnalytics
import com.tatasky.binge.ui.features.details.DetailAnalytics
import com.tatasky.binge.ui.features.device_management.DeviceListManagementAnalytics
import com.tatasky.binge.ui.features.games.GameAnalytics
import com.tatasky.binge.ui.features.home.HomeAnalytics
import com.tatasky.binge.ui.features.home.subpage.SeeAllAnalytics
import com.tatasky.binge.ui.features.more.MoreAnalytics
import com.tatasky.binge.ui.features.notifications.NotificationAnalytics
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.player.PlayerAnalytics
import com.tatasky.binge.ui.features.prime.PrimeAnalytics
import com.tatasky.binge.ui.features.search.SearchAnalytics
import com.tatasky.binge.ui.features.sidemenunavdrawer.SideMenuDrawerAnalytics
import com.tatasky.binge.ui.features.splash.SplashAnalytics
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.update_password.UpdatePasswordAnalytics
import com.tatasky.binge.ui.features.updateprofile.ProfileAnalytics
import com.tatasky.binge.ui.features.watchlist.WatchlistAnalytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AnalyticsModule {
    @Singleton
    @Provides
    fun provideMixpanelEvent(context: Context): MixpanelHelper {
        return MixpanelHelper(context)
    }

    @Singleton
    @Provides
    fun provideMoEngageEvent(context: Context): MoEngageHelper {
        return MoEngageHelper(context)
    }

    @Singleton
    @Provides
    fun provideFirebaseAnalyticsHelper(context: Context): FirebaseAnalyticsHelper {
        return FirebaseAnalyticsHelper(context)
    }

    @Singleton
    @Provides
    fun provideFacebookAnalyticsHelper(context: Context): FacebookAnalyticsHelper {
        return FacebookAnalyticsHelper(context)
    }

    @Singleton
    @Provides
    fun provideLoginAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
        facebookAnalyticsHelper: FacebookAnalyticsHelper,
        appsFlyerHelper: AppsFlyerHelper
    ): LoginAnalytics {
        return LoginAnalytics(mixpanelHelper, moEngageHelper, firebaseAnalyticsHelper, facebookAnalyticsHelper, appsFlyerHelper)
    }

    @Singleton
    @Provides
    fun provideSubscriptionAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
        facebookAnalyticsHelper: FacebookAnalyticsHelper,
        appsFlyerHelper: AppsFlyerHelper
    ): SubscriptionAnalytics {
        return SubscriptionAnalytics(mixpanelHelper, moEngageHelper,facebookAnalyticsHelper,firebaseAnalyticsHelper, appsFlyerHelper)
    }

    @Singleton
    @Provides
    fun providePlayerAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        appsFlyerHelper: AppsFlyerHelper,
        firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
        facebookAnalyticsHelper: FacebookAnalyticsHelper
    ): PlayerAnalytics {
        return PlayerAnalytics(mixpanelHelper, moEngageHelper, appsFlyerHelper, firebaseAnalyticsHelper, facebookAnalyticsHelper)
    }

    @Singleton
    @Provides
    fun provideHomeAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        appsFlyerHelper: AppsFlyerHelper,
        firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
        facebookAnalyticsHelper: FacebookAnalyticsHelper
    ): HomeAnalytics {
        return HomeAnalytics(mixpanelHelper, moEngageHelper, appsFlyerHelper, firebaseAnalyticsHelper, facebookAnalyticsHelper)
    }

    @Singleton
    @Provides
    fun provideMiscAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        appsFlyerHelper: AppsFlyerHelper,
        firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
        facebookAnalyticsHelper: FacebookAnalyticsHelper
    ): MiscAnalytics {
        return MiscAnalytics(mixpanelHelper, moEngageHelper, appsFlyerHelper, firebaseAnalyticsHelper, facebookAnalyticsHelper)
    }



    @Singleton
    @Provides
    fun provideDetailAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        appsFlyerHelper: AppsFlyerHelper
    ): DetailAnalytics {
        return DetailAnalytics(mixpanelHelper, moEngageHelper, appsFlyerHelper)
    }

    @Singleton
    @Provides
    fun provideWatchlistAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        appsFlyerHelper: AppsFlyerHelper
    ): WatchlistAnalytics {
        return WatchlistAnalytics(mixpanelHelper, moEngageHelper, appsFlyerHelper)
    }

    @Singleton
    @Provides
    fun provideMoreAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        appsFlyerHelper: AppsFlyerHelper
    ): MoreAnalytics {
        return MoreAnalytics(mixpanelHelper, moEngageHelper, appsFlyerHelper)
    }

    @Singleton
    @Provides
    fun provideSplashAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        appsFlyerHelper: AppsFlyerHelper,
        firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
        facebookAnalyticsHelper: FacebookAnalyticsHelper
    ): SplashAnalytics {
        return SplashAnalytics(mixpanelHelper, moEngageHelper, appsFlyerHelper, firebaseAnalyticsHelper, facebookAnalyticsHelper)
    }

    @Singleton
    @Provides
    fun provideSearchAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        appsFlyerHelper: AppsFlyerHelper
    ): SearchAnalytics {
        return SearchAnalytics(mixpanelHelper, moEngageHelper, appsFlyerHelper)
    }

    @Singleton
    @Provides
    fun provideDeviceListManagementAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper
    ): DeviceListManagementAnalytics {
        return DeviceListManagementAnalytics(mixpanelHelper, moEngageHelper)
    }

    @Singleton
    @Provides
    fun provideProfileAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper
    ): ProfileAnalytics {
        return ProfileAnalytics(mixpanelHelper, moEngageHelper)
    }

    @Singleton
    @Provides
    fun provideUpdatePasswordAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper
    ): UpdatePasswordAnalytics {
        return UpdatePasswordAnalytics(mixpanelHelper, moEngageHelper)
    }

    @Singleton
    @Provides
    fun provideNotificationAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper
    ): NotificationAnalytics {
        return NotificationAnalytics(mixpanelHelper, moEngageHelper)
    }

    @Singleton
    @Provides
    fun providePrimeAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        appsFlyerHelper: AppsFlyerHelper
    ): PrimeAnalytics {
        return PrimeAnalytics(mixpanelHelper, moEngageHelper, appsFlyerHelper)
    }

    @Singleton
    @Provides
    fun provideAppsFlyerHelper(context: Context, sharedPrefs: PrefsRepo, mixpanelHelper: MixpanelHelper): AppsFlyerHelper {
        return AppsFlyerHelper(context, sharedPrefs, mixpanelHelper)
    }

    @Singleton
    @Provides
    fun provideSeeAllAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        appsFlyerHelper: AppsFlyerHelper
    ): SeeAllAnalytics {
        return SeeAllAnalytics(mixpanelHelper, moEngageHelper, appsFlyerHelper)
    }

    @Singleton
    @Provides
    fun provideSideMenuDrawerAnalytics(
        appsFlyerHelper: AppsFlyerHelper,
        mixpanelHelper: MixpanelHelper
    ): SideMenuDrawerAnalytics {
        return SideMenuDrawerAnalytics(appsFlyerHelper, mixpanelHelper)
    }

    @Singleton
    @Provides
    fun provideForceUpdateAnalytics(
        moEngageHelper: MoEngageHelper
    ): ForceUpdateAnalytics {
        return ForceUpdateAnalytics(moEngageHelper)
    }


    @Singleton
    @Provides
    fun provideGameAnalytics(
        mixpanelHelper: MixpanelHelper,
        moEngageHelper: MoEngageHelper,
        appsFlyerHelper: AppsFlyerHelper
    ): GameAnalytics {
        return GameAnalytics(mixpanelHelper, moEngageHelper, appsFlyerHelper)
    }

    @Singleton
    @Provides
    fun provideCoachMarkAnalytics(
        mixpanelHelper: MixpanelHelper
    ): CoachMarkAnalytics {
        return CoachMarkAnalytics(mixpanelHelper)
    }
}