package com.tatasky.binge.ui.features.details.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentDetailMoreInfoBottomSheetDialogBinding
import com.tatasky.binge.ui.features.details.DetailViewModel
import com.tatasky.binge.utils.expandBottomSheet
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class DetailMoreInfoBottomSheetDialogFragment : BottomSheetDialogFragment() {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: FragmentDetailMoreInfoBottomSheetDialogBinding
    private val moreInfoArgs by navArgs<DetailMoreInfoBottomSheetDialogFragmentArgs>()


    private lateinit var mDetailViewModel: DetailViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.model = moreInfoArgs.meta
        setListeners()
    }

    private fun setListeners(){
        mBinding.tvNotNow.setOnClickListener {
            dialog?.dismiss()
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding =
            FragmentDetailMoreInfoBottomSheetDialogBinding.inflate(inflater, container, false)
        mBinding.lifecycleOwner = viewLifecycleOwner
        mDetailViewModel = ViewModelProvider(
            requireActivity(),
            mViewModelFactory
        )[DetailViewModel::class.java]
//        mBinding.vm = mSelectLanguageBottomSheetViewModel
        return mBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        expandBottomSheet(dialog)
    }

}