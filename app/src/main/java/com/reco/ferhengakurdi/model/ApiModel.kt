package com.reco.ferhengakurdi.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/////
data class GetSimilarModel(
    val phrases: List<PhraseModel>,
    val success: Boolean
)

data class PhraseModel(
    val reverse: Boolean,
    val phrase: String,
    val transliterated: String?,
    val emphStart: Int,
    val emphLength: Int,
    val emphTStart: Int,
    val emphTLength: Int,
    val nonLatin: Boolean
)

/////


data class ApiResponse(
    val status: String,
    val data: DetailScreenModelWrapper
)

data class DetailScreenModelWrapper(
    val total: Int,
    val images: List<DetailScreenListModel>
)

data class DetailScreenListModel(
    val id: String,
    val uploaded_at: String,
    val title: String,
    val display_caption: String,
    val link: String
)
/////


data class GalleryInfo(val title: String, var imageUrl: String, val galleryId: String)


//@Entity(tableName = "gallery_info")
//data class GalleryInfoEntity(
//    @PrimaryKey(autoGenerate = true) val id: Long = 0,
//    val title: String,
//    val imageUrl: String,
//    val galleryId: String
//)

@Entity
data class GalleryInfoEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "gallery_id") val galleryId: String
)
