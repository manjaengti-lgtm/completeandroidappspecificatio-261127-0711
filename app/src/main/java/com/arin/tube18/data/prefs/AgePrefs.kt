package com.arin.tube18.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AgePrefs(private val context: Context) {
    private val key = booleanPreferencesKey("age_confirmed")

    val accepted: Flow<Boolean> = context.settingsStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[key] == true }

    suspend fun setAccepted(value: Boolean) {
        context.settingsStore.edit { it[key] = value }
    }
}
