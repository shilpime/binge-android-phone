package com.tatasky.binge.ui.features.common
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.tatasky.binge.R
import com.tatasky.binge.databinding.LayoutAppupdateDialogBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.features.dialog.DialogModel
import dagger.android.support.DaggerDialogFragment

class AppUpdateDialog() : DaggerDialogFragment() {

    private lateinit var binding: LayoutAppupdateDialogBinding
    private var listener: CommonDialogEventListener? = null
    private var imgUrl: String? = null
    private var dialogModel: DialogModel? = null

    private constructor(listener: CommonDialogEventListener, dialogModel: DialogModel?, imgUrl: String?) : this() {
        this.listener = listener
        this.imgUrl = imgUrl
        this.dialogModel = dialogModel
    }

    override fun show(manager: FragmentManager, tag: String?) {
        manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutAppupdateDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        imgUrl?.takeIf { it.isNotBlank() }?.let {
            imageLoad(binding.ivDialog, it)
        }?: binding.ivDialog.hide()
        binding.dialogModel = dialogModel
        isCancelable = false
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.btnDialogPrimary.setOnClickListener {
            try {
                listener?.onPrimaryButtonClick()
            } catch (e: Exception) {
            }
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            try {
                listener?.onSecondaryButtonClick()
            } catch (e: Exception) {
            }
            dismiss()
        }
    }

    companion object {
        fun newInstance(
            listener: CommonDialogEventListener,
            dialogModel: DialogModel?,
            imgUrl: String?
        ): AppUpdateDialog =
            AppUpdateDialog(listener, dialogModel, imgUrl)
    }
}