package com.arin.tube18.viewmodel

import android.content.Context
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.SingletonImageLoader
import com.arin.tube18.TubeApp
import com.arin.tube18.data.SavedRepository
import com.arin.tube18.data.prefs.AgePrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val saved: SavedRepository,
    private val agePrefs: AgePrefs
) : ViewModel() {

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun clearCache(context: Context) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            val ok = try {
                val loader = SingletonImageLoader.get(appContext)
                loader.memoryCache?.clear()
                withContext(Dispatchers.IO) {
                    loader.diskCache?.clear()
                    appContext.cacheDir.listFiles()
                        ?.filter { it.name != TubeApp.IMAGE_CACHE_DIR }
                        ?.forEach { it.deleteRecursively() }
                }
                try {
                    val wv = WebView(context)
                    wv.clearCache(true)
                    wv.destroy()
                    WebStorage.getInstance().deleteAllData()
                } catch (e: Exception) {
                    // WebView may be unavailable on some devices; image and file cache are already cleared.
                }
                true
            } catch (e: Exception) {
                false
            }
            _messages.emit(if (ok) "Cache cleared" else "Couldn't clear the whole cache")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            saved.clearHistory()
            _messages.emit("Watch history cleared")
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            saved.clearFavorites()
            _messages.emit("Favourites cleared")
        }
    }

    fun reacceptAge() {
        viewModelScope.launch { agePrefs.setAccepted(false) }
    }
}
