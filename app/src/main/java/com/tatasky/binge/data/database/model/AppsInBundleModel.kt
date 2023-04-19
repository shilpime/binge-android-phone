package com.tatasky.binge.data.database.model

import androidx.room.*

/**
 * Created by Srikant Karnani on 27/2/20.
 */

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = BundleDBModel::class,
            parentColumns = ["bundleId"],
            childColumns = ["bundleId"]
        ),
        ForeignKey(
            entity = PackDBModel::class,
            parentColumns = ["packId"],
            childColumns = ["packId"]
        )],
     indices= [Index(value = ["bundleId"]), Index(value = ["packId"])],
    primaryKeys = ["bundleId","packId"]
)
data class AppsInBundleModel(
    @ColumnInfo(name = "bundleId")
    val bundleId: String,
    @ColumnInfo(name = "packId")
    val packId: String
)