package com.tatasky.binge.ui.features.home.subpage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.RecommendationResponse
import com.tatasky.binge.databinding.FragmentAppSeeAllBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.utils.filterSubscribedUnsubscribedContents
import com.tatasky.binge.utils.navigateSafe

class AppSeeAllFragment : BaseFragment<FragmentAppSeeAllBinding, SeeAllViewModel>() {

    val args by navArgs<AppSeeAllFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            this.duration = 250
        }
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            this.duration = 250
        }
        returnTransition = backward
        reenterTransition = backward
        exitTransition = forward
    }

    override fun setRetainInstance(retain: Boolean) {
        super.setRetainInstance(true)
    }

    override fun getViewModelClass(): Class<SeeAllViewModel> = SeeAllViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_app_see_all

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    override fun toBeCalledOnce() {
        binding.viewModel = viewModel
        viewModel.onlyMessage = false
        //now partner are more than 10
        viewModel.PAGELIMIT = 20
        viewModel.fetchRailData(args.railId.toString(), true, null)
    }

    override fun onError(errorModel: ErrorModel) {
        //super.onError(errorMessage)
        showDialog(
            DialogModel(false, null, errorModel.message, "Ok", null),
            object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    hideDialog()
                    findNavController().navigateUp()
                }

                override fun onCloseButtonClick() {
                    hideDialog()
                    findNavController().navigateUp()
                }

                override fun onSecondaryButtonClick() {
                }
            })
    }

    override fun setObserver() {
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (!viewModel.onlyMessage)
                    findNavController().navigateUp()
            }
        })
        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                if (it) {
                    val uri = getString(R.string.deeplink_app_see_all, BuildConfig.hostName, args.railId, args.title)
                    val notificationIntent =
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(uri),
                            activity?.applicationContext,
                            LandingActivity::class.java
                        )
                    notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(notificationIntent)
                }
            }
        })
        viewModel.getRailResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                viewModel.onlyMessage = true
                setAdapter(response)
            }
        })
        viewModel.getClickedItem().observe(viewLifecycleOwner, Observer
        {
            val contentIfNotHandled = it.getContentIfNotHandled()
            if (contentIfNotHandled != null) {
                findNavController().navigateSafe(
                    AppSeeAllFragmentDirections.actionActionHomeLandingToSubHomeFragment(
                        contentIfNotHandled.contentItem.pageType,//pageType
                        contentIfNotHandled.contentItem.provider,
                        contentIfNotHandled.contentItem.image,
                        contentIfNotHandled.contentItem.partnerId ?: "",
                        contentIfNotHandled.contentItem.contentTitle
                    )
                )
            }
        })
        viewModel.getChangedCount().observe(viewLifecycleOwner, Observer {
            /*if (::endlessScrollListener.isInitialized) {
                it.getContentIfNotHandled()?.let { changedCount ->
                    endlessScrollListener.setTotalEntries(changedCount)
                }
            }*/
        })
    }

    private fun setAdapter(railResponse: RecommendationResponse) {
        binding.clSubscribedView.hide()
        binding.clUnsubscribedView.hide()
        var selectedPartners :Set<String>? = null
        if(sharedPrefs.isActivePack()) {
            selectedPartners = viewModel.sharedPrefs.getPartnerIdsList()
        }
        val appResponse = filterSubscribedUnsubscribedContents(selectedPartners, railResponse.data?.filteredContentItems ?: ArrayList())

        if (railResponse.data?.contentItem!!.size == 0) {
            binding.tvNoData.visibility = View.VISIBLE
            onError(ErrorModel(message = getString(R.string.no_content_available)))
            } else {
            binding.tvNoData.visibility = View.GONE
            binding.clSubscribedView.show()
            if(appResponse.subscribedContent.isNotEmpty())
                binding.tvSubscribeMsg.hide()
            else
                binding.tvSubscribeMsg.show()
            if(appResponse.unsubscribedContent.isNotEmpty())
                binding.clUnsubscribedView.show()
        }
        binding.subscribedRecycler.scrollToPosition(0)
        railResponse.data?.layoutType = ItemLayoutType.APP_RAIL.name

        viewModel.updateAppsList(appResponse)
    }

}