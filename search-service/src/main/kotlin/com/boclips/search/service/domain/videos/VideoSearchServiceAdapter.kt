package com.boclips.search.service.domain.videos

import com.boclips.search.service.domain.AdminSearchService
import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery

abstract class VideoSearchServiceAdapter<T>(
    private val queryService: VideoSearchService,
    private val adminService: AdminSearchService<VideoMetadata>
) : VideoSearchService, AdminSearchService<T> {
    override fun safeRebuildIndex(videos: Sequence<T>, notifier: ProgressNotifier?) {
        adminService.safeRebuildIndex(videos.map(::convert), notifier)
    }

    override fun upsert(videos: Sequence<T>, notifier: ProgressNotifier?) {
        adminService.upsert(videos.map(::convert), notifier)
    }

    override fun search(searchRequest: PaginatedSearchRequest<VideoQuery>): List<String> {
        return queryService.search(searchRequest)
    }

    override fun count(videoQuery: VideoQuery): Long {
        return queryService.count(videoQuery)
    }

    override fun removeFromSearch(videoId: String) {
        adminService.removeFromSearch(videoId)
    }

    abstract fun convert(document: T): VideoMetadata
}
