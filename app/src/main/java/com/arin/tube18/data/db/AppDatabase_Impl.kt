package com.arin.tube18.data.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlin.reflect.KClass

/**
 * Hand-written version of the class Room's code generator would normally create.
 * Room finds it by name ("AppDatabase_Impl") at runtime, so no KSP step is needed in the build.
 */
@Suppress("RestrictedApi")
class AppDatabase_Impl : AppDatabase() {

    private val savedDaoInstance: Lazy<SavedDao> = lazy { SavedDao_Impl(this) }

    override fun createOpenDelegate(): RoomOpenDelegate {
        return object : RoomOpenDelegate(DB_VERSION, IDENTITY_HASH, IDENTITY_HASH) {
            override fun createAllTables(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `favorites` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                        "`thumbnailUrl` TEXT, `duration` TEXT NOT NULL, `views` TEXT NOT NULL, " +
                        "`savedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `history` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                        "`thumbnailUrl` TEXT, `duration` TEXT NOT NULL, `views` TEXT NOT NULL, " +
                        "`watchedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)"
                )
                connection.execSQL(
                    "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '$IDENTITY_HASH')"
                )
            }

            override fun dropAllTables(connection: SQLiteConnection) {
                connection.execSQL("DROP TABLE IF EXISTS `favorites`")
                connection.execSQL("DROP TABLE IF EXISTS `history`")
            }

            override fun onCreate(connection: SQLiteConnection) {}

            override fun onOpen(connection: SQLiteConnection) {
                internalInitInvalidationTracker(connection)
            }

            override fun onPreMigrate(connection: SQLiteConnection) {
                dropFtsSyncTriggers(connection)
            }

            override fun onPostMigrate(connection: SQLiteConnection) {}

            override fun onValidateSchema(connection: SQLiteConnection): ValidationResult {
                val missing = REQUIRED_TABLES.filterNot { tableExists(connection, it) }
                return if (missing.isEmpty()) {
                    ValidationResult(true, null)
                } else {
                    ValidationResult(false, "Missing tables: ${missing.joinToString()}")
                }
            }
        }
    }

    override fun createInvalidationTracker(): InvalidationTracker {
        val shadowTables = mutableMapOf<String, String>()
        val viewTables = mutableMapOf<String, Set<String>>()
        return InvalidationTracker(this, shadowTables, viewTables, "favorites", "history")
    }

    override fun clearAllTables() {
        super.performClear(false, "favorites", "history")
    }

    override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
        return mutableMapOf(SavedDao::class to emptyList())
    }

    override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
        return mutableSetOf()
    }

    override fun createAutoMigrations(
        autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>
    ): List<Migration> {
        return mutableListOf()
    }

    override fun savedDao(): SavedDao = savedDaoInstance.value

    private companion object {
        const val DB_VERSION = 1
        const val IDENTITY_HASH = "b7f1c0d4a2e94f3c8d6a5b1e0f2c3d41"
        val REQUIRED_TABLES = listOf("favorites", "history")

        fun tableExists(connection: SQLiteConnection, name: String): Boolean {
            val statement = connection.prepare(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?"
            )
            try {
                statement.bindText(1, name)
                return statement.step()
            } finally {
                statement.close()
            }
        }
    }
}
