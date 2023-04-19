package com.tatasky.binge.ui.features.details

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.tatasky.binge.R
import com.tatasky.binge.ui.features.home.PlayButtonType
import com.tatasky.binge.utils.CONSTANT_50
import com.tatasky.binge.utils.DETAILS_LAYOUT_WEIGHT_1F
import com.tatasky.binge.utils.DETAILS_LAYOUT_WEIGHT_2F
import com.tatasky.binge.utils.DETAILS_LAYOUT_WEIGHT_3F

fun DetailsFragment.setPlayButtonsWeight(linearLayout:LinearLayout,buttonType: PlayButtonType){
    var endMargin=0
    var weightOfPlayButton=DETAILS_LAYOUT_WEIGHT_1F
    when(buttonType){
        PlayButtonType.PLAY_BUTTON_WITH_TRAILER_PORTRAIT->{
            weightOfPlayButton=DETAILS_LAYOUT_WEIGHT_2F
        }
        PlayButtonType.PLAY_BUTTON_WITH_TRAILER_LANDSCAPE->{
            weightOfPlayButton=DETAILS_LAYOUT_WEIGHT_3F
        }
        PlayButtonType.MATCH_PLAY_BUTTON->{
            weightOfPlayButton=DETAILS_LAYOUT_WEIGHT_1F
        }
        PlayButtonType.CENTER_PLAY_BUTTON->{
            endMargin=linearLayout.context?.resources?.getDimensionPixelSize(R.dimen.margin_40dp) ?: CONSTANT_50
            weightOfPlayButton=DETAILS_LAYOUT_WEIGHT_3F
        }
    }
    linearLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        setMargins(0, 0, endMargin, 0) //parameters are in pixel
    }
    linearLayout.weightSum = weightOfPlayButton
}

