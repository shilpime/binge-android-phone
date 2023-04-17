package com.tatasky.binge.customviews

import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tatasky.binge.databinding.DialogPaymentSuccessfulBinding
import com.tatasky.binge.ui.features.dialog.ConfettiDialogViewModel
import nl.dionsegijn.konfetti.models.Size
import javax.inject.Inject


class ConfettiDialog : CustomConfettiDialog() {

    lateinit var viewModel: ConfettiDialogViewModel
    lateinit var binding: DialogPaymentSuccessfulBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    override fun getRootViewLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = DialogPaymentSuccessfulBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        isCancelable = false
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val display = DisplayMetrics()
        binding.viewKonfetti.build()
            .addColors(Color.parseColor("#FFA800"),
                Color.parseColor("#FFA800"))
            .setDirection(90.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(1000L)
            .addShapes(
                nl.dionsegijn.konfetti.models.Shape.Rectangle(0.5f),
                nl.dionsegijn.konfetti.models.Shape.Circle
            )
            .addSizes(Size(8))
            .setPosition(-50f, 2000f, -50f, -50f)
            .streamFor(250, 1500L);

        viewModel =
            ViewModelProvider(requireActivity(), viewModelFactory).get(ConfettiDialogViewModel::class.java)
        binding.dialogModel = viewModel.getDialogModel().also {
            if (it == null)
                dialog?.dismiss()
        }
        if (viewModel.getDialogModel()?.showSubtitle1 == false) {
            binding.tvSubtitle1.visibility = View.GONE
        }
        binding.btnStartWatching.setOnClickListener {
            try {
                viewModel.getEventHandler()?.onPrimaryButtonClick()
            } catch (e:Exception) {
                dismiss()
            }
        }
    }

    companion object {
        fun newInstance(): ConfettiDialog = ConfettiDialog()
    }

}