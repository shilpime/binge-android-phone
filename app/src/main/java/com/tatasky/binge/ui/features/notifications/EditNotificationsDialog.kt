package com.tatasky.binge.ui.features.notifications

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tatasky.binge.databinding.LayoutEditNotificationDialogBinding
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class EditNotificationsDialog : BottomSheetDialogFragment(){

    lateinit var viewModel: NotificationViewModel
    lateinit var binding: LayoutEditNotificationDialogBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutEditNotificationDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProvider(requireActivity(), viewModelFactory).get(NotificationViewModel::class.java)

   }

    companion object {
        fun newInstance(): EditNotificationsDialog = EditNotificationsDialog()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
    }
}