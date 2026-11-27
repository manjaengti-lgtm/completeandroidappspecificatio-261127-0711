package com.arin.tube18.data

import com.arin.tube18.model.FeedQuery
import com.arin.tube18.model.SearchChip
import okhttp3.HttpUrl.Companion.toHttpUrl

object SiteUrls {
    const val BASE = "https://www.pornhub.com"

    fun feedUrl(query: FeedQuery, page: Int): String {
        val search = query.search?.trim().orEmpty()
        val builder = if (search.isEmpty()) {
            "$BASE/video".toHttpUrl().newBuilder().also { b ->
                when (query.chip) {
                    SearchChip.AMATEUR -> b.addQueryParameter("c", "3")
                    SearchChip.HD -> b.addQueryParameter("c", "38")
                    SearchChip.POPULAR -> b.addQueryParameter("o", "mv")
                    SearchChip.NEWEST -> b.addQueryParameter("o", "cm")
                    null -> Unit
                }
            }
        } else {
            val term = if (query.chip == SearchChip.AMATEUR) "$search amateur" else search
            "$BASE/video/search".toHttpUrl().newBuilder()
                .addQueryParameter("search", term)
                .also { b ->
                    when (query.chip) {
                        SearchChip.HD -> b.addQueryParameter("hd", "1")
                        SearchChip.POPULAR -> b.addQueryParameter("o", "mv")
                        SearchChip.NEWEST -> b.addQueryParameter("o", "mr")
                        SearchChip.AMATEUR, null -> Unit
                    }
                }
        }
        builder.addQueryParameter("page", page.toString())
        return builder.build().toString()
    }

    fun embedUrl(id: String) = "$BASE/embed/$id"

    fun watchUrl(id: String) = "$BASE/view_video.php?viewkey=$id"
}
