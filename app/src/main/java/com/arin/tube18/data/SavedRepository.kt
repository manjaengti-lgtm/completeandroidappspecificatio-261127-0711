package com.arin.tube18.data

import com.arin.tube18.data.db.FavoriteEntity
import com.arin.tube18.data.db.HistoryEntity
import com.arin.tube18.data.db.SavedDao
import com.arin.tube18.model.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavedRepository(private val dao: SavedDao) {

    val favorites: Flow<List<Video>> = dao.observeFavorites().map { list ->
        list.map { Video(it.id, it.title, it.thumbnailUrl, it.duration, it.views) }
    }

    val history: Flow<List<Video>> = dao.observeHistory().map { list ->
        list.map { Video(it.id, it.title, it.thumbnailUrl, it.duration, it.views) }
    }

    fun isFavorite(id: String): Flow<Boolean> = dao.observeIsFavorite(id)

    suspend fun addFavorite(v: Video) {
        dao.insertFavorite(FavoriteEntity(v.id, v.title, v.thumbnailUrl, v.duration, v.views, System.currentTimeMillis()))
    }

    suspend fun removeFavorite(id: String) = dao.deleteFavorite(id)

    suspend fun clearFavorites() = dao.clearFavorites()

    suspend fun addHistory(v: Video) {
        dao.insertHistory(HistoryEntity(v.id, v.title, v.thumbnailUrl, v.duration, v.views, System.currentTimeMillis()))
    }

    suspend fun removeHistory(id: String) = dao.deleteHistory(id)

    suspend fun clearHistory() = dao.clearHistory()
}
