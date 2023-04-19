package com.tatasky.binge.utils

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.SubscriberIdListResponse
import com.tatasky.binge.interfaces.ProfileDialogEventListener
import com.tatasky.binge.ui.base.frameworks.extensions.show


private var mLoadingdialog: Dialog? = null

fun showLoading(context: Context): Dialog {
    mLoadingdialog = Dialog(context, R.style.DialogTheme)
    mLoadingdialog!!.setContentView(R.layout.view_dialog_loading)
    mLoadingdialog!!.setCancelable(false)
    val loaderView = mLoadingdialog?.findViewById<ImageView>(R.id.progressBar)
    if(loaderView?.drawable is AnimatedVectorDrawable)
        (loaderView.drawable as AnimatedVectorDrawable).start()
    mLoadingdialog!!.show()
    return mLoadingdialog as Dialog
}


fun loadingDismiss() {
    try {
        if (mLoadingdialog != null && mLoadingdialog!!.isShowing()) {
            mLoadingdialog!!.dismiss()
            mLoadingdialog = null
        }
    } catch (e: IllegalArgumentException) {
        e("Catch IllegalArgumentException dialog dismiss", "crash")
    } catch (e: Exception) {
        e.printStackTrace()
        e("Catch dialog dismiss", "crash")
    }
}

fun showProfilePicAlert(profilePicExists : Boolean,
                        verbiage: SubscriberIdListResponse.Settings?,
                        ctx: Context, callBack: ProfileDialogEventListener?,
                        lifecycleOwner: LifecycleOwner) {
    try {

        val view = View.inflate(ctx, R.layout.layout_profile_pic_dialog, null)
        var dialog:Dialog
        if(isTablet(ctx))
            dialog = Dialog(ctx)
        else dialog = BottomSheetDialog(ctx)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val btnCamera = dialog.findViewById<View>(R.id.btn_dialog_primary) as MaterialButton
        val btnGallery = dialog.findViewById<View>(R.id.btn_dialog_secondary) as MaterialButton
        val btnRemove = dialog.findViewById<View>(R.id.btn_remove) as MaterialButton
        val btnCancel = dialog.findViewById<View>(R.id.btn_close) as MaterialButton

        tvDialogTitle?.text = verbiage?.choose
        btnCamera.text = verbiage?.capture
        btnGallery.text = verbiage?.from
        btnCancel.text = verbiage?.close
        if(profilePicExists){
            btnRemove.show()
            btnRemove.text = verbiage?.remove
        }
        btnCamera.setOnClickListener {
            dialog.dismiss()
            try{
                callBack?.onPrimaryButtonClick()
            } catch (e:Exception){ }
        }

        btnGallery.setOnClickListener {
            dialog.dismiss()
            try{
                callBack?.onSecondaryButtonClick()
            } catch (e:Exception){ }
        }

        btnRemove.setOnClickListener {
            dialog.dismiss()
            try{
                callBack?.onRemoveButtonClick()
            } catch (e:Exception){ }
        }


        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && dialog.isShowing) {
                dialog.dismiss()
            }
        }
        lifecycleOwner.apply { this.lifecycle.addObserver(lifecycleObserver) }
        dialog.setOnDismissListener { lifecycleOwner.apply { this.lifecycle.removeObserver(lifecycleObserver) } }
        if (!dialog.isShowing) {
            dialog.show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        e("Exception ", e.toString())

    }
}
