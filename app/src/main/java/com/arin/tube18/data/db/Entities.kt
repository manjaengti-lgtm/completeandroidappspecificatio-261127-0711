package com.arin.tube18.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val duration: String,
    val views: String,
    val savedAt: Long
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val duration: String,
    val views: String,
    val watchedAt: Long
)
