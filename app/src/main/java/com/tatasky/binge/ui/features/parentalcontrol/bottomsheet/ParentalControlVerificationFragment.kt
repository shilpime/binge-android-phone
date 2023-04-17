package com.tatasky.binge.ui.features.parentalcontrol.bottomsheet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.KeyListener
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tatasky.binge.R
import com.tatasky.binge.analytics.BINGE_MOBILE
import com.tatasky.binge.databinding.FragmentParentalControlVerificationBinding
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
import com.tatasky.binge.utils.navigateSafe
import kotlinx.android.synthetic.main.layout_pin_view.*
import javax.inject.Inject

class ParentalControlVerificationFragment :
    BaseFragment<FragmentParentalControlVerificationBinding, ParentalControlViewModel>(),
    View.OnKeyListener {

    @Inject
    lateinit var loginAnalytics: LoginAnalytics
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private val parentalControlVerificationFragmentArgs by navArgs<ParentalControlVerificationFragmentArgs>()

    override fun getViewModelClass(): Class<ParentalControlViewModel> =
        ParentalControlViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_parental_control_verification

    override fun getViewModelOwner(): ViewModelStoreOwner =
        requireParentFragment().requireParentFragment()

    var keycode_del_triggered=false
    override fun onKey(p0: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        //Handling for deleting OTP and regaining the focus after deletion
        if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent?.action == KeyEvent.ACTION_DOWN/*&& keyEvent?.action == KeyEvent.ACTION_UP*/)
            binding.clEtContainerParentalPinSetup.apply {
                keycode_del_triggered=true
                when(p0) {
                    etPinDig1 -> clearFieldAndFocus(etPinDig1)
                    etPinDig2 -> {
                        if (etPinDig2.text.isNotEmpty())
                            clearFieldAndFocus(etPinDig2)
                        else
                            clearFieldAndFocus(etPinDig1)
                    }
                    etPinDig3 -> {
                        if (etPinDig3.text.isNotEmpty())
                            clearFieldAndFocus(etPinDig3)
                        else
                            clearFieldAndFocus(etPinDig2)
                    }
                    etPinDig4 -> {
                        if (etPinDig4.text.isNotEmpty())
                            clearFieldAndFocus(etPinDig4)
                        else
                            clearFieldAndFocus(etPinDig3)
                    }
                }
            }
        return false
    }

    //Handling for deleting digit fields and regaining the focus after deletion
    private fun clearFieldAndFocus(etOtpDig: EditText) {
        etOtpDig.text.clear()
        etOtpDig.requestFocus()
    }

    override fun setObserver() {
        viewModel.validateParentalPinResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                viewModel.parentalControlBottomDialogResult.postValue(
                    SingleEvent(
                        ParentalControlBottomSheetResultStatus.PIN_VERIFIED
                    )
                )
            }
        }

        viewModel.validateParentalPinError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { errorModel ->
                loginAnalytics.trackPinEntryIncorrect(viewModel.pinEntrySource ?: "", "MOBILE")
                when (errorModel.code) {
                    11004 -> {
                        setError(true, errorModel.message)
                    }
                    else -> {
                        onError(errorModel)
                    }
                }
            }
        }

        viewModel.saveParentalPinResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                viewModel.parentalControlBottomDialogResult.postValue(
                    SingleEvent(
                        ParentalControlBottomSheetResultStatus.SUCCESS_DISMISS
                    )
                )
            }
        }

        viewModel.saveParentalPinError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { errorModel ->
                onError(errorModel)
            }
        }
    }

    private fun setError(enabled: Boolean, errMsg: String? = null) {
        binding.apply {
            tvSubtitleParentalPinSetup.text =
                errMsg ?: getString(R.string.text_subtitle_parental_pin_setup)
            clEtContainerParentalPinSetup.apply {
                etPinDig1.background.level = if (enabled) 1 else 0
                etPinDig2.background.level = if (enabled) 1 else 0
                etPinDig3.background.level = if (enabled) 1 else 0
                etPinDig4.background.level = if (enabled) 1 else 0
            }
            if (!enabled) {
                clEtContainerParentalPinSetup.apply {
                    etPinDig1.text.clear()
                    etPinDig2.text.clear()
                    etPinDig3.text.clear()
                    etPinDig4.text.clear()
                    etPinDig1.requestFocus()
                }
            }
        }
    }

    override fun toBeCalledOnce() {
        when {
            parentalControlVerificationFragmentArgs.isPinChangeRequested || parentalControlVerificationFragmentArgs.isPinResetRequested -> {
                binding.titleParentalPinSetup.text =
                    getString(R.string.title_parental_pin_change)
                binding.groupParentalControlPinVerification.visibility = View.GONE
                binding.btnCancelParentalPin.visibility = View.VISIBLE
            }
            parentalControlVerificationFragmentArgs.isPinSetupRequested -> {
                loginAnalytics.trackCreatePinInitiate(viewModel.getSource(viewModel.actionBeforeOpeningBottomSheet))
                binding.titleParentalPinSetup.text =
                    getString(R.string.title_parental_pin_setup)
                binding.groupParentalControlPinVerification.visibility = View.GONE
                binding.btnCancelParentalPin.visibility = View.VISIBLE
            }
            parentalControlVerificationFragmentArgs.isPinVerificationRequested -> {
                loginAnalytics.trackPinEntryInitiate(viewModel.pinEntrySource ?: "", "MOBILE")
                binding.titleParentalPinSetup.text =
                    getString(R.string.title_parental_pin_verification)
                binding.groupParentalControlPinVerification.visibility = View.VISIBLE
                binding.btnCancelParentalPin.visibility = View.GONE
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*overriding loader set on activity via BaseFragment*/
        showProgress = Runnable { }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnProceedParentalPinSetup.setOnClickListener {
                if (getPin().length == 4) {
                    viewModel.parentalPinValue = getPin()
                    when {
                        parentalControlVerificationFragmentArgs.isPinChangeRequested || parentalControlVerificationFragmentArgs.isPinResetRequested -> {
                            viewModel.saveParentalPin(
                                getPin(),
                                "",
                                false,
                                null
                            )
                        }
                        parentalControlVerificationFragmentArgs.isPinSetupRequested -> {
                            loginAnalytics.trackCreatePinProceed(viewModel.getSource(viewModel.actionBeforeOpeningBottomSheet))
                            viewModel.saveParentalPin(
                                getPin(),
                                "",
                                false,
                                viewModel.ageRatingValue
                            )
                        }
                        parentalControlVerificationFragmentArgs.isPinVerificationRequested -> {
                            loginAnalytics.trackPinEntryProceed(viewModel.pinEntrySource ?: "")
                            viewModel.validateParentalPin(
                                parentalPinValue = getPin()
                            )
                        }
                    }
                }
            }

            btnCancelParentalPinVerification.setOnClickListener {
                loginAnalytics.trackPinEntryNotNow(viewModel.pinEntrySource ?: "", "MOBILE")
                viewModel.parentalControlBottomDialogResult.postValue(
                    SingleEvent(
                        ParentalControlBottomSheetResultStatus.DISMISS
                    )
                )
            }

            btnCancelParentalPin.setOnClickListener {
                loginAnalytics.trackCreatePinCancel(viewModel.getSource(viewModel.actionBeforeOpeningBottomSheet))
                viewModel.parentalControlBottomDialogResult.postValue(
                    SingleEvent(
                        ParentalControlBottomSheetResultStatus.DISMISS
                    )
                )
            }

            btnForgotParentalPinVerification.setOnClickListener {
                loginAnalytics.trackPinEntryForgot(viewModel.pinEntrySource ?: "", "MOBILE")
                viewModel.actionBeforeOpeningBottomSheet = ACTION_PIN_FORGOT
                findNavController().navigateSafe(
                    ParentalControlVerificationFragmentDirections.actionParentalControlVerificationFragmentToParentalControlDialogOtpFragment()
                )
            }

            editTextList= listOf<EditText>(et_pin_dig_1,et_pin_dig_2,et_pin_dig_3,et_pin_dig_4)

            setOtpEditTextHandler()

        }
    }


    private fun getPin(): String {
        val stringBuilder = StringBuilder()
        binding.clEtContainerParentalPinSetup.apply {
            stringBuilder.append(etPinDig1.text)
            stringBuilder.append(etPinDig2.text)
            stringBuilder.append(etPinDig3.text)
            stringBuilder.append(etPinDig4.text)
        }
        return stringBuilder.toString()
    }


    var editTextList : List<EditText> = emptyList()




    fun setOtpEditTextHandler () { //This is the function to be called
        var count=0
        for(et in editTextList){
            val index=count
            et.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    binding.btnProceedParentalPinSetup.isEnabled = getPin().length == 4

                }

                override fun afterTextChanged(p0: Editable?) {
                    if(index == 3 && !et.getText().toString().isEmpty()) {
                        et.clearFocus();
                        binding.root.closeKeyboard()
                    } else if (!et.getText().toString().isEmpty()) {
                        if(index+1<4)
                        editTextList[index+1].requestFocus(); //focuses on the next edittext after a digit is entered.

                    }
                }

            })
            et.setOnKeyListener(object :View.OnKeyListener{
                override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    if (event?.getAction() != KeyEvent.ACTION_DOWN) {
                        return false; //Dont get confused by this, it is because onKeyListener is called twice and this condition is to avoid it.
                    }
                    if(keyCode == KeyEvent.KEYCODE_DEL &&
                        et.getText().toString().isEmpty() && index != 0) {
//this condition is to handel the delete input by users.
                        editTextList[index-1].setText("");//Deletes the digit of OTP
                        editTextList[index-1].requestFocus();//and sets the focus on previous digit
                    }
                    return false;
                }

            })

            et.setOnClickListener { setError(false) }
            count++
        }

    }

}