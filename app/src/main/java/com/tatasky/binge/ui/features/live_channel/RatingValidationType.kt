package com.tatasky.binge.ui.features.live_channel

sealed class RatingValidationType {
    object NonContentRatingValidation: RatingValidationType()
    object ContentRatingValidation: RatingValidationType()
}
