package com.tatasky.binge.ui.features.coachmark

import android.app.Activity

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.tatasky.binge.R
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.ui.base.di.AppComponent
import com.tatasky.binge.utils.TextUtils
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.*
import javax.inject.Inject

class CoachMark @Inject constructor(private val coachMarkAnalytics: CoachMarkAnalytics) {

    fun Activity.buildCoachMark(
        coachMarkName: String,
        source: String,
        target: View,
        title: String,
        description: String,
        icon: Int?,
        iconColor: Int?,
        increasePromptBackgroundRadius: Int = 0
    ) {
        val builder = MaterialTapTargetPrompt.Builder(this)
        builder.apply {
            setTarget(target)
            primaryText = title
            secondaryText = description
            setTextGravity(Gravity.END)
            primaryTextTypeface = Typeface.createFromAsset(
                assets,
                getString(
                    R.string.medium_font
                )
            )
            secondaryTextTypeface = Typeface.createFromAsset(
                assets,
                getString(
                    R.string.default_font
                )
            )
            backgroundColour = ContextCompat.getColor(this@buildCoachMark, R.color.launcher_background)
            focalRadius = 60f
            primaryTextColour = ContextCompat.getColor(this@buildCoachMark, R.color.white)
            secondaryTextColour = ContextCompat.getColor(this@buildCoachMark, R.color.white)
            maxTextWidth = 640f
            primaryTextSize = TextUtils.spToPx(16F, this@buildCoachMark)
            secondaryTextSize = TextUtils.spToPx(15F, this@buildCoachMark)
            icon?.let {
                iconDrawable = AppCompatResources.getDrawable(this@buildCoachMark, it)
            }
            iconColor?.let {
                setIconDrawableColourFilter(
                        ContextCompat.getColor(
                            this@buildCoachMark,
                            it
                        )
                )
            }
            promptBackground = DimmedCirclePromptBackground(
                context = this@buildCoachMark,
                increasePromptBackgroundRadiusBy = increasePromptBackgroundRadius
            )
            captureTouchEventOnFocal = true
            captureTouchEventOutsidePrompt = true
            setPromptStateChangeListener { _, state ->
                when (state) {
                    STATE_REVEALED -> coachMarkAnalytics.trackCoachMarkDisplayed(
                        coachMarkName = coachMarkName,
                        source = source,
                        displayCount = 1
                    )
                    STATE_FOCAL_PRESSED -> coachMarkAnalytics.trackCoachMarkIconClick(
                        coachMarkName = coachMarkName,
                        source = source,
                        displayCount = 1
                    )
                    STATE_NON_FOCAL_PRESSED -> coachMarkAnalytics.trackCoachMarkOutsideClick(
                        coachMarkName = coachMarkName,
                        source = source,
                        displayCount = 1
                    )
                }
            }
        }.show()
    }
}