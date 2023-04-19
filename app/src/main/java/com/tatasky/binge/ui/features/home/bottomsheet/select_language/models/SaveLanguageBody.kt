package com.tatasky.binge.ui.features.home.bottomsheet.select_language.models

import com.google.gson.annotations.SerializedName

data class SaveLanguageBody(
    @SerializedName("baId")
    var baId: String = "",
    @SerializedName("bingeSubscriberId")
    var bingeSubscriberId: String = "",
    @SerializedName("languageId")
    var languages: List<String> = mutableListOf(),
    @SerializedName("mobileNumber")
    var mobileNumber: String = ""
)