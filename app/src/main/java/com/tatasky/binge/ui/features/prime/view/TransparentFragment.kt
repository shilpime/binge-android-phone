package com.tatasky.binge.ui.features.prime.view

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentTransactionHistoryBinding
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseFragment
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseViewModel

class TransparentFragment : CancellationBaseFragment<FragmentTransactionHistoryBinding, CancellationBaseViewModel>() {
	override fun getViewModelClass(): Class<CancellationBaseViewModel> = CancellationBaseViewModel::class.java

	override fun layoutId(): Int = R.layout.fragment_transparent

	override fun getViewModelOwner(): ViewModelStoreOwner = this
	override fun toBeCalledOnce() {
		viewModel.fetchBaIdList(sharedPrefs.getOriginalSubscriberId())
	}

	override fun setObserver() {
		super.setObserver()
		viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
			activity?.finish()
		})
		viewModel.getClosePrimeActivity().observe(viewLifecycleOwner, Observer {
			it.getContentIfNotHandled()?.let {
				activity?.finish()
			}
		})
	}
}