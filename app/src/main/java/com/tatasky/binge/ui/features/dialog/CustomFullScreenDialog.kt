package com.tatasky.binge.ui.features.dialog

import android.app.Dialog
import android.content.Context
import android.view.WindowManager

class CustomFullScreenDialog(context: Context, themeId: Int) : Dialog(context, themeId) {
    override fun show() {
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        // Show the dialog with NavBar hidden.
        super.show();

        // Set the dialog to focusable again.
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }
}