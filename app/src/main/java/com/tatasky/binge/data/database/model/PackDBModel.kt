package com.tatasky.binge.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Srikant Karnani on 27/2/20.
 */

@Entity(
    tableName = "Pack"
)
data class PackDBModel(
    var appId: String,
    var name: String?,
    var price: Float? = Float.MAX_VALUE,
    var packName: String?,
    var productType: String?,
    var summary: String?,
    var isFree: Boolean = false,
    var iconUrl: String?,
    @PrimaryKey
    var packId: String
) {

}