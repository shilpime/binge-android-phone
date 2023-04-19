package com.tatasky.binge.ui.features.details.bottomsheet

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.navArgs
import com.tatasky.binge.R
import com.tatasky.binge.analytics.util.emptyContentAnalyticsModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.databinding.FragmentDetailEpisodeBottomSheetDialogBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.ui.base.frameworks.base.BaseBottomSheetDialogFragment
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.player.PlayerViewModel
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.expandBottomSheet
import com.tatasky.binge.utils.getCloudinaryUrl
import dagger.android.support.AndroidSupportInjection

class DetailEpisodeBottomSheetDialogFragment: BaseBottomSheetDialogFragment<FragmentDetailEpisodeBottomSheetDialogBinding,PlayerViewModel>(tabSupported = true)  {


    private val args by navArgs<DetailEpisodeBottomSheetDialogFragmentArgs>()
    private var nextEpisode = ContentItem()
    private var previousEpisode = ContentItem()
    private var currentEpisode = ContentItem()
    private lateinit var showProgressBar: Runnable
    private val mHandler = Handler(Looper.getMainLooper())





    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

//    override fun getTheme(): Int {
//        return R.style.AppBottomSheetDialogTheme
//    }

    private fun setListeners(){
        binding.ivEpisode.root.setOnClickListener {
            args.episodeClick.selectedEpisode(
                currentEpisode,
                args.contentAnalyticsModel ?: emptyContentAnalyticsModel()
            )
            dialog?.dismiss()
        }

        binding.tvNotNow.setOnClickListener {
            dialog?.dismiss()
        }

        binding.btnNxtEpisode.setOnClickListener {
            currentEpisode = nextEpisode
            viewModel.fetchNextAndPreviousEpisode(currentEpisode.id)
            refreshData()
        }

        binding.btnPrvEpisode.setOnClickListener {
            currentEpisode = previousEpisode
            viewModel.fetchNextAndPreviousEpisode(currentEpisode.id)
            refreshData()
        }
    }

    private fun refreshData(){
        binding.contentItem = currentEpisode
        binding.tvEpisodeTitle.text =
            "Episode ${currentEpisode.episodeId}: ${currentEpisode.title}"
        val width = context?.let { dpToPx(it, 156) } ?: 156
        val height = context?.let { dpToPx(it, 89) } ?: 89
        val url = getCloudinaryUrl(
            viewModel.getCloudinaryUrl(),
            width, height,
            currentEpisode.getImageItem()
        )
        imageLoad(binding.ivEpisode.image, url)
    }


    override fun getViewModelClass(): Class<PlayerViewModel> {
        return PlayerViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_detail_episode_bottom_sheet_dialog
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return this
    }

    override fun setObserver() {
        viewModel.bottomSheetProgressListener.observe(viewLifecycleOwner, Observer {
            if(it) {
//                dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                activity?.window?.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
                mHandler.postDelayed(
                    showProgressBar,loaderDelayTime
                )

            } else {
                mHandler.removeCallbacks(showProgressBar)
                binding.progressBar.startAvd(false)
//                dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                activity?.window?.clearFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
            }
        })

        viewModel.getNextPreviousEpisodeDetails().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.data?.let{episodeDetails ->
                if (episodeDetails.nextEpisodeExists && episodeDetails.nextEpisode != null) {
                    binding.btnNxtEpisode.show()
                    nextEpisode = episodeDetails.nextEpisode!!
                }else{
                    binding.btnNxtEpisode.invisible()
                }
                if (episodeDetails.previousEpisodeExists && episodeDetails.previousEpisode != null) {
                    binding.btnPrvEpisode.show()
                    previousEpisode = episodeDetails.previousEpisode!!
                }else{
                    binding.btnPrvEpisode.invisible()
                }

            }
        })
    }

    override fun toBeCalledOnce() {
        showProgressBar = Runnable {
            binding.progressBar.startProgressAvd(true)
        }
        expandBottomSheet(dialog)
        currentEpisode = args.contentItem
        viewModel.fetchNextAndPreviousEpisode(currentEpisode.id)
        setListeners()
        refreshData()
    }
}