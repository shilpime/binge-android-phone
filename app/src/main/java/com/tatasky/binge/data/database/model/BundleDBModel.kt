package com.tatasky.binge.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tatasky.binge.data.database.DBTypeConverter

/**
 * Created by Srikant Karnani on 27/2/20.
 */
@Entity(tableName = "Bundles")
data class BundleDBModel(
    @PrimaryKey
    var bundleId: String,
    var bundleName: String?,
    var packPrice: Float? = Float.MAX_VALUE,
    var bundleType: String?,
    @TypeConverters(DBTypeConverter::class)
    var packsIdList: List<String>
) {

}