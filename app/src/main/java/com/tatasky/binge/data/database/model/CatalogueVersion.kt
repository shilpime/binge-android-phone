package com.tatasky.binge.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Srikant Karnani on 27/2/20.
 */
@Entity
data class CatalogueVersion(@PrimaryKey val id: Int = 1, var version: String)