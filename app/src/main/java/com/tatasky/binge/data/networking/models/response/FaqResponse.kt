package com.tatasky.binge.data.networking.models.response

import android.text.SpannedString
import android.text.TextUtils
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.google.gson.annotations.SerializedName

class FaqResponse : BaseResponse() {

    @SerializedName("data")
    var data: ArrayList<Question>? = null


//    data class Data(
//        @SerializedName("faqs", alternate = ["faq"])
//        var faqContent: FaqContent? = null
//
//    )
//    class FaqContent{
//        @SerializedName("content")
//        var questions: ArrayList<Question>? = null
//    }

    class Question {
        @SerializedName("answer")
        var answer: String? = null

        @SerializedName("question")
        var question: String? = null

    }

}