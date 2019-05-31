package com.boclips.search.service.domain.videos

import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery

abstract class VideoSearchServiceAdapter<T>(
    private val readSearchService: ReadSearchService<VideoMetadata, VideoQuery>,
    private val writeSearchService: WriteSearchService<VideoMetadata>
) : ReadSearchService<VideoMetadata, VideoQuery>, WriteSearchService<T> {

    override fun safeRebuildIndex(videos: Sequence<T>, notifier: ProgressNotifier?) {
        writeSearchService.safeRebuildIndex(videos.map(::convert), notifier)
    }

    override fun upsert(videos: Sequence<T>, notifier: ProgressNotifier?) {
        writeSearchService.upsert(videos.map(::convert), notifier)
    }

    override fun search(searchRequest: PaginatedSearchRequest<VideoQuery>): List<String> {
        return readSearchService.search(searchRequest)
    }

    override fun count(videoQuery: VideoQuery): Long {
        return readSearchService.count(videoQuery)
    }

    override fun removeFromSearch(videoId: String) {
        writeSearchService.removeFromSearch(videoId)
    }

    abstract fun convert(document: T): VideoMetadata
}
