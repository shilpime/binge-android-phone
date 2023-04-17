package com.tatasky.binge.ui.features.link_accounts

import android.os.Bundle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentLinkAccountSuccessfulBinding
import com.tatasky.binge.databinding.FragmentLinkAccountsOtpBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment

class LinkAccountsSuccessfulFragment : BaseFragment<FragmentLinkAccountSuccessfulBinding, LinkAccountViewModel>() {

   // var subId:String=""
    private val args by navArgs<LinkAccountsSuccessfulFragmentArgs>()

    override fun getViewModelClass(): Class<LinkAccountViewModel> {
        return LinkAccountViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_link_account_successful
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return this
    }

    override fun setObserver() {
    }

    override fun toBeCalledOnce() {
       binding.subID= args.subID

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack(R.id.action_account_landing,false)
        }

    }

}