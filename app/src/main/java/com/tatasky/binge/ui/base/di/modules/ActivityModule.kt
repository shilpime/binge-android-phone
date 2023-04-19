package com.tatasky.binge.ui.base.di.modules

import com.tatasky.binge.ui.base.di.scopes.ActivityScope
import com.tatasky.binge.ui.features.device_management.DeviceListManagementActivity
import com.tatasky.binge.ui.features.fsinstallation.FSInstallationActivity
import com.tatasky.binge.ui.features.games.GamePlayerActivity
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.onboarding.LicenseAgreementActivity
import com.tatasky.binge.ui.features.onboarding.marketing.MarketingActivity
import com.tatasky.binge.ui.features.player.PlayerActivity
import com.tatasky.binge.ui.features.prime.view.PrimeActivationActivity
import com.tatasky.binge.ui.features.prime.view.PrimeActivity
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.splash.AppSplashActivity
import com.tatasky.binge.ui.features.subscription_freemium.FreemiumSubscriptionActivity
import com.tatasky.binge.ui.features.subscription_freemium.PaymentJourneyActivity
import com.tatasky.binge.ui.features.subscription_freemium.WalletPaymentActivity
import com.tatasky.binge.ui.features.switchaccount.SwitchAccountActivity
import com.tatasky.binge.ui.features.zee5.InAppBrowserActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ActivityModule {

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun splashActivity(): AppSplashActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun homeActivity(): LandingActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun playerActivity(): PlayerActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun rechargeActivity(): RechargeActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun fsInstallationActivity(): FSInstallationActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun inappBrowser(): InAppBrowserActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun marketingActivity(): MarketingActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun primeActivity(): PrimeActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun primeActivationActivity(): PrimeActivationActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun switchAccountActivity() : SwitchAccountActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun licenseAgreementActivity() : LicenseAgreementActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun freemiumSubscriptionActivity() : FreemiumSubscriptionActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun paymentJourneyActivity() : PaymentJourneyActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun deviceListManagementActivity() : DeviceListManagementActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun walletPaymentActivity() : WalletPaymentActivity

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    @ActivityScope
    abstract fun gamePlayerActivity() : GamePlayerActivity

}