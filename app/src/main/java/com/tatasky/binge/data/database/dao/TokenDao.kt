package com.tatasky.binge.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tatasky.binge.data.database.model.TokenContentDBModel

@Dao
interface TokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTokenContent(la: TokenContentDBModel) : Long

    @Query("SELECT * FROM TokenContent where TokenContent.contentId = :contentId")
    fun getTokenContents(contentId : String): List<TokenContentDBModel>

    @Query("SELECT * FROM TokenContent")
    fun getAllTokenContents(): List<TokenContentDBModel>

    @Query("DELETE FROM TokenContent where TokenContent.contentId = :contentId")
    fun deleteTokenContent(contentId: String)
}