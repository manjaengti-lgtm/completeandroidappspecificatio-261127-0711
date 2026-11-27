package com.arin.tube18.data

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.arin.tube18.model.FeedQuery
import com.arin.tube18.model.Video
import kotlin.coroutines.cancellation.CancellationException

fun videoPagingConfig() = PagingConfig(
    pageSize = 32,
    prefetchDistance = 10,
    initialLoadSize = 32,
    enablePlaceholders = false
)

class VideoPagingSource(
    private val repository: VideoRepository,
    private val query: FeedQuery
) : PagingSource<Int, Video>() {

    private val seenIds = HashSet<String>()

    override fun getRefreshKey(state: PagingState<Int, Video>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Video> {
        val start = params.key ?: 1
        return try {
            var page = start
            var fresh: List<Video> = emptyList()
            var end = false
            for (attempt in 0 until 3) {
                val result = repository.fetchPage(query, page)
                fresh = synchronized(seenIds) { result.items.filter { seenIds.add(it.id) } }
                end = result.endReached
                if (end || fresh.isNotEmpty()) break
                page++
            }
            LoadResult.Page(
                data = fresh,
                prevKey = null,
                nextKey = if (end || fresh.isEmpty()) null else page + 1
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
