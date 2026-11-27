package com.arin.tube18.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedDao {
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    fun observeIsFavorite(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavorite(id: String)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()

    @Query("SELECT * FROM history ORDER BY watchedAt DESC LIMIT 500")
    fun observeHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistory(id: String)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}
