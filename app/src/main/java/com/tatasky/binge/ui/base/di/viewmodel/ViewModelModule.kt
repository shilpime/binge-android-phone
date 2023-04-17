package com.tatasky.binge.ui.base.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.details.DetailViewModel
import com.tatasky.binge.ui.features.dialog.ConfettiDialogViewModel
import com.tatasky.binge.ui.features.dialog.DialogViewModel
import com.tatasky.binge.ui.features.fsinstallation.FSInstallationViewModel
import com.tatasky.binge.ui.features.games.GamesViewModel
import com.tatasky.binge.ui.features.home.bottomsheet.HomeBottomSheetViewModel
import com.tatasky.binge.ui.features.home.sub.SubViewModel
import com.tatasky.binge.ui.features.home.subpage.SeeAllViewModel
import com.tatasky.binge.ui.features.link_accounts.LinkAccountViewModel
import com.tatasky.binge.ui.features.more.SettingsViewModel
import com.tatasky.binge.ui.features.myaccount.MyAccountViewModel
import com.tatasky.binge.ui.features.notifications.NotificationViewModel
import com.tatasky.binge.ui.features.onboarding.login.LoginViewModel
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.GuestLoginViewModel
import com.tatasky.binge.ui.features.onboarding.marketing.MarketingViewModel
import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
import com.tatasky.binge.ui.features.player.PlayerViewModel
import com.tatasky.binge.ui.features.prime.viewmodel.PrimeViewModel
import com.tatasky.binge.ui.features.recharge.RechargeViewModel
import com.tatasky.binge.ui.features.search.model.SearchViewModel
import com.tatasky.binge.ui.features.splash.SplashViewModel
import com.tatasky.binge.ui.features.subscription.viewmodel.SubscriptionViewModel
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.ManagedAppViewModel
import com.tatasky.binge.ui.features.transaction_history.THViewModel
import com.tatasky.binge.ui.features.update_password.UpdatePasswordViewModel
import com.tatasky.binge.ui.features.updateprofile.EditProfileViewModel
import com.tatasky.binge.ui.features.watchlist.FavouriteViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    /**
     * Bind all the view models here
     */
    @Binds
    @IntoMap
    @ViewModelKey(CommonSampleViewModel::class)
    internal abstract fun bindOnBoardingViewModel(onBoardingViewModel: CommonSampleViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    internal abstract fun bindSplashViewModel(splashViewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DetailViewModel::class)
    internal abstract fun bindDetailsViewModel(detailViewModel: DetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    internal abstract fun bindLoginViewModel(loginViewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FavouriteViewModel::class)
    internal abstract fun bindFavouriteViewModel(favouriteViewModel: FavouriteViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(PlayerViewModel::class)
    internal abstract fun bindPlayerViewModel(playerViewModel: PlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SeeAllViewModel::class)
    internal abstract fun bindSubpageViewModel(seeAllViewModel: SeeAllViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DialogViewModel::class)
    internal abstract fun bindDialogViewModel(dialogViewModel: DialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    internal abstract fun bindSearchViewModel(searchViewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    internal abstract fun bindSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(MyAccountViewModel::class)
    internal abstract fun bindMyAccountViewModel(myAccountViewModel: MyAccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditProfileViewModel::class)
    internal abstract fun bindEditProfileViewModel(editProfileViewModel: EditProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UpdatePasswordViewModel::class)
    internal abstract fun bindUpdatePasswordViewModel(updatePasswordViewModel: UpdatePasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubViewModel::class)
    internal abstract fun bindSubViewViewModel(ViewModel: SubViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(THViewModel::class)
    internal abstract fun bindTHViewModel(ViewModel: THViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LinkAccountViewModel::class)
    internal abstract fun bindLinkAccountViewModel(ViewModel: LinkAccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    internal abstract fun bindNotificationViewModel(ViewModel: NotificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubscriptionViewModel::class)
    internal abstract fun bindNewViewModel(ViewModel: SubscriptionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RechargeViewModel::class)
    internal abstract fun bindRechargeViewModel(ViewModel: RechargeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FSInstallationViewModel::class)
    internal abstract fun bindFSInstallationViewModel(ViewModel: FSInstallationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MarketingViewModel::class)
    internal abstract fun bindMarketingViewModel(ViewModel: MarketingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PrimeViewModel::class)
    internal abstract fun bindPrimeViewModel(ViewModel: PrimeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeBottomSheetViewModel::class)
    internal abstract fun bindSelectLanguageBottomSheetViewModel(viewModel: HomeBottomSheetViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GuestLoginViewModel::class)
    internal abstract fun bindGuestLoginBottomSheetDialogViewModel(ViewModel: GuestLoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ParentalControlViewModel::class)
    internal abstract fun bindParentalControlViewModel(viewModel: ParentalControlViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FreemiumSubscriptionViewModel::class)
    internal abstract fun bindFreemiumSubscriptionViewModel(viewModel: FreemiumSubscriptionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConfettiDialogViewModel::class)
    internal abstract fun bindConfettiDialogViewModel(viewModel: ConfettiDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManagedAppViewModel::class)
    internal abstract fun managedAppViewModel(viewModel: ManagedAppViewModel) : ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(GamesViewModel::class)
    internal abstract fun bindGameViewModel(viewModel: GamesViewModel): ViewModel

}