package com.boclips.search.service.domain.videos

import com.boclips.search.service.domain.PaginatedSearchRequest

interface VideoSearchService {
    fun search(searchRequest: PaginatedSearchRequest<VideoQuery>): List<String>
    fun count(videoQuery: VideoQuery): Long
}
