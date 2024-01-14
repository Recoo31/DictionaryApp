package com.reco.ferhengakurdi.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GalleryInfoDao {
    @Insert
    suspend fun insertAll(galleryList: List<GalleryInfoEntity>)

    @Query("SELECT * FROM GalleryInfoEntity")
    suspend fun getAll(): List<GalleryInfoEntity>
}

