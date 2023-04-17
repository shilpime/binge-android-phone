package com.tatasky.binge.ui.features.parentalcontrol.sidemenu.adapter

import com.tatasky.binge.data.networking.models.response.AgeRatingsResponse

interface RatingSelector {
    fun onRatingSelect(value: AgeRatingsResponse.AgeRatings)
}