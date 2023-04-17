package com.tatasky.binge.ui.features.dialog

import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.interfaces.ConfettiDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import javax.inject.Inject

class ConfettiDialogViewModel@Inject constructor() : BaseViewModel() {
    private lateinit var mDialogModel: ConfettiDialogModel
    private lateinit var mEventListener: ConfettiDialogEventListener

    fun setDialogModel(dialogModel: ConfettiDialogModel) {
        this.mDialogModel = dialogModel
    }

    fun setEventHandler(eventListener: ConfettiDialogEventListener) {
        mEventListener = eventListener
    }

    fun getDialogModel(): ConfettiDialogModel? = if (::mDialogModel.isInitialized) mDialogModel else null

    fun getEventHandler(): ConfettiDialogEventListener? =
        if (::mEventListener.isInitialized) mEventListener else null

}