package com.arin.tube18.data.scrape

/**
 * Every CSS selector and detection phrase lives here so the scraper can be
 * fixed in one place when the site changes its markup. Each list is tried in order.
 */
object Selectors {

    /** Video cards on list pages, most specific first. */
    val listItems = listOf(
        "#videoSearchResult li.pcVideoListItem",
        "#videoCategory li.pcVideoListItem",
        "ul.videos li.pcVideoListItem",
        "li.pcVideoListItem",
        "li.videoBox",
        "li[data-video-vkey]",
        "div[data-video-vkey]",
        "li.videoWrapper",
        "div.videoWrapper"
    )

    /** Last-resort: any link that carries a viewkey. */
    const val viewKeyLinks = "a[href*=viewkey=]"

    val vkeyAttributes = listOf("data-video-vkey", "data-vkey")

    val titleLinks = "a[title]"
    val titleTextFallbacks = listOf(".title a", "span.title a", ".title", "a.title")

    val durationSelectors = listOf("var.duration", ".duration", ".marker-overlays var", ".time")
    val viewSelectors = listOf(".views var", ".views", ".videoViews", ".viewsCount")

    val thumbnailAttributes = listOf("data-mediumthumb", "data-src", "data-thumb_url", "data-image", "src")

    /** Containers that indicate a real (but possibly empty) result list. */
    val listContainers = listOf("#videoSearchResult", "#videoCategory", "ul.videos.search-video-thumbs")

    val noResultsSelectors = listOf("#noResultsWrapper", ".noResultsWrapper", ".noResults", ".no-results")
    val noResultsPhrases = listOf(
        "no results found",
        "returned no results",
        "did not match any",
        "no videos found",
        "we couldn't find any"
    )

    val regionBlockUrlHints = listOf("/blocked", "geoblock", "georestrict")
    val regionBlockPhrases = listOf(
        "not available in your country",
        "not available in your region",
        "not available in your state",
        "unavailable in your country",
        "unavailable in your region",
        "blocked in your country",
        "disable access to our site",
        "disabled access to our site",
        "access to this site has been disabled",
        "restricted in your region",
        "your elected officials",
        "geo-restricted",
        "geoblocked"
    )

    val ageWallUrlHints = listOf("age-verification", "ageverification", "age_verification", "age-check")
    val ageWallPhrases = listOf(
        "verify your age",
        "age verification is required",
        "ageverificationwrapper",
        "age-verification-wrapper",
        "you must be 18",
        "are you 18 or older",
        "agedisclaimer"
    )

    // ---- Detail (watch) page ----
    val detailTitle = listOf(
        "meta[property=og:title]",
        "meta[name=twitter:title]",
        "h1.title span.inlineFree",
        "h1.title",
        ".video-title",
        "title"
    )
    val detailThumbnail = listOf("meta[property=og:image]", "meta[name=twitter:image]")
    val detailDurationMeta = listOf("meta[property=video:duration]", "meta[itemprop=duration]")
    val detailViews = listOf(".rating-info-container .views .count", ".views .count", "span.count", ".views")

    val tagLinks = listOf(
        ".tagsWrapper a",
        ".categoriesWrapper a",
        "a[data-label=tag]",
        "a[data-label=Tag]",
        ".video-detailed-info a[href*=/video/search]",
        ".video-info-row a[href*=/categories/]"
    )
    val tagIgnore = setOf("suggest", "+", "add", "more", "show more", "show less", "categories", "tags")
    const val keywordsMeta = "meta[name=keywords]"

    val relatedContainers = listOf(
        "#relatedVideosCenter",
        "#relatedVideos",
        ".relatedVideos",
        "#relateRecommendedItems",
        "#recommendedVideos"
    )
}
