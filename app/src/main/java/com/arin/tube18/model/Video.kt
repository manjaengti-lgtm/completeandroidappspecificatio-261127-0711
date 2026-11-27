package com.arin.tube18.model

data class Video(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val duration: String,
    val views: String
)

data class VideoDetail(
    val video: Video,
    val tags: List<String>,
    val related: List<Video>
)

enum class SearchChip(val label: String) {
    AMATEUR("Amateur"),
    HD("HD"),
    POPULAR("Popular"),
    NEWEST("Newest")
}

/** search == null means a browse feed (popular / category / sort). */
data class FeedQuery(
    val search: String? = null,
    val chip: SearchChip? = null
)
