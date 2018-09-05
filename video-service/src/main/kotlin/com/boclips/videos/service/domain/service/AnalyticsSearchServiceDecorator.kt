package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.Video

class AnalyticsSearchServiceDecorator(
        private val searchService: SearchService,
        private val analyticsService: AnalyticsService
) : SearchService {

    override fun search(query: String): List<Video> {
        return searchService.search(query)
                .apply { analyticsService.saveSearch(query, this.size) }
    }
}