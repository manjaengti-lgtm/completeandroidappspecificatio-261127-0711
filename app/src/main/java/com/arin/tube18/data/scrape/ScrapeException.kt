package com.arin.tube18.data.scrape

sealed class ScrapeException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Offline : ScrapeException("No internet connection")
    class RegionBlocked : ScrapeException("The site is blocked in this region")
    class AgeWall : ScrapeException("The site returned an age-verification wall")
    class MarkupChanged(detail: String = "No videos could be read from the page") : ScrapeException(detail)
    class Http(val code: Int) : ScrapeException("HTTP error $code")
    class Network(cause: Throwable) : ScrapeException("Network error: ${cause.message ?: cause.javaClass.simpleName}", cause)
}
