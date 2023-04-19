package com.tatasky.binge.ui.base.di.modules

import com.tatasky.binge.customviews.ConfettiDialog
import com.tatasky.binge.ui.base.di.scopes.PerFragment
import com.tatasky.binge.ui.features.common.AppUpdateDialog
import com.tatasky.binge.ui.features.details.DetailsFragment
import com.tatasky.binge.ui.features.details.EpisodeSeeAllFragment
import com.tatasky.binge.ui.features.details.EpisodeSeeAllVoiceSearchDialogFragment
import com.tatasky.binge.ui.features.details.bottomsheet.DetailEpisodeBottomSheetDialogFragment
import com.tatasky.binge.ui.features.details.bottomsheet.DetailMoreInfoBottomSheetDialogFragment
import com.tatasky.binge.ui.features.device_management.DeviceListFragment
import com.tatasky.binge.ui.features.dialog.CommonDialog
import com.tatasky.binge.ui.features.fsinstallation.FSConfirmAddressFragment
import com.tatasky.binge.ui.features.fsinstallation.FSInstallationMethodFragment
import com.tatasky.binge.ui.features.fsinstallation.FSInstalltionScheduleFragment
import com.tatasky.binge.ui.features.games.GamePlayerFragment
import com.tatasky.binge.ui.features.home.bottomsheet.categories.CategoriesBottomSheetDialog
import com.tatasky.binge.ui.features.home.bottomsheet.select_language.SelectLanguageBottomSheetDialog
import com.tatasky.binge.ui.features.home.bottomsheet.welcome.WelcomeMessageDialog
import com.tatasky.binge.ui.features.home.sub.SubFragment
import com.tatasky.binge.ui.features.home.subpage.AppSeeAllFragment
import com.tatasky.binge.ui.features.home.subpage.SeeAllFragment
import com.tatasky.binge.ui.features.home.subpage.SubHomeFragment
import com.tatasky.binge.ui.features.link_accounts.LinkAccountsFragment
import com.tatasky.binge.ui.features.link_accounts.LinkAccountsOTPFragment
import com.tatasky.binge.ui.features.link_accounts.LinkAccountsSuccessfulFragment
import com.tatasky.binge.ui.features.live_channel.LiveChannelDetailsFragment
import com.tatasky.binge.ui.features.live_channel.LiveChannelPlayerFragment
import com.tatasky.binge.ui.features.more.*
import com.tatasky.binge.ui.features.myaccount.MyAccountFragment
import com.tatasky.binge.ui.features.myaccount.profiles.SwitchAccountFragment
import com.tatasky.binge.ui.features.notifications.EditNotificationsDialog
import com.tatasky.binge.ui.features.notifications.NotificationFragment
import com.tatasky.binge.ui.features.onboarding.LicenseAgreementFragment
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.*
import com.tatasky.binge.ui.features.onboarding.marketing.MarketingFragment
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.*
import com.tatasky.binge.ui.features.parentalcontrol.sidemenu.ParentalControlRatingFragment
import com.tatasky.binge.ui.features.parentalcontrol.sidemenu.ParentalControlSettingsFragment
import com.tatasky.binge.ui.features.player.ErosnowPlayerFragment
import com.tatasky.binge.ui.features.player.HungamaPlayerFragment
import com.tatasky.binge.ui.features.prime.view.PrimeInterstitialFragment
import com.tatasky.binge.ui.features.prime.view.PrimeRechargeFragment
import com.tatasky.binge.ui.features.prime.view.PrimeTncFragment
import com.tatasky.binge.ui.features.prime.view.TransparentFragment
import com.tatasky.binge.ui.features.search.LanguageGenreFragment
import com.tatasky.binge.ui.features.search.SearchFragment
import com.tatasky.binge.ui.features.search.VoiceRecognizerDialogFragment
import com.tatasky.binge.ui.features.sidemenunavdrawer.NavDrawerFragment
import com.tatasky.binge.ui.features.sidemenunavdrawer.SettingsFragment
import com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.ContentLanguageFragment
import com.tatasky.binge.ui.features.splash.AppSplashFragment
import com.tatasky.binge.ui.features.subscription.view.*
import com.tatasky.binge.ui.features.subscription_freemium.*
import com.tatasky.binge.ui.features.subscription_freemium.view.*
import com.tatasky.binge.ui.features.transaction_history.TransactionHistoryFragment
import com.tatasky.binge.ui.features.ttnplayer.TTNPlayerFragment
import com.tatasky.binge.ui.features.update_password.UpdatePasswordFragment
import com.tatasky.binge.ui.features.updateprofile.EditProfileFragment
import com.tatasky.binge.ui.features.watchlist.WatchlistFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class FragmentModule {

    @ContributesAndroidInjector
    @PerFragment
    abstract fun splashFragment(): AppSplashFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun detailsFragment(): DetailsFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun watchlistFragment(): WatchlistFragment


    @ContributesAndroidInjector
    @PerFragment
    abstract fun searchLandingFragment(): SearchFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun subpageGridFragment(): SeeAllFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun subHomeFragment(): SubHomeFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun contactUsFragment(): ContactUsFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun notificationSettingsFragment(): NotificationSettingsFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun privacyPolicyFragment(): PrivacyPolicyFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun tncFragment(): TermsConditionFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun licenseFragment(): LicenseAgreementFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun commonDialogFragment(): CommonDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun recommendationDialogFragment(): RecommendationDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun appUpdateDialogFragment(): AppUpdateDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun cancellationSelectionDialog(): CancellationSelectionDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun myAccountFragment(): MyAccountFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun editProfileFragment(): EditProfileFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun updatePasswordWithoutOTPFragment(): UpdatePasswordFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun appsSeeAllFragment(): AppSeeAllFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun deviceListFragment(): DeviceListFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun transactionHistoryFragment(): TransactionHistoryFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun linkAccountsFragment(): LinkAccountsFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun linkAccountsOTPFragment(): LinkAccountsOTPFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun linkAccountsSuccessfulFragment(): LinkAccountsSuccessfulFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun notificationFragment(): NotificationFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun EditNotificationsDialogFragment(): EditNotificationsDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun switchAccount(): SwitchAccountFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun hungamaPlayerFragment(): HungamaPlayerFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun ttnPlayerFragment(): TTNPlayerFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun voiceRecognizerFragment(): VoiceRecognizerDialogFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun faqFragment(): FaqFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun subscriptionFragment(): SubscriptionFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun summaryFragment(): SubscriptionSummaryFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun currentSubscriptionFragment(): CurrentSubscriptionFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun rechargeSubscriptionFragment(): RechargeSubscriptionFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun languageGenreFragment(): LanguageGenreFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun firestickDialogFragment(): FirestickDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun firestickUpSellDialogFragment(): FireStickUpsellDialog


    @ContributesAndroidInjector
    @PerFragment
    abstract fun fsInstallationScheduleFragment(): FSInstalltionScheduleFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun FSInstallationMethodFragment(): FSInstallationMethodFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun FSConfirmAddressFragment(): FSConfirmAddressFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun logoutDialog(): LogoutDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun deviceStatusLogoutDialog(): DeviceStatusLogoutDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun knowMoreDialog(): KnowMoreDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun ErosnowPlayerFragment(): ErosnowPlayerFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun atvCancellationDialog(): AtvCancellationDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun marketingFragment(): MarketingFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun primeInterstitialFragment(): PrimeInterstitialFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun primeRechargeFragment(): PrimeRechargeFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun primeTncFragment(): PrimeTncFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun transparentFragment(): TransparentFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun trialSubscriptionFragment(): TrialSubscriptionFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun trialSubscribedFragment(): TrialCurrentSubscriptionFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun trialUpgradeFragment(): FreeTrialUpgradeFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun subFragment(): SubFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun selectLanguageBottomSheetDialog(): SelectLanguageBottomSheetDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun guestLoginBottomSheetDialogFragment(): GuestLoginBottomDialogFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun guestLoginFragment(): GuestLoginFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun guestLoginBaidListingFragment(): GuestBAIDListingFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun guestLoginSidListingFragment(): GuestSidListingFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun guestLoginPreviouslyUsedMobileFragment(): UsedRMNListingFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun parentalPinSetupFragment(): ParentalPinSetupFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun parentalPinSuccessFragment(): ParentalPinSuccessFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun parentalControlSettingsFragment(): ParentalControlSettingsFragment

//    @ContributesAndroidInjector
//    @PerFragment
//    abstract fun parentalControlOtpFragment(): ParentalControlOtpFragment
//
//    @ContributesAndroidInjector
//    @PerFragment
//    abstract fun parentalControlPinFragment(): ParentalControlPinFragment
//
//    @ContributesAndroidInjector
//    @PerFragment
//    abstract fun parentalControlSetupFragment(): ParentalControlSetupFragment

//    @ContributesAndroidInjector
//    @PerFragment
//    abstract fun hambergerMenuFragment(): ParentalPinMenuFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun settingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun navDrawerFragment(): NavDrawerFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun detailMoreInfoBottomSheetDialogFragment(): DetailMoreInfoBottomSheetDialogFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun detailEpisodeBottomSheetDialogFragment(): DetailEpisodeBottomSheetDialogFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun episodeSeeAll(): EpisodeSeeAllFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun episodeSeeAllVoiceSearchDiaglogFragment(): EpisodeSeeAllVoiceSearchDialogFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun parentalControlRatingFragment(): ParentalControlRatingFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun parentalControlBottomDialogFragment(): ParentalControlBottomDialogFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun parentalControlSuccessFragment(): ParentalControlSuccessFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun parentalControlVerificationFragment(): ParentalControlVerificationFragment
    @ContributesAndroidInjector
    @PerFragment
    abstract fun contentLanguageFragment(): ContentLanguageFragment


    @ContributesAndroidInjector
    @PerFragment
    abstract fun subscriptionTenureBottomSheetDialogFragment(): SubscriptionTenureBottomSheetDialogFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun comparePlanFragment(): ComparePlanFragment


    @ContributesAndroidInjector
    @PerFragment
    abstract fun freemiumCurrentSubscriptionFragment(): FreemiumCurrentSubscriptionFragment


    @ContributesAndroidInjector
    @PerFragment
    abstract fun freemiumSubscriptionFragment(): FreemiumSubscriptionFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun otherOptionsBottomSheetDialogFragment(): MyPlanOtherOptionsBottomSheet

    @ContributesAndroidInjector
    @PerFragment
    abstract fun confettiDialog():ConfettiDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun cancellationConfirmationBottomSheet():CancellationConfirmationBottomSheet

    @ContributesAndroidInjector
    @PerFragment
    abstract fun addOnCancellationBottomSheet():AddOnCancellationBottomSheet

    @ContributesAndroidInjector
    @PerFragment
    abstract fun parentalControlDialogOtpFragment(): ParentalControlDialogOtpFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun categoriesBottomSheetDialog(): CategoriesBottomSheetDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun walletpaymentFragment(): WalletpaymentFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun fragmentSubscriptionPopup(): SubscriptionStarterFragment


    @ContributesAndroidInjector
    @PerFragment
    abstract fun fragmentSubscriptionBottomContainer(): SubscriptionBottomContainerFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun fragmentSubscriptionTypeSelector(): SubscriptionTypeSelectorFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun managedAppFragment(): ManagedAppFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun managedAppsContainerFragment(): ManagedAppsContainerFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun gamePlayerFragment(): GamePlayerFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun welcomeMessageDialogFragment(): WelcomeMessageDialog

    @ContributesAndroidInjector
    @PerFragment
    abstract fun liveChannelDetailsFragment(): LiveChannelDetailsFragment

    @ContributesAndroidInjector
    @PerFragment
    abstract fun liveChannelPlayerFragment(): LiveChannelPlayerFragment

}