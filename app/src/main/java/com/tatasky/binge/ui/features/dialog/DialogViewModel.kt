package com.tatasky.binge.ui.features.dialog

import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import javax.inject.Inject

/**
 * Created by Srikant Karnani on 20/12/19.
 */
class DialogViewModel @Inject constructor() : BaseViewModel() {
    private lateinit var mDialogModel: DialogModel
    private lateinit var mEventListener: CommonDialogEventListener

    fun setDialogModel(dialogModel: DialogModel) {
        this.mDialogModel = dialogModel
    }

    fun setEventHandler(eventListener: CommonDialogEventListener) {
        mEventListener = eventListener
    }

    fun getDialogModel(): DialogModel? = if (::mDialogModel.isInitialized) mDialogModel else null

    fun getEventHandler(): CommonDialogEventListener? =
        if (::mEventListener.isInitialized) mEventListener else null

}