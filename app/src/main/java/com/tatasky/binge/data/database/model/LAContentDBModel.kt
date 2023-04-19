package com.tatasky.binge.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigInteger

@Entity(
    tableName = "LAContent"
)
data class LAContentDBModel(
    var contentType: String,
    var timestamp: Long?,
    var learnActionName: String,
    @PrimaryKey
    var contentId: String
) {

}