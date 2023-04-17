package com.tatasky.binge.ui.features.updateprofile

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tatasky.binge.R
import com.tatasky.binge.analytics.NAME
import com.tatasky.binge.data.networking.models.requests.UpdateEmailRequest
import com.tatasky.binge.data.networking.models.response.SubscriberProfileListModel
import com.tatasky.binge.databinding.FragmentEditProfileBinding
import com.tatasky.binge.helper.circularBitmapImageLoad
import com.tatasky.binge.helper.circularImageLoadWithoutCache
import com.tatasky.binge.interfaces.ProfileDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.splash.SplashAnalytics
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.imagepicker.ImagePicker
import com.tatasky.binge.utils.imagepicker.ImagePicker.PERMISSION_REQUEST_CODE
import java.io.File
import javax.inject.Inject

class EditProfileFragment : BaseFragment<FragmentEditProfileBinding, EditProfileViewModel>() {

    private var dthStatus: String? = null
    private var prevEmail: String? = null
    private var prevName: String? = null
    private var isRemovePic: Boolean = false

    @Inject
    lateinit var profileAnalytics: ProfileAnalytics

    @Inject
    lateinit var splashAnalytics: SplashAnalytics

    private val editProfileFragmentArgs : EditProfileFragmentArgs by navArgs<EditProfileFragmentArgs>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(true == savedInstanceState?.containsKey("filePath")){
            ImagePicker.setCameraFilePath(savedInstanceState?.getString("filePath", ""))
        }
        //hideNudges()
        (activity as? LandingActivity)?.hideNudges()
    }
    override fun getViewModelClass(): Class<EditProfileViewModel> {
        return EditProfileViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_edit_profile
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return this
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.etFirstName.et.setOnEditorActionListener { v, actionId, event ->
            binding.etEmail.et.focus()
            true
        }
        /*
        binding.etLastName.et.setOnEditorActionListener { v, actionId, event ->
            binding.etEmail.et.focus()
            true
        }*/
        binding.etEmail.et.setOnEditorActionListener { v, actionId, event ->
            binding.root.closeKeyboard()
            true
        }

        binding.etFirstName.et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.btnUpdateProfile.isEnabled =
                    (!s?.toString()?.trim().equals(prevName)
                            || binding.etEmail.et.text.toString().trim() != prevEmail)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etFirstName.til.clearError()
            }
        })

        binding.etEmail.et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(NON_DTH_USER.equals( dthStatus, true)) {
                    binding.btnUpdateProfile.isEnabled =
                        (!s?.toString()?.trim().equals(prevEmail)
                                || binding.etFirstName.et.text.toString().trim() != prevName)
                }
                else {
                    binding.btnUpdateProfile.isEnabled =
                        !s?.toString()?.trim().equals(prevEmail)

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etEmail.til.clearError()
            }
        })
    }

    override fun setObserver() {
        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                if(it){
                    viewModel.fetchProfileInfo()
                }
            }
        })
        viewModel.getFetchProfileInfo().observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.userData?.let {
                //update User profile
                editProfileFragmentArgs.profileModel.image = it.image
                updateUI(it)
            }
        })
        viewModel.getEditProfileResponse().observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let { response ->
                showToast(context, response.message?:"")
                val selectedProfile = sharedPrefs.getSelectedProfile()
                selectedProfile?.let { it1 ->
                    it1.emailId = binding.etEmail.et.text.toString()
                    if(NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(), true)) {
                        it1.firstName = binding.etFirstName.et.text.toString()
                        it1.lastName = ""
                    }
                    sharedPrefs.setSelectedProfile(it1)
                }
                //NON-dth
                if(binding.isDTHUSer == false)
                    splashAnalytics.updateUserProperty(NAME, binding.etFirstName.et.text.toString())
                splashAnalytics.trackEmailId(binding.etEmail.et.text.toString())
                profileAnalytics.trackUpdateProfile()
                findNavController().popBackStack()
            }
        })

        viewModel.getEditImageProfileResponse().observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let { response ->
                showToast(context, response.message?:"")
                var imageUrl : String? = null
                editProfileFragmentArgs.profileModel.image = imageUrl
                if (!isRemovePic) {
                    editProfileFragmentArgs.profileModel.image = imageUrl
                    imageUrl = response.data?.relativePath
                }
                else{
                    setImage(editProfileFragmentArgs.profileModel)
                    isRemovePic = false
                }
                val selectedProfile = sharedPrefs.getSelectedProfile()
                selectedProfile?.let { it1 ->
                    it1.imageUrl = imageUrl
                    sharedPrefs.setSelectedProfile(it1)
                }
            }
        })
        viewModel.getValidEmail().observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let{validEmail ->
                if(!validEmail){
                    validateEmail()
                }
            }
        })
        viewModel.getValidName().observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let { validName ->
                if (!validName) {
                    validateName()
                }
            }
        })
    }

    override fun toBeCalledOnce() {
        profileAnalytics.trackEditProfileVisit()
        binding.lifecycleOwner = viewLifecycleOwner

        val model : SubscriberProfileListModel.Data = editProfileFragmentArgs.profileModel
        updateUI(model)
        setListeners()
    }

    private fun setListeners() {
        binding.ivCaptureImage.setOnClickListener {
            showCaptureProfilePicAlert()
        }

        binding.tvCancel.setOnClickListener {
            findNavController().navigateUp()

        }

        binding.btnUpdateProfile.setOnClickListener {
            when (sharedPrefs.getDthStatusFreemium()) {
                DTH_W_BINGE_OLD_USER -> {
                    viewModel.updateEmailAddress(createRequest())
                }
                DTH_W_BINGE_NEW_USER -> {
                    viewModel.updateEmailAndName(createRequest(), HEADER_FROM_SETTINGS)
                }
                else -> {
                    if (binding.etFirstName.et.text.toString().isEmpty()
                        && binding.etEmail.et.text.toString().isEmpty()
                    ) {
                        validateName()
                        validateEmail()
                    } else {
                        viewModel.updateEmailAndName(createRequest(), HEADER_FROM_SETTINGS)
                    }
                }
            }
        }
    }

    private fun validateEmail() {
        binding.etEmail.til.error = getString(R.string.error_email)
    }

    private fun validateName() {
        binding.etFirstName.til.error = getString(R.string.error_first_name)
    }

    private fun updateUI(model: SubscriberProfileListModel.Data) {
        model.rmn = sharedPrefs.getClearRMN()
        binding.model = model
        dthStatus = sharedPrefs.getDthStatusFreemium()
        e("setupHeaderData","dthStatus:$dthStatus,")
        binding.isDTHUSer = !NON_DTH_USER.equals( dthStatus, true)
        prevEmail = editProfileFragmentArgs.profileModel.email

        prevName = "${
            editProfileFragmentArgs.profileModel.firstName
                ?: ""
        } ${
            editProfileFragmentArgs.profileModel.lastName
                ?: ""
        }"
        binding.userName = if(prevName?.trim()?.length?:0 >0) prevName else ""
        setImage(editProfileFragmentArgs.profileModel)
    }

    private fun showCaptureProfilePicAlert() {
        showProfilePicAlert(viewModel.profilePicExists,
            binding.root.context, object : ProfileDialogEventListener {
                override fun onRemoveButtonClick() {
                    isRemovePic = true
                    viewModel.removeProfileImage()
                }

                override fun onPrimaryButtonClick() {
                    startActivityForResult(
                        ImagePicker.getPickImageCameraIntent(context!!),
                        ImagePicker.CAMERA_INTENT
                    )
                }

                override fun onSecondaryButtonClick() {
                    ImagePicker.getGalleryImage(requireActivity(), this@EditProfileFragment).apply {
                        if (this != null) {
                            openGallery(this)
                        }
                    }
                }

                override fun onCloseButtonClick() {
                }

            }, viewLifecycleOwner)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //   data?.let {
            if (isNetworkConnected(requireContext())) {
                var newData = Intent()
                if (data != null) {
                    newData = data
                }
                viewModel.setProgressing(true)
                ImagePicker.onActivityResult(requireContext(), requestCode, resultCode, newData, object :
                    ImagePicker.OnImagePicked() {
                    override fun onSuccess(imageFile: File?, bm: Bitmap?) {
                        bm?.let {
                            binding.tvLetter.hide()
                            binding.profileImage.show()
                            circularBitmapImageLoad(binding.profileImage, imageFile!!.absolutePath)
                            viewModel.updateProfileImage(imageFile)
                        }
                    }

                    override fun onError(msg : String?){
                        // Log image Pick failed

                    }

                })
                //  }
            } else {
                onNetworkError("",false)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>
        , grantedResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE ->
                if (grantedResults.isNotEmpty() && grantedResults.get(0) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery(ImagePicker.pickImageGalleryIntent)
                } else {
                    showToast(context, getString(R.string.permission_denied))
                }
        }
    }

    private fun openGallery(intent: Intent) {
        startActivityForResult(
            intent,
            ImagePicker.GALLERY_INTENT
        )
    }

    private fun setImage(selectedProfile: SubscriberProfileListModel.Data) {
        val config = viewModel.sharedPrefs.getConfigResponse()
        e("UpdateProfile","selectedProfile : ${selectedProfile.firstName}," +
                "${selectedProfile.lastName}," +
                "${selectedProfile.email}")
        binding.etEmail.etValue = selectedProfile.email
        binding.etFirstName.etValue = selectedProfile.firstName+" "+selectedProfile.lastName
        binding.tvFirstName.etValue = selectedProfile.firstName+" "+selectedProfile.lastName
        selectedProfile.let {
            if(it.firstName?.isNotBlank() == true)
                binding.tvLetter.text = (it.firstName?.substring(0, 1) ?: "A").toUpperCase()
            if (it.image.isNullOrEmpty() && it.firstName?.isNotBlank() == true) {
                viewModel.profilePicExists = false
                binding.tvLetter.show()
                binding.profileImage.hide()
            } else if(it.image.isNullOrEmpty() && it.firstName.isNullOrEmpty()){
                viewModel.profilePicExists = false
                binding.tvLetter.hide()
                binding.profileImage.show()
                circularImageLoadWithoutCache(binding.profileImage, "", R.drawable.circle_without_gradient_border)
            }else{
                viewModel.profilePicExists = true
                binding.tvLetter.hide()
                binding.profileImage.show()
                val w = dpToPx(requireContext(), 80)
                it.image?.let {
                    val imgUrl = getCloudinaryUrl(
                        viewModel.sharedPrefs.getCloudenieryUrl(),
                        w, w,
                        config?.data?.config?.subscriberImage?.imageBaseUrl + it
                    )
                    e("EditProfileP", "imgUrl $imgUrl")
                    circularImageLoadWithoutCache(binding.profileImage, imgUrl, R.drawable.circle_without_gradient_border)
                }
            }
        }
    }

    private fun createRequest(): UpdateEmailRequest {
        return UpdateEmailRequest(
            binding.etEmail.et.text.toString().trim(),
            editProfileFragmentArgs.profileModel.rmn ?: "",
            subscriberId = viewModel.sharedPrefs.getOriginalSubscriberId() ?: "",
            baId = viewModel.sharedPrefs.getBaId() ?: "",
            binding.etFirstName.et.text.toString().trim()
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("filePath", ImagePicker.getCameraFilePath())
        super.onSaveInstanceState(outState)
    }
}