package com.tatasky.binge.ui.features.dialog

data class ConfettiDialogModel (
    val imageId: Int?=null,
    val title: String?,
    val primaryButtonText: String?,
    val secondaryText: String?,
    val primaryText: String?=null,
    val showSubtitle1 : Boolean = true
)