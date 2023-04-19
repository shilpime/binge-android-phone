package com.tatasky.binge.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tatasky.binge.data.database.model.AppsInBundleModel
import com.tatasky.binge.data.database.model.BundleDBModel
import com.tatasky.binge.data.database.model.CatalogueVersion
import com.tatasky.binge.data.database.model.PackDBModel

/**
 * Created by Srikant Karnani on 27/2/20.
 */

@Dao
interface PacksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPack(pack: PackDBModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBundle(bundlesModel: BundleDBModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAppsInBundle(appsInBundleModel: AppsInBundleModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCatalogueVersion(version: CatalogueVersion)

    @Query("SELECT * FROM CatalogueVersion limit 1")
    fun getCatalogueVersion(): CatalogueVersion?

    @Query("SELECT * FROM Pack where Pack.isFree=1")
    fun getFreePacksList(): List<PackDBModel>

    @Query("SELECT * FROM Pack where Pack.isFree=0")
    fun getPaidPacksList(): List<PackDBModel>

    @Query("SELECT * FROM Pack")
    fun getAllPacksList(): List<PackDBModel>

    @Query("SELECT * FROM Bundles order by Bundles.packPrice")
    fun getAllBundles(): List<BundleDBModel>

    @Query("SELECT * FROM Pack where Pack.packId=:packId")
    fun getPack(packId: String): PackDBModel

    @Query("SELECT * FROM Bundles where Bundles.bundleId=:bundleId")
    fun getBundle(bundleId: String): BundleDBModel

    @Query("SELECT * FROM Pack where Pack.appId=:appId")
    fun getAllPacksInApp(appId: String): List<PackDBModel>

    @Query("select * from Pack where Pack.packId in (select AppsInBundleModel.packId from AppsInBundleModel where AppsInBundleModel.bundleId=:bundleId)")
    fun getAllPacksInBundle(bundleId: String): List<PackDBModel>

}