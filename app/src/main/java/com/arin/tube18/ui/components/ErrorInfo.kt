package com.arin.tube18.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.ui.graphics.vector.ImageVector
import com.arin.tube18.data.scrape.ScrapeException

data class ErrorInfo(val icon: ImageVector, val title: String, val message: String)

fun describeError(t: Throwable): ErrorInfo = when (t) {
    is ScrapeException.Offline -> ErrorInfo(
        Icons.Filled.WifiOff,
        "You're offline",
        "There's no internet connection. Connect to Wi-Fi or mobile data, then tap Retry."
    )
    is ScrapeException.RegionBlocked -> ErrorInfo(
        Icons.Filled.Public,
        "Blocked in your region",
        "The site refuses to serve content to your location (a country or state block). The app can't get around this. Another network may work."
    )
    is ScrapeException.AgeWall -> ErrorInfo(
        Icons.Filled.Lock,
        "The site asked for its own age check",
        "Instead of videos, the site showed its own age-verification wall, usually because of local law. Tap Retry, or open the site in your browser."
    )
    is ScrapeException.MarkupChanged -> ErrorInfo(
        Icons.Filled.ErrorOutline,
        "Couldn't read the page",
        "The site answered, but no videos could be found in the page. Its layout may have changed. Tap Retry. If it keeps happening, the app needs an update."
    )
    is ScrapeException.Http -> ErrorInfo(
        Icons.Filled.CloudOff,
        when (t.code) {
            404 -> "Not found"
            429 -> "Too many requests"
            in 500..599 -> "The site is having problems"
            else -> "Request refused"
        },
        when (t.code) {
            404 -> "The site answered 404. The video or page may have been removed."
            429 -> "The site is limiting requests right now. Wait a moment and tap Retry."
            403 -> "The site refused the request (403). This can be a regional block. Tap Retry."
            else -> "The site answered with error ${t.code}. Tap Retry."
        }
    )
    is ScrapeException.Network -> ErrorInfo(
        Icons.Filled.CloudOff,
        "Connection problem",
        "Couldn't reach the site (${t.cause?.javaClass?.simpleName ?: "network error"}). Check your connection and tap Retry."
    )
    else -> ErrorInfo(
        Icons.Filled.ErrorOutline,
        "Something went wrong",
        (t.message ?: "Unexpected error.") + " Tap Retry."
    )
}
