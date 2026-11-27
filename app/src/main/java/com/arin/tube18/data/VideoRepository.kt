package com.arin.tube18.data

import com.arin.tube18.data.net.HttpClients
import com.arin.tube18.data.net.NetworkMonitor
import com.arin.tube18.data.scrape.HtmlParser
import com.arin.tube18.data.scrape.ListParse
import com.arin.tube18.data.scrape.ScrapeException
import com.arin.tube18.data.scrape.Selectors
import com.arin.tube18.model.FeedQuery
import com.arin.tube18.model.Video
import com.arin.tube18.model.VideoDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class PageResult(val items: List<Video>, val endReached: Boolean)

private data class RawPage(val code: Int, val finalUrl: String, val body: String)

class VideoRepository(
    private val client: OkHttpClient,
    private val network: NetworkMonitor
) {
    private val cache = ConcurrentHashMap<String, Video>()

    fun cached(id: String): Video? = cache[id]

    fun remember(video: Video) {
        cache[video.id] = video
    }

    fun isOnline(): Boolean = network.isOnline()

    suspend fun fetchPage(query: FeedQuery, page: Int): PageResult = withContext(Dispatchers.IO) {
        ensureOnline()
        val url = SiteUrls.feedUrl(query, page)
        val mobile = download(url, desktop = false)
        if (mobile.code == 404 && page > 1) return@withContext PageResult(emptyList(), endReached = true)
        throwForStatus(mobile)

        var parsed = HtmlParser.parseList(mobile.body, mobile.finalUrl)
        if (parsed is ListParse.Unrecognized || parsed is ListParse.AgeWall) {
            // The mobile layout may differ; try the desktop layout once.
            val desktop = try { download(url, desktop = true) } catch (e: ScrapeException) { null }
            if (desktop != null && desktop.code < 400) {
                val alt = HtmlParser.parseList(desktop.body, desktop.finalUrl)
                if (alt is ListParse.Items || alt is ListParse.NoResults) parsed = alt
            }
        }

        when (val result = parsed) {
            is ListParse.Items -> {
                val allowed = result.videos.filterNot { Blocklist.isBlocked(it.title) }
                allowed.forEach(::remember)
                PageResult(allowed, endReached = false)
            }
            ListParse.NoResults -> PageResult(emptyList(), endReached = true)
            ListParse.RegionBlocked -> throw ScrapeException.RegionBlocked()
            ListParse.AgeWall -> throw ScrapeException.AgeWall()
            ListParse.Unrecognized ->
                if (page > 1) PageResult(emptyList(), endReached = true)
                else throw ScrapeException.MarkupChanged()
        }
    }

    suspend fun fetchDetail(id: String): VideoDetail = withContext(Dispatchers.IO) {
        ensureOnline()
        val url = SiteUrls.watchUrl(id)
        val fallback = cache[id]
        val first = download(url, desktop = false)
        throwForStatus(first)

        var firstError: Exception? = null
        val primary = try {
            HtmlParser.parseDetail(first.body, first.finalUrl, id, fallback)
        } catch (e: Exception) {
            firstError = e
            null
        }

        val result = if (primary != null && (primary.related.isNotEmpty() || primary.tags.isNotEmpty())) {
            primary
        } else {
            val second = try { download(url, desktop = true) } catch (e: ScrapeException) { null }
            val alt = second?.takeIf { it.code < 400 }?.let {
                try { HtmlParser.parseDetail(it.body, it.finalUrl, id, fallback) } catch (e: Exception) { null }
            }
            val primaryScore = primary?.let { it.related.size + it.tags.size } ?: -1
            when {
                alt != null && alt.related.size + alt.tags.size > primaryScore -> alt
                primary != null -> primary
                else -> throw (firstError ?: ScrapeException.MarkupChanged("The video page could not be read"))
            }
        }
        remember(result.video)
        result.related.forEach(::remember)
        result
    }

    private fun ensureOnline() {
        if (!network.isOnline()) throw ScrapeException.Offline()
    }

    private fun download(url: String, desktop: Boolean): RawPage {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", if (desktop) HttpClients.DESKTOP_UA else HttpClients.MOBILE_UA)
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                return RawPage(response.code, response.request.url.toString(), body)
            }
        } catch (e: IOException) {
            throw if (!network.isOnline()) ScrapeException.Offline() else ScrapeException.Network(e)
        }
    }

    private fun throwForStatus(page: RawPage) {
        if (page.code < 400) return
        val lower = page.body.lowercase(Locale.ROOT)
        if (page.code == 451 || Selectors.regionBlockPhrases.any { lower.contains(it) }) {
            throw ScrapeException.RegionBlocked()
        }
        throw ScrapeException.Http(page.code)
    }
}
