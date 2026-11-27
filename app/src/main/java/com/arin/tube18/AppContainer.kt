package com.arin.tube18

import android.content.Context
import androidx.room.Room
import com.arin.tube18.data.SavedRepository
import com.arin.tube18.data.VideoRepository
import com.arin.tube18.data.db.AppDatabase
import com.arin.tube18.data.net.HttpClients
import com.arin.tube18.data.net.NetworkMonitor
import com.arin.tube18.data.prefs.AgePrefs
import okhttp3.OkHttpClient

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val network = NetworkMonitor(appContext)
    val pageHttp: OkHttpClient = HttpClients.createPageClient()
    val imageHttp: OkHttpClient = HttpClients.createImageClient(pageHttp)

    val videos = VideoRepository(pageHttp, network)

    private val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "tube18.db"
    ).build()

    val saved = SavedRepository(database.savedDao())
    val agePrefs = AgePrefs(appContext)
}
