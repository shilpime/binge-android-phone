package com.tatasky.binge.ui.features.games

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import com.facebook.FacebookSdk
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.FragmentGamesPlayerBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.setSingleOnClick
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.utils.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_recharge.*
import javax.inject.Inject


class GamePlayerFragment : BaseFragment<FragmentGamesPlayerBinding, GamesViewModel>() {
    lateinit var contentItem : ContentItem
    var gamesMixpanelInfoModel: GamesMixpanelInfoModel? = null
    var freeGame: String = NO
    val customSnackbarWithTwoActionsUtil = CustomSnackbarWithTwoActionsUtil()
    @Inject
    lateinit var gameAnalytics: GameAnalytics

    override fun getViewModelClass(): Class<GamesViewModel> =
        GamesViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_games_player

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    override fun setObserver() {
        viewModel.getAddFavGame().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let{
                if (it) {
                    if (binding.lvGameFav.progress == 0F) {
                        context?.let { it1 -> vibratePhone(it1,100L) }
                        gamesMixpanelInfoModel?.let{it ->
                            gameAnalytics.trackGameAddToFav(
                                pageName = it.pageName,
                                railTitle = it.railTitle,
                                railPosition = it.railPosition,
                                railType = com.tatasky.binge.analytics.EDITORIAL,
                                railCategory = it.railCategory,
                                gameGenre = it.gameGenre,
                                gamePartner = contentItem.provider,
                                gamePosition = it.gamePosition,
                                gameRating = it.gameRating,
                                gameTitle = contentItem.title,
                                freeGame = freeGame,
                                releaseYear = contentItem.releaseYear ?: "",
                                deviceType = PLATFORM_ANDROID_CAPS,
                                source = it.source,
                                packPrice = sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                                packName = sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM
                            )
                        }
                        binding.lvGameFav.playAnimation()
                        customSnackbarWithTwoActionsUtil.showCustomSnackbarWithTwoActions(
                            context = requireContext(),
                            snackbarType = CustomSnackbarWithTwoActionsType.SnackbarTypeNormalSizeImage,
                            mszTitle = getString(R.string.add_fav_game),
                            mszDesc = "",
                            imgResourceSmall = R.drawable.ic_tick_login_success,
                            imgResourceLarge = null,
                            imgResourceCancel = R.drawable.ic_cross,
                            btnActionText = "",
                            maxProgress = 0,
                            currProgress = 0,
                            lambdaAction = {
                                customSnackbarWithTwoActionsUtil.hideCustomSnackbarWithTwoActions()
                                activity?.onBackPressed()
                            },
                            lambdaCancel = {
                                customSnackbarWithTwoActionsUtil.hideCustomSnackbarWithTwoActions()
                            }
                        )
                        Handler(Looper.getMainLooper()).postDelayed(
                            { customSnackbarWithTwoActionsUtil?.hideCustomSnackbarWithTwoActions() },
                            4000
                        )
                    }
                } else {
                    gamesMixpanelInfoModel?.let{it ->
                        gameAnalytics.trackGameRemoveFromFav(
                            pageName = it.pageName,
                            railTitle = it.railTitle,
                            railPosition = it.railPosition,
                            railType = com.tatasky.binge.analytics.EDITORIAL,
                            railCategory = it.railCategory,
                            gameGenre = it.gameGenre,
                            gamePartner = contentItem.provider,
                            gamePosition = it.gamePosition,
                            gameRating = it.gameRating,
                            gameTitle = contentItem.title,
                            freeGame = freeGame,
                            releaseYear = contentItem.releaseYear ?: "",
                            deviceType = PLATFORM_ANDROID_CAPS,
                            source = it.source,
                            packPrice = sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                            packName = sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM
                        )
                    }
                    customSnackbarWithTwoActionsUtil.hideCustomSnackbarWithTwoActions()
                    binding.lvGameFav.progress = 0F
                }
            }
        })

        viewModel.getGameFavResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                binding.lvGameFav.show()
                if (it.data?.favourite == true) {
                    binding.lvGameFav.progress = 1F
                } else {
                    binding.lvGameFav.progress = 0F
                }
            }
        })
    }

    private fun determineGameUrl(
        currentPack: PartnerPacks?,
        playUrl: String?,
        adUrl: String?
    ): String? {
        if (currentPack == null || currentPack.isInactive) {
            freeGame = YES
            return adUrl
        }
        else {
            freeGame = NO
            return playUrl
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                val webView = binding.wvFaq
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    showGameExitPopup()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            this, // LifecycleOwner
            callback
        )
    }

    private fun setWebView(url: String) {
        if (isNetworkConnected(requireContext())) {
            viewModel.setProgressing(true)
            binding.wvFaq.settings.javaScriptEnabled = true
            binding.wvFaq.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.darkBackground
                )
            )
            binding.wvFaq.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            binding.wvFaq.webChromeClient = MyChrome()
            binding.wvFaq.settings.domStorageEnabled = true
            binding.wvFaq.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    viewModel.setProgressing(true)
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    viewModel.setProgressing(false)
                    super.onPageCommitVisible(view, url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    viewModel.setProgressing(false)
//                    injectCSS()
                    super.onPageFinished(view, url)
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError
                ) {
                    if (request!!.isForMainFrame() && error != null) {
                        showGameUrlError(
                            contentItem.playUrl,
                            contentItem.adPlayUrl
                        )
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    try{
                        request?.url?.let {
                            val uri = Uri.parse(request?.url.toString())
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                            return true
                        }
                    } catch (e: Exception){
                        return false
                    }
                    return false
                }
            }
            binding.wvFaq.loadUrl(url)
        } else {
            onNetworkError("", false)
        }
    }


    inner class MyChrome internal constructor() : WebChromeClient() {
        private var mCustomView: View? = null
        private var mCustomViewCallback: WebChromeClient.CustomViewCallback? = null
        protected var mFullscreenContainer: FrameLayout? = null
        private var mOriginalOrientation = 0
        private var mOriginalSystemUiVisibility = 0
        override fun getDefaultVideoPoster(): Bitmap? {
            return if (mCustomView == null) {
                null
            } else BitmapFactory.decodeResource(
                FacebookSdk.getApplicationContext().getResources(),
                2130837573
            )
        }

        override fun onHideCustomView() {
            (activity?.window?.decorView as FrameLayout).removeView(
                mCustomView
            )
            mCustomView = null
            activity?.getWindow()?.getDecorView()?.setSystemUiVisibility(
                mOriginalSystemUiVisibility
            )
            activity?.setRequestedOrientation(mOriginalOrientation)
            mCustomViewCallback?.onCustomViewHidden()
            mCustomViewCallback = null
        }

        override fun onShowCustomView(
            paramView: View?,
            paramCustomViewCallback: WebChromeClient.CustomViewCallback?
        ) {
            if (mCustomView != null) {
                onHideCustomView()
                return
            }
            mCustomView = paramView
            mOriginalSystemUiVisibility =
                activity?.window?.decorView?.systemUiVisibility ?: View.SYSTEM_UI_FLAG_FULLSCREEN
            mOriginalOrientation =
                activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            mCustomViewCallback = paramCustomViewCallback
            (activity?.window?.decorView as FrameLayout?)?.addView(
                mCustomView,
                FrameLayout.LayoutParams(-1, -1)
            )
            activity?.window?.decorView?.systemUiVisibility =
                3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    private fun getShareIntent(): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        val shareBody = getString(
            R.string.deeplink_games,
            BuildConfig.hostName
        )
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Play games on Tata Play Binge! $shareBody"
        )
        return shareIntent
    }

    private fun setClickListeners(){
        binding.ivClose.setOnClickListener{
            showGameExitPopup()
        }
        binding.ivShare.setSingleOnClick(1000) {
            val sharingIntent = getShareIntent()
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }

        val contentItem = activity?.intent?.extras?.get("contentItem") as ContentItem

        binding.lvGameFav.setSingleOnClick(500) {
            if(!binding.lvGameFav.isAnimating) {
                viewModel.addGameToFav(contentItem)
            }
        }
    }

    private fun showGameExitPopup() {
        showDialog(
            DialogModel(
                false,
                R.drawable.ic_logout,
                getString(R.string.game_exit_warning),
                getString(R.string.yes),
                getString(R.string.no)
            ), object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    binding.wvFaq.destroy()
                    activity?.finish()
                }

                override fun onSecondaryButtonClick() {
                    hideDialog()
                }

                override fun onCloseButtonClick() {
                }

            }
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.wvFaq.destroy()
    }

    private fun showGameUrlError(playUrl: String, adPlayUrl: String) {
        gamesMixpanelInfoModel?.let{it ->
            gameAnalytics.trackGamePlayFailed(
                pageName = it.pageName,
                railTitle = it.railTitle,
                railPosition = it.railPosition,
                railType = it.railType,
                railCategory = it.railCategory,
                gameGenre = it.gameGenre,
                gamePartner = contentItem.provider,
                gamePosition = it.gamePosition,
                gameTitle = contentItem.title,
                freeGame = freeGame,
                releaseYear = it.releaseYear,
                deviceType = PLATFORM_ANDROID_CAPS,
                source = it.source,
                packPrice = sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                packName = sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                gameType = com.tatasky.binge.analytics.EDITORIAL,
                reason = "Url not found"
            )
        }
        showDialog(DialogModel(
            false,
            R.drawable.ic_subscription_error,
            "Unable to play the game at the moment",
            "Try again",
            "Close"
        ), object : CommonDialogEventListener {
            override fun onPrimaryButtonClick() {
                hideDialog()
                setUpWebView()
            }

            override fun onSecondaryButtonClick() {
                hideDialog()
                activity?.onBackPressed()
            }

            override fun onCloseButtonClick() {
                hideDialog()
                activity?.onBackPressed()
            }

        })
    }

    private fun setUpWebView() {
        val gameUrl = determineGameUrl(
            sharedPrefs.getSubscribedPack(),
            contentItem.playUrl,
            contentItem.adPlayUrl
        )
        if (!gameUrl.isNullOrBlank()) {
            setWebView(gameUrl)
        } else {
            showGameUrlError(
                contentItem.playUrl,
                contentItem.adPlayUrl
            )
        }

    }

    override fun toBeCalledOnce() {
        if(!sharedPrefs.getLoginStatus()){
            showToast(requireContext(),"User not logged in")
            activity?.onBackPressed()
        }
        contentItem = activity?.intent?.extras?.get("contentItem") as ContentItem
        gamesMixpanelInfoModel = activity?.intent?.extras?.get("gamesMixpanelInfoModel") as? GamesMixpanelInfoModel
        if(gamesMixpanelInfoModel?.pageName.equals(PROVIDER_GAMEZOP,true)){
            gamesMixpanelInfoModel?.pageName = SOURCE_GAMES
        }
        if(gamesMixpanelInfoModel?.source.equals(PROVIDER_GAMEZOP,true)){
            gamesMixpanelInfoModel?.pageName = SOURCE_GAMES
        }

        if(sharedPrefs.getSubscribedPack() == null || sharedPrefs.getSubscribedPack()?.isInactive == true){
            freeGame = YES
        } else {
            freeGame = NO
        }

        gamesMixpanelInfoModel?.let{it ->
            gameAnalytics.trackGameClick(
                pageName = it.pageName,
                railTitle = it.railTitle,
                railPosition = it.railPosition,
                railType = com.tatasky.binge.analytics.EDITORIAL,
                railCategory = it.railCategory,
                gameGenre = it.gameGenre,
                gamePartner = contentItem.provider,
                gamePosition = it.gamePosition,
                gameRating = it.gameRating,
                gameTitle = contentItem.title,
                freeGame = freeGame,
                releaseYear = it.releaseYear ?: "",
                deviceType = PLATFORM_ANDROID_CAPS,
                source = it.source,
                packPrice = sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                packName = sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM
            )
        }
        viewModel.fetchGamesLastWatchedFavourite(contentItem.id,contentItem.contentType?: TYPE_GAMES)

        setUpWebView()
        setClickListeners()
    }

}