package com.arin.tube18.data.scrape

import com.arin.tube18.data.Blocklist
import com.arin.tube18.data.SiteUrls
import com.arin.tube18.model.Video
import com.arin.tube18.model.VideoDetail
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.Locale

sealed interface ListParse {
    data class Items(val videos: List<Video>) : ListParse
    data object NoResults : ListParse
    data object RegionBlocked : ListParse
    data object AgeWall : ListParse
    data object Unrecognized : ListParse
}

object HtmlParser {

    private val idRegex = Regex("^[A-Za-z0-9_-]{4,40}$")
    private val siteSuffix = Regex("""\s*[-|]\s*Pornhub(\.com)?\s*$""", RegexOption.IGNORE_CASE)

    fun parseList(html: String, finalUrl: String): ListParse {
        val doc = try {
            Jsoup.parse(html, finalUrl)
        } catch (e: Exception) {
            return ListParse.Unrecognized
        }
        val videos = parseCards(doc)
        if (videos.isNotEmpty()) return ListParse.Items(videos)
        return classifyEmpty(doc, html, finalUrl, allowNoResults = true)
    }

    fun classifyEmpty(doc: Document, html: String, finalUrl: String, allowNoResults: Boolean): ListParse {
        val lower = html.lowercase(Locale.ROOT)
        val url = finalUrl.lowercase(Locale.ROOT)
        if (Selectors.regionBlockUrlHints.any { url.contains(it) } ||
            Selectors.regionBlockPhrases.any { lower.contains(it) }
        ) return ListParse.RegionBlocked
        if (allowNoResults && (
                Selectors.noResultsSelectors.any { safeSelectFirst(doc, it) != null } ||
                    Selectors.noResultsPhrases.any { lower.contains(it) })
        ) return ListParse.NoResults
        if (Selectors.ageWallUrlHints.any { url.contains(it) } ||
            Selectors.ageWallPhrases.any { lower.contains(it) }
        ) return ListParse.AgeWall
        if (allowNoResults && Selectors.listContainers.any { safeSelectFirst(doc, it) != null }) {
            return ListParse.NoResults
        }
        return ListParse.Unrecognized
    }

    fun parseCards(root: Element): List<Video> {
        for (selector in Selectors.listItems) {
            val elements = safeSelect(root, selector)
            if (elements.isEmpty()) continue
            val parsed = elements.mapNotNull { parseCard(it) }.distinctBy { it.id }
            if (parsed.isNotEmpty()) return parsed
        }
        // Fallback: build cards around any viewkey link.
        return safeSelect(root, Selectors.viewKeyLinks).mapNotNull { link ->
            val card = link.parents().firstOrNull { it.tagName() == "li" } ?: link.parent() ?: link
            parseCard(card)
        }.distinctBy { it.id }
    }

    fun parseCard(el: Element): Video? {
        return try {
            val id = Selectors.vkeyAttributes
                .firstNotNullOfOrNull { attr -> el.attr(attr).trim().takeIf { idRegex.matches(it) } }
                ?: viewKeyFromHref(el.attr("href"))
                ?: safeSelect(el, Selectors.viewKeyLinks).firstNotNullOfOrNull { viewKeyFromHref(it.attr("href")) }
                ?: return null

            val title = safeSelect(el, Selectors.titleLinks)
                .firstNotNullOfOrNull { it.attr("title").trim().takeIf { t -> t.isNotEmpty() } }
                ?: Selectors.titleTextFallbacks.firstNotNullOfOrNull { sel ->
                    safeSelectFirst(el, sel)?.text()?.trim()?.takeIf { it.isNotEmpty() }
                }
                ?: safeSelect(el, "img[alt]").firstNotNullOfOrNull { it.attr("alt").trim().takeIf { a -> a.isNotEmpty() } }
                ?: return null

            val duration = Selectors.durationSelectors.firstNotNullOfOrNull { sel ->
                safeSelectFirst(el, sel)?.text()?.trim()?.takeIf { it.isNotEmpty() }
            }.orEmpty()

            val views = Selectors.viewSelectors.firstNotNullOfOrNull { sel ->
                safeSelectFirst(el, sel)?.text()?.trim()
                    ?.replace(Regex("(?i)\\s*views?\\s*"), "")
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
            }.orEmpty()

            Video(
                id = id,
                title = title,
                thumbnailUrl = findThumbnail(el),
                duration = duration,
                views = views
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseDetail(html: String, finalUrl: String, id: String, fallback: Video?): VideoDetail {
        val doc = Jsoup.parse(html, finalUrl)
        val rawTitle = Selectors.detailTitle.firstNotNullOfOrNull { sel ->
            safeSelectFirst(doc, sel)?.let { el ->
                val v = if (el.tagName() == "meta") el.attr("content") else el.text()
                v.trim().takeIf { it.isNotEmpty() }
            }
        }
        val hasOgTitle = safeSelectFirst(doc, "meta[property=og:title]") != null
        val tags = parseTags(doc)
        val related = parseRelated(doc, id)

        if (!hasOgTitle && tags.isEmpty() && related.isEmpty()) {
            when (classifyEmpty(doc, html, finalUrl, allowNoResults = false)) {
                ListParse.RegionBlocked -> throw ScrapeException.RegionBlocked()
                ListParse.AgeWall -> throw ScrapeException.AgeWall()
                else -> throw ScrapeException.MarkupChanged("The video page could not be read")
            }
        }

        val title = rawTitle?.replace(siteSuffix, "")?.trim()?.takeIf { it.isNotEmpty() }
            ?: fallback?.title
            ?: id
        val thumb = Selectors.detailThumbnail.firstNotNullOfOrNull { sel ->
            safeSelectFirst(doc, sel)?.attr("content")?.trim()?.takeIf { it.isNotEmpty() }
        }?.let { absolutize(it) } ?: fallback?.thumbnailUrl

        val duration = Selectors.detailDurationMeta.firstNotNullOfOrNull { sel ->
            safeSelectFirst(doc, sel)?.attr("content")?.trim()?.toIntOrNull()
        }?.let { formatSeconds(it) } ?: fallback?.duration.orEmpty()

        val views = Selectors.detailViews.firstNotNullOfOrNull { sel ->
            safeSelectFirst(doc, sel)?.text()?.trim()
                ?.replace(Regex("(?i)\\s*views?\\s*"), "")
                ?.trim()
                ?.takeIf { it.isNotEmpty() && it.any(Char::isDigit) }
        } ?: fallback?.views.orEmpty()

        return VideoDetail(
            video = Video(id, title, thumb, duration, views),
            tags = tags,
            related = related
        )
    }

    private fun parseTags(doc: Document): List<String> {
        val fromLinks = Selectors.tagLinks
            .flatMap { safeSelect(doc, it) }
            .map { it.text().trim() }
        val fromMeta = safeSelectFirst(doc, Selectors.keywordsMeta)?.attr("content")
            ?.split(",")
            ?.map { it.trim() }
            .orEmpty()
        val clean: (List<String>) -> List<String> = { list ->
            list.filter { tag ->
                tag.length in 2..40 &&
                    Selectors.tagIgnore.none { it.equals(tag, ignoreCase = true) } &&
                    !Blocklist.isBlocked(tag)
            }.distinctBy { it.lowercase(Locale.ROOT) }.take(30)
        }
        return clean(fromLinks).ifEmpty { clean(fromMeta) }
    }

    private fun parseRelated(doc: Document, ownId: String): List<Video> {
        val fromContainers = Selectors.relatedContainers
            .mapNotNull { safeSelectFirst(doc, it) }
            .flatMap { parseCards(it) }
        val list = fromContainers.ifEmpty { parseCards(doc) }
        return list
            .filter { it.id != ownId && !Blocklist.isBlocked(it.title) }
            .distinctBy { it.id }
            .take(30)
    }

    private fun findThumbnail(el: Element): String? {
        for (img in safeSelect(el, "img")) {
            for (attr in Selectors.thumbnailAttributes) {
                val v = img.attr(attr).trim()
                if (v.isNotEmpty() && !v.startsWith("data:") && !v.endsWith(".gif")) {
                    return absolutize(v)
                }
            }
        }
        return null
    }

    private fun viewKeyFromHref(href: String): String? {
        if (!href.contains("viewkey=")) return null
        return href.substringAfter("viewkey=")
            .substringBefore("&")
            .substringBefore("#")
            .trim()
            .takeIf { idRegex.matches(it) }
    }

    private fun absolutize(url: String): String = when {
        url.startsWith("//") -> "https:$url"
        url.startsWith("/") -> SiteUrls.BASE + url
        else -> url
    }

    fun formatSeconds(total: Int): String {
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
        else String.format(Locale.US, "%d:%02d", m, s)
    }

    private fun safeSelect(el: Element, selector: String): List<Element> =
        try { el.select(selector) } catch (e: Exception) { emptyList() }

    private fun safeSelectFirst(el: Element, selector: String): Element? =
        try { el.selectFirst(selector) } catch (e: Exception) { null }
}
