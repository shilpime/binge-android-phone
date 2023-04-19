package com.tatasky.binge.ui.features.details.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentDetailEpisodeBottomSheetDialogBinding
import com.tatasky.binge.databinding.FragmentDetailMoreInfoBottomSheetDialogBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseBottomSheetDialogFragment
import com.tatasky.binge.ui.features.details.DetailViewModel
import com.tatasky.binge.ui.features.player.PlayerViewModel
import com.tatasky.binge.utils.expandBottomSheet
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class DetailMoreInfoBottomSheetDialogFragment : BaseBottomSheetDialogFragment<FragmentDetailMoreInfoBottomSheetDialogBinding, PlayerViewModel>(tabSupported = true)  {
    private val moreInfoArgs by navArgs<DetailMoreInfoBottomSheetDialogFragmentArgs>()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }


    private fun setListeners(){
        binding.tvNotNow.setOnClickListener {
            dialog?.dismiss()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        expandBottomSheet(dialog)
    }

    override fun getViewModelClass(): Class<PlayerViewModel> {
        return PlayerViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_detail_more_info_bottom_sheet_dialog
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return this
    }

    override fun setObserver() {

    }

    override fun toBeCalledOnce() {
        binding.model = moreInfoArgs.meta
        setListeners()
    }

}
