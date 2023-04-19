package com.tatasky.binge.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "TokenContent"
)
data class TokenContentDBModel(var expiryIn: Long?,
                               var timestamp: Long?,
                               var token: String,
                               @PrimaryKey
                               var contentId: String
)
