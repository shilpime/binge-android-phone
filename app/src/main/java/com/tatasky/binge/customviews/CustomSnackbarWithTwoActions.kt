package com.tatasky.binge.customviews

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.tatasky.binge.R
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.utils.BindingAdapters.Companion.setHtmlText
import com.tatasky.binge.utils.CustomSnackbarWithTwoActionsType
import com.tatasky.binge.utils.d
import com.tatasky.binge.utils.dpToPx

@SuppressLint("ClickableViewAccessibility")
class CustomSnackbarWithTwoActions(
    parent: ViewGroup,
    content: CustomSnackbarWithTwoActionsView,
) : BaseTransientBottomBar<CustomSnackbarWithTwoActions>(parent, content, content) {

    init {
        getView().setBackgroundColor(
            ContextCompat.getColor(
                view.context,
                android.R.color.transparent
            )
        )
        val lp = getView().layoutParams
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        getView().layoutParams = lp
        getView().setOnTouchListener { _, _ -> false }
    }

    companion object {
        fun make(
            viewGroup: ViewGroup,
            snackbarType: CustomSnackbarWithTwoActionsType,
            mszTitle: String,
            mszDesc: String,
            imgResourceSmall: Int? = null,
            imgResourceLarge: Int? = null,
            imgResourceCancel: Int? = null,
            btnText: String,
            maxProgress: Int,
            currProgress: Int,
            lambdaAction: (() -> Unit)? = null,
            lambdaCancel: (() -> Unit)? = null,
            baseActivity: BaseActivity<*>,
        ): CustomSnackbarWithTwoActions {
            val customView = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.layout_custom_snackbar_with_two_actions,
                viewGroup,
                false
            ) as CustomSnackbarWithTwoActionsView

            val groupProgress =
                customView.findViewById<Group>(R.id.group_progress_snackbar_custom_with_two_actions)
            val groupIvLarge =
                customView.findViewById<Group>(R.id.group_iv_large_snackbar_custom_with_two_actions)
            val imageView =
                customView.findViewById<ImageView>(R.id.iv_snackbar_custom_with_two_actions)
            val tvDesc =
                customView.findViewById<TextView>(R.id.tv_desc_snackbar_custom_with_two_actions)
            val tvTitle =
                customView.findViewById<TextView>(R.id.tv_title_snackbar_custom_with_two_actions)

            val gameNudge =
                customView.findViewById<ConstraintLayout>(R.id.snackbar_game_nudge)

            val packNudge =
                customView.findViewById<ConstraintLayout>(R.id.cl_snackbar_custom_with_two_actions)
            gameNudge.visibility = View.GONE
            packNudge.visibility = View.VISIBLE
            when (snackbarType) {
                is CustomSnackbarWithTwoActionsType.SnackbarTypeRegionalApps ->{
                    val regionalAppNudge =
                        customView.findViewById<ConstraintLayout>(R.id.snackbar_regional_apps)
                    val config : Configuration = regionalAppNudge.context.resources.configuration
                    val regionalAppTitle = regionalAppNudge.findViewById<TextView>(R.id.tv_regional_app_nudge_title)
                    val regionalAppSubtitle = regionalAppNudge.findViewById<TextView>(R.id.tv_regional_app_nudge_subtitle)
                    regionalAppNudge.visibility = View.VISIBLE
                    packNudge.visibility = View.GONE
                    regionalAppTitle.text = mszTitle
                    regionalAppSubtitle.text = btnText
                    regionalAppNudge.findViewById<TextView>(R.id.tv_regional_app_nudge_subtitle).setOnClickListener {
                        lambdaAction?.invoke()
                    }
                    regionalAppNudge.findViewById<ImageView>(R.id.iv_close).setOnClickListener {
                        lambdaCancel?.invoke()
                    }
                    d("SmallestScreenWidthDp", "make: ${config.smallestScreenWidthDp}")
                    if(config.smallestScreenWidthDp <=370){
                        val titleParams: ViewGroup.MarginLayoutParams =
                            regionalAppTitle.layoutParams as ViewGroup.MarginLayoutParams
                        titleParams.marginStart = dpToPx(regionalAppTitle.context,8)

                        val bodyParams:  ViewGroup.MarginLayoutParams =
                            regionalAppSubtitle.layoutParams as ViewGroup.MarginLayoutParams
                        bodyParams.marginStart = dpToPx(regionalAppSubtitle.context,8)

                        val imageViewParams:  ViewGroup.MarginLayoutParams =
                            gameNudge.findViewById<ImageView>(R.id.iv_nudge).layoutParams as ViewGroup.MarginLayoutParams
                        imageViewParams.marginStart = dpToPx(regionalAppSubtitle.context,12)
                    }

                }
                is CustomSnackbarWithTwoActionsType.SnackbarTypeGameNudge -> {
                    val config : Configuration = gameNudge.context.resources.configuration
                    val gameNudgeTitle = gameNudge.findViewById<TextView>(R.id.tv_game_nudge_title)
                    val gameNudgeSubtitle = gameNudge.findViewById<TextView>(R.id.tv_game_nudge_subtitle)
                    gameNudge.visibility = View.VISIBLE
                    packNudge.visibility = View.GONE
                    gameNudgeTitle.text = mszTitle
                    gameNudgeSubtitle.setHtmlText(mszDesc)
                    gameNudge.findViewById<TextView>(R.id.btn_game_action).setOnClickListener {
                        lambdaAction?.invoke()
                    }
                    gameNudge.findViewById<ImageView>(R.id.iv_close).setOnClickListener {
                        lambdaCancel?.invoke()
                    }
                    d("SmallestScreenWidthDp", "make: ${config.smallestScreenWidthDp}")
                    if(config.smallestScreenWidthDp <=370){
                        val titleParams: ViewGroup.MarginLayoutParams =
                            gameNudgeTitle.layoutParams as ViewGroup.MarginLayoutParams
                        titleParams.marginStart = dpToPx(gameNudgeTitle.context,8)

                        val bodyParams:  ViewGroup.MarginLayoutParams =
                            gameNudgeSubtitle.layoutParams as ViewGroup.MarginLayoutParams
                        bodyParams.marginStart = dpToPx(gameNudgeSubtitle.context,8)

                        val imageViewParams:  ViewGroup.MarginLayoutParams =
                            gameNudge.findViewById<ImageView>(R.id.iv_nudge).layoutParams as ViewGroup.MarginLayoutParams
                        imageViewParams.marginStart = dpToPx(gameNudgeSubtitle.context,12)
                    }

                }
                is CustomSnackbarWithTwoActionsType.SnackbarTypeNormalSizeImage -> {
                    groupProgress.visibility = View.GONE
                    groupIvLarge.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                    tvDesc.visibility = View.GONE
                    tvTitle.textSize = 14f

                    customView.findViewById<ImageView>(R.id.iv_snackbar_custom_with_two_actions)
                        .apply {
                            adjustViewBounds
                            imgResourceSmall?.let { setImageResource(it) }
                        }
                }
                is CustomSnackbarWithTwoActionsType.SnackbarTypeLargeSizeImage -> {
                    groupProgress.visibility = View.GONE
                    groupIvLarge.visibility = View.VISIBLE
                    imageView.visibility = View.VISIBLE
                    tvDesc.visibility = View.GONE
                    tvTitle.textSize = 14f

                    customView.findViewById<ImageView>(R.id.iv_large_snackbar_custom_with_two_actions)
                        .apply {
                            adjustViewBounds
                            imgResourceLarge?.let { setImageResource(it) }
                        }
                }
            }

            customView.findViewById<TextView>(R.id.btn_action_snackbar_custom_with_two_actions)
                .apply {
                    text = btnText
                    setOnClickListener { lambdaAction?.invoke() }
                }

            customView.findViewById<ImageView>(R.id.iv_close_snackbar_custom_with_two_actions)
                .apply {
                    adjustViewBounds
                    imgResourceCancel?.let { setImageResource(it) }
                    setOnClickListener { lambdaCancel?.invoke() }
                }
            tvTitle.apply {
                text = mszTitle
                typeface =
                    Typeface.createFromAsset(
                        viewGroup.context.assets,
                        viewGroup.context.getString(
                            R.string.medium_font
                        )
                    )
            }
            return CustomSnackbarWithTwoActions(viewGroup, customView)
        }

        fun makeNewStyleNudge(
            viewGroup: ViewGroup,
            snackbarType: CustomSnackbarWithTwoActionsType,
            nudgeTitle: String,
            nudgeDescription: String,
            imgResource: Int? = null,
            imgResourceCancel: Int?,
            positiveBtnText: String,
            maxProgress: Int,
            currProgress: Int,
            lambdaAction: (() -> Unit)? = null,
            lambdaCancel: (() -> Unit)? = null
        ): CustomSnackbarWithTwoActions {
            val customView = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.layout_custom_snackbar_with_two_actions,
                viewGroup,
                false
            ) as CustomSnackbarWithTwoActionsView
            val nudge = customView.setupNewNudgeStyle(
                nudgeTitle,
                nudgeDescription,
                positiveBtnText,
                imgResource,
                imgResourceCancel,
                lambdaAction,
                lambdaCancel
            )
            when (snackbarType) {
                is CustomSnackbarWithTwoActionsType.SnackbarTypeSubsExpired ->
                    nudge?.apply {
                        findViewById<Group>(R.id.progressBarGroup).hide()
                        findViewById<ImageView>(R.id.icon_IV).show()
                    }
                is CustomSnackbarWithTwoActionsType.SnackbarTypeSubsRenewal ->
                    nudge?.apply {
                        findViewById<Group>(R.id.progressBarGroup).show()
                        findViewById<ImageView>(R.id.icon_IV).hide()
                        findViewById<ProgressBar>(R.id.remainingProgress_PB)?.let {
                            it.apply {
                                max = maxProgress
                                progress = currProgress
                            }
                        }
                        findViewById<TextView>(R.id.remainingCount_TV)?.let {
                            it.apply {
                                text = currProgress.toString()
                            }
                        }
                        findViewById<TextView>(R.id.remainingCountUnit_TV)?.let {
                            it.apply {
                                text = resources.getString(
                                    if (currProgress > 1) R.string.days
                                    else R.string.nudge_day
                                )
                            }
                        }
                    }
                else -> {}
            }
            return CustomSnackbarWithTwoActions(viewGroup, customView)
        }
    }
}

private fun CustomSnackbarWithTwoActionsView.setupNewNudgeStyle(
    nudgeTitle: String,
    nudgeDesc: String,
    actionBtnTxt: String,
    imgResource: Int?,
    imgResourceCancel: Int?,
    lambdaAction: (() -> Unit)?,
    lambdaCancel: (() -> Unit)?
): MaterialCardView? {
    val nudge =
        findViewById<MaterialCardView>(R.id.snackbar_new_design)
    val title = nudge.findViewById<TextView>(R.id.nudgeTitle_TV)
    val description = nudge.findViewById<TextView>(R.id.nudgeDescription_TV)
    val image = nudge.findViewById<ImageView>(R.id.icon_IV)
    val actionBtn = nudge.findViewById<TextView>(R.id.nudgeAction_Btn)
    val closeBtn = nudge.findViewById<ImageView>(R.id.close_IV)
    nudge.visibility = View.VISIBLE
    title.text = nudgeTitle
    description.text = nudgeDesc
    imgResource?.let {
        image.setImageResource(it)
    }
    actionBtn.apply {
        text = actionBtnTxt
        setOnClickListener { lambdaAction?.invoke() }
    }
    closeBtn.apply {
        imgResourceCancel?.let { setImageResource(it) }
        setOnClickListener { lambdaCancel?.invoke() }
    }
    return nudge
}