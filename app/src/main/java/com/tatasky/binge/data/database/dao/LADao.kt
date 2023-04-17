package com.tatasky.binge.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tatasky.binge.data.database.model.LAContentDBModel

@Dao
interface LADao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLAContent(la: LAContentDBModel) : Long

    @Query("SELECT * FROM LAContent where LAContent.contentId = :contentId")
    fun getLAContents(contentId : String): List<LAContentDBModel>

    @Query("DELETE FROM LAContent where LAContent.contentId = :contentId")
    fun deleteLAContent(contentId: String)
}