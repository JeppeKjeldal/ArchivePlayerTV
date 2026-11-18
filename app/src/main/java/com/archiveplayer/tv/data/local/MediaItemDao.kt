package com.archiveplayer.tv.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaItemDao {

    @Query("SELECT * FROM media_items ORDER BY categoryName ASC, title ASC")
    fun getAllMediaItems(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE categoryName = :category ORDER BY title ASC")
    fun getMediaItemsByCategory(category: String): Flow<List<MediaItemEntity>>

    @Query("SELECT DISTINCT categoryName FROM media_items ORDER BY categoryName ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaItemById(id: Int): MediaItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mediaItem: MediaItemEntity): Long

    @Delete
    suspend fun delete(mediaItem: MediaItemEntity)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: Int)
}