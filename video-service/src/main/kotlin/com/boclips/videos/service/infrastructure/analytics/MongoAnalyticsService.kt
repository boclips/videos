package com.boclips.videos.service.infrastructure.analytics

import com.boclips.videos.service.domain.service.AnalyticsService

class MongoAnalyticsService(private val analyticsRepository: AnalyticsRepository) : AnalyticsService {

    override fun saveSearch(query: String, resultsReturned: Int) {
        val event = SearchEvent(query,resultsReturned)
        analyticsRepository.insert(event)
    }
}