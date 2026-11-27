package com.arin.tube18.data.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import kotlinx.coroutines.flow.Flow

/**
 * Hand-written version of the DAO class Room's code generator would normally create.
 * It uses Room's own runtime helpers, so Flows update automatically when the tables change.
 */
@Suppress("RestrictedApi")
class SavedDao_Impl(private val db: RoomDatabase) : SavedDao {

    private val favoriteInsert = object : EntityInsertAdapter<FavoriteEntity>() {
        override fun createQuery(): String =
            "INSERT OR REPLACE INTO `favorites` (`id`,`title`,`thumbnailUrl`,`duration`,`views`,`savedAt`) " +
                "VALUES (?,?,?,?,?,?)"

        override fun bind(statement: SQLiteStatement, entity: FavoriteEntity) {
            statement.bindText(1, entity.id)
            statement.bindText(2, entity.title)
            val thumb = entity.thumbnailUrl
            if (thumb == null) statement.bindNull(3) else statement.bindText(3, thumb)
            statement.bindText(4, entity.duration)
            statement.bindText(5, entity.views)
            statement.bindLong(6, entity.savedAt)
        }
    }

    private val historyInsert = object : EntityInsertAdapter<HistoryEntity>() {
        override fun createQuery(): String =
            "INSERT OR REPLACE INTO `history` (`id`,`title`,`thumbnailUrl`,`duration`,`views`,`watchedAt`) " +
                "VALUES (?,?,?,?,?,?)"

        override fun bind(statement: SQLiteStatement, entity: HistoryEntity) {
            statement.bindText(1, entity.id)
            statement.bindText(2, entity.title)
            val thumb = entity.thumbnailUrl
            if (thumb == null) statement.bindNull(3) else statement.bindText(3, thumb)
            statement.bindText(4, entity.duration)
            statement.bindText(5, entity.views)
            statement.bindLong(6, entity.watchedAt)
        }
    }

    override fun observeFavorites(): Flow<List<FavoriteEntity>> {
        val sql = "SELECT * FROM favorites ORDER BY savedAt DESC"
        return createFlow(db, false, arrayOf("favorites")) { connection ->
            val stmt = connection.prepare(sql)
            try {
                val cId = getColumnIndexOrThrow(stmt, "id")
                val cTitle = getColumnIndexOrThrow(stmt, "title")
                val cThumb = getColumnIndexOrThrow(stmt, "thumbnailUrl")
                val cDuration = getColumnIndexOrThrow(stmt, "duration")
                val cViews = getColumnIndexOrThrow(stmt, "views")
                val cSavedAt = getColumnIndexOrThrow(stmt, "savedAt")
                val result = mutableListOf<FavoriteEntity>()
                while (stmt.step()) {
                    result.add(
                        FavoriteEntity(
                            id = stmt.getText(cId),
                            title = stmt.getText(cTitle),
                            thumbnailUrl = if (stmt.isNull(cThumb)) null else stmt.getText(cThumb),
                            duration = stmt.getText(cDuration),
                            views = stmt.getText(cViews),
                            savedAt = stmt.getLong(cSavedAt)
                        )
                    )
                }
                result
            } finally {
                stmt.close()
            }
        }
    }

    override fun observeIsFavorite(id: String): Flow<Boolean> {
        val sql = "SELECT EXISTS(SELECT 1 FROM favorites WHERE id = ?)"
        return createFlow(db, false, arrayOf("favorites")) { connection ->
            val stmt = connection.prepare(sql)
            try {
                stmt.bindText(1, id)
                if (stmt.step()) stmt.getLong(0) != 0L else false
            } finally {
                stmt.close()
            }
        }
    }

    override suspend fun insertFavorite(item: FavoriteEntity) {
        performSuspending(db, false, true) { connection ->
            favoriteInsert.insert(connection, item)
        }
    }

    override suspend fun deleteFavorite(id: String) {
        execWithId("DELETE FROM favorites WHERE id = ?", id)
    }

    override suspend fun clearFavorites() {
        execPlain("DELETE FROM favorites")
    }

    override fun observeHistory(): Flow<List<HistoryEntity>> {
        val sql = "SELECT * FROM history ORDER BY watchedAt DESC LIMIT 500"
        return createFlow(db, false, arrayOf("history")) { connection ->
            val stmt = connection.prepare(sql)
            try {
                val cId = getColumnIndexOrThrow(stmt, "id")
                val cTitle = getColumnIndexOrThrow(stmt, "title")
                val cThumb = getColumnIndexOrThrow(stmt, "thumbnailUrl")
                val cDuration = getColumnIndexOrThrow(stmt, "duration")
                val cViews = getColumnIndexOrThrow(stmt, "views")
                val cWatchedAt = getColumnIndexOrThrow(stmt, "watchedAt")
                val result = mutableListOf<HistoryEntity>()
                while (stmt.step()) {
                    result.add(
                        HistoryEntity(
                            id = stmt.getText(cId),
                            title = stmt.getText(cTitle),
                            thumbnailUrl = if (stmt.isNull(cThumb)) null else stmt.getText(cThumb),
                            duration = stmt.getText(cDuration),
                            views = stmt.getText(cViews),
                            watchedAt = stmt.getLong(cWatchedAt)
                        )
                    )
                }
                result
            } finally {
                stmt.close()
            }
        }
    }

    override suspend fun insertHistory(item: HistoryEntity) {
        performSuspending(db, false, true) { connection ->
            historyInsert.insert(connection, item)
        }
    }

    override suspend fun deleteHistory(id: String) {
        execWithId("DELETE FROM history WHERE id = ?", id)
    }

    override suspend fun clearHistory() {
        execPlain("DELETE FROM history")
    }

    private suspend fun execWithId(sql: String, id: String) {
        performSuspending(db, false, true) { connection ->
            val stmt = connection.prepare(sql)
            try {
                stmt.bindText(1, id)
                stmt.step()
            } finally {
                stmt.close()
            }
        }
    }

    private suspend fun execPlain(sql: String) {
        performSuspending(db, false, true) { connection ->
            val stmt = connection.prepare(sql)
            try {
                stmt.step()
            } finally {
                stmt.close()
            }
        }
    }
}
