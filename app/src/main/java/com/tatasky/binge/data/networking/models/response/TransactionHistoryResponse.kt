package com.tatasky.binge.data.networking.models.response

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

class TransactionHistoryResponse : BaseResponse() {
    @SerializedName("data")
    var data: List<TransactionHistoryList>? = null

    internal class TransactionHistoryDeserializer : JsonDeserializer<TransactionHistoryResponse?> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): TransactionHistoryResponse {
            val jObj = json.asJsonObject
            val jElement = jObj["data"]
            var tags: List<TransactionHistoryList>? = Collections.emptyList()
            if (jElement.isJsonArray) {
                tags = context.deserialize<List<TransactionHistoryList>>(jElement.asJsonArray, object : TypeToken<List<TransactionHistoryList>?>() {}.type)
            }
            //assuming there is an appropriate constructor
            return TransactionHistoryResponse().apply {
                code = jObj["code"].asInt
                message = jObj["message"].asString
                data = tags
            }
        }
    }
}