package com.arin.tube18.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedDao(): SavedDao
}
