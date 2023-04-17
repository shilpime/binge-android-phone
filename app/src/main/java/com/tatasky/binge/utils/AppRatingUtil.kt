package com.tatasky.binge.utils

import android.content.Context
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.android.play.core.review.testing.FakeReviewManager
import com.tatasky.binge.R
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.features.dialog.DialogModel

object AppRatingUtil {
    private val TAG = this.javaClass.simpleName
    private var reviewInfo: ReviewInfo? = null
    private lateinit var manager: ReviewManager

    fun showAppRatingDialog(
        ctx: Context,
        title: String?,
        primaryButtonText: String?,
        secondaryButtonText: String?,
        dialogImage: String?,
        completionLambda: (Boolean) -> Unit
    ) {
        // Init ReviewInfo object early/Pre cache
        initReviewInfoObject(ctx)
        (ctx as? BaseActivity<*>)?.apply {
            showDialog(
                DialogModel(
                    false,
                    R.drawable.ic_binge,
                    title,
                    primaryButtonText,
                    secondaryButtonText
                ),
                object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                        completionLambda(true)
                        reviewInfo?.let {
                            val flow = manager.launchReviewFlow(ctx, it)
                            flow.addOnCompleteListener { _ ->
                                // The flow has finished. The API does not indicate whether the user
                                // reviewed or not, or even whether the review dialog was shown. Thus, no
                                // matter the result, we continue our app flow.
                            }
                        }

                    }

                    override fun onSecondaryButtonClick() {
                        completionLambda(false)
                        hideDialog()

                    }

                    override fun onCloseButtonClick() {
                    }
                }
            )
        }
    }

    private fun initReviewInfoObject(ctx: Context) {
        /**Test without Publishing to Play Store internal/beta/prod track
         * @see FakeReviewManager(Context)
        */
//        manager = FakeReviewManager(ctx)
        manager = ReviewManagerFactory.create(ctx)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            reviewInfo = if (task.isSuccessful) {
                // We got the ReviewInfo object
                task.result

            } else {
                // There was some problem, log or handle the error code.
                d(TAG, task.exception?.message)
                null
            }
        }
    }
}