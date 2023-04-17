package com.tatasky.binge.ui.features.update_password

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.databinding.FragmentUpdatePasswordBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.utils.*
import javax.inject.Inject

class UpdatePasswordFragment :
    BaseFragment<FragmentUpdatePasswordBinding, UpdatePasswordViewModel>() {
    private val PASSWORD_MIN_LENGTH = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            this.duration = 500
        }
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            this.duration = 500
        }
        returnTransition = backward
        reenterTransition = backward
        exitTransition = forward
    }

    @Inject
    lateinit var updatePasswordAnalytics: UpdatePasswordAnalytics

    val updatePasswordFragmentArgs by navArgs<UpdatePasswordFragmentArgs>()
    override fun getViewModelClass(): Class<UpdatePasswordViewModel> {
        return UpdatePasswordViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_update_password
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return findNavController().getViewModelStoreOwner(R.id.change_password)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.setMobileNumber(updatePasswordFragmentArgs.rmn)
        binding.vm = viewModel
        binding.etOldPassword.til.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        binding.etNewPassword.til.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        binding.etConfirmPassword.til.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        binding.etConfirmPassword.et.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkAndEnableButton()
                binding.etConfirmPassword.til.isEndIconVisible = s?.isNotEmpty() ?: false
            }
        })
        binding.etNewPassword.et.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkAndEnableButton()
                binding.etNewPassword.til.isEndIconVisible = s?.isNotEmpty() ?: false
            }
        })
        binding.etOldPassword.et.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkAndEnableButton()
                binding.etOldPassword.til.error = null
            }
        })
        binding.etOldPassword.et.setOnEditorActionListener { v, actionId, event ->
            binding.etNewPassword.et.requestFocus()
        }
        binding.etNewPassword.et.setOnEditorActionListener { v, actionId, event ->
            binding.etConfirmPassword.et.requestFocus()
        }
    }

    fun checkAndEnableButton(){
        resetError()
        binding.btnUpdatePassword.isEnabled = (binding.etOldPassword.et.text?.length?:0 )>=PASSWORD_MIN_LENGTH && (binding.etNewPassword.et.text?.length?:0 )>=PASSWORD_MIN_LENGTH && (binding.etConfirmPassword.et.text?.length?:0 )>=PASSWORD_MIN_LENGTH
    }

    override fun setObserver() {
        viewModel.getUpdatePasswordResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { passwordResponse ->
                updatePasswordAnalytics.trackUpdatePasswordSuccess()
                showPasswordUpdatedAlert()
            }
        })
    }

    private fun showPasswordUpdatedAlert() {
        showDialog(
            DialogModel(
                false,
                R.drawable.ic_success_tick,
                getString(R.string.password_update_alert),
                getString(R.string.back_to_account),
                null
            ),
            object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    hideDialog()
                    findNavController().popBackStack(R.id.action_account_landing, false)
                }

                override fun onCloseButtonClick() {
                    hideDialog()
                    findNavController().navigateUp()
                }

                override fun onSecondaryButtonClick() {
                }
            })
    }

    private fun resetError() {
        binding.etConfirmPassword.til.error = null
        binding.etOldPassword.til.error = null
        binding.etNewPassword.til.error = null
    }

    private fun validateForm() {
        binding.root.closeKeyboard()
        resetError()
        var isError = false
        val oldPassword = viewModel.getOldPassword()
        val newPassword = viewModel.getNewPassword()
        val cnfPassword = viewModel.getConfirmPassword()
//        if (viewModel.getIsPasswordCreated()) {
        val errorMessage = getString(R.string.error_password)
        if (oldPassword.isBlank()) {
            binding.etOldPassword.et.error = errorMessage
            isError = true
        }
//        }
        newPassword.isBlank().apply {
            if (this || newPassword.length < PASSWORD_MIN_LENGTH) {
                binding.etNewPassword.til.error = getString(R.string.error_password)
                isError = true
            }
        }
        if (cnfPassword != newPassword) {
            binding.etConfirmPassword.til.error = getString(R.string.error_verify_password)
            isError = true
        }
        if (isError) {
            updatePasswordAnalytics.trackUpdatePasswordFailure(errorMessage)
            return
        }
//        if (viewModel.getIsPasswordCreated()) {
        viewModel.callUpdatePasswordAPI()
//        }
//        else {
//            findNavController().navigateSafe(UpdatePasswordFragmentDirections.actionUpdatePasswordWithoutOTPToOTPAuthenticationFragment())
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.let {
            it.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
    }

    override fun toBeCalledOnce() {
//        if (viewModel.getIsPasswordCreated()) {
//            updatePasswordAnalytics.trackCreatePassword()
//        } else {
        updatePasswordAnalytics.trackUpdatePassword()
//        }
        binding.btnUpdatePassword.setOnClickListener {
            validateForm()
        }
    }

    override fun onError(errorModel: ErrorModel) {
        if(errorModel.statusCode== RESPONSE_CODE_SUCCESS || errorModel.statusCode== CODE_SUCCESS){
            if(errorModel.code == RESPONSE_CODE_WRONG_CURRENT_PASSWORD){
                binding.etOldPassword.til.error = errorModel.message
            } else {
                showDialog(
                    DialogModel(
                        false,
                        R.drawable.ic_subscription_error,
                        errorModel.message,
                        "Ok",
                        null
                    ),
                    object : CommonDialogEventListener {
                        override fun onPrimaryButtonClick() {
                            if (activity != null)
                                hideDialog()
                            else
                                throw IllegalStateException()
                        }

                        override fun onCloseButtonClick() {
                            hideDialog()
                        }

                        override fun onSecondaryButtonClick() {
                        }
                    })
            }
        }
        else {
            showDialog(
                DialogModel(false,
                    R.drawable.ic_subscription_error,
                    errorModel.title,
                    "Ok", null,
                    errorModel.message, errorModel.statusCode),
                object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                    }
                })
        }

    }
}
