package com.tatasky.binge.utils

import android.content.Intent
import android.net.Uri
import com.tatasky.binge.R
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.dialog.DialogModel

fun BaseFragment<*, *>?.showAppNotInstalledDialog(provider: String, packageName: String) {
    this?.let {
        showDialog(
            DialogModel(
                cancelable = false,
                title = getString(R.string.app_install_title_placeholder, provider),
                primaryButtonText = getString(R.string.install),
                secondaryButtonText = getString(R.string.cancel),
                text = getString(
                    R.string.hotstar_body_not_installed,
                    sharedPrefs.getClearRMN().lowercase()
                )
            ), object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data =
                            Uri.parse(
                                getString(
                                    R.string.play_store_app_market_url_placeholder,
                                    packageName
                                )
                            )
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        hideDialog()
                    }
                }

                override fun onSecondaryButtonClick() {
                    hideDialog()
                }

                override fun onCloseButtonClick() {
                    hideDialog()
                }
            })
    }
}
