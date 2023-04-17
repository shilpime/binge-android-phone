package com.tatasky.binge.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tatasky.binge.data.database.dao.LADao
import com.tatasky.binge.data.database.dao.PacksDao
import com.tatasky.binge.data.database.dao.TokenDao
import com.tatasky.binge.data.database.model.*
import com.tatasky.binge.utils.e


/**
 */
@Database(
    entities = [PackDBModel::class, BundleDBModel::class, AppsInBundleModel::class, CatalogueVersion::class,
        LAContentDBModel::class, TokenContentDBModel::class],
    version = AppDatabase.VERSION,
    exportSchema = false
)
@TypeConverters(
    DBTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    init {

        e("SingletonModule", "inside AppDatabase")
    }

    abstract val packsDao: PacksDao
    abstract val laDao : LADao
    abstract val tokenDao : TokenDao

    companion object {
        const val VERSION = 2

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Since we didn't alter the table, there's nothing else to do here.
                // Create the new table
                database.execSQL(
                    "CREATE TABLE TokenContent (expiryIn INTEGER, timestamp INTEGER, token TEXT NOT NULL," +
                            " contentId TEXT NOT NULL, " +
                            "PRIMARY KEY(contentId))");
            }
        }
    }

}