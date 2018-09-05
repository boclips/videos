package com.boclips.videos.service.domain.service

interface AnalyticsService {

    fun saveSearch(query: String, resultsReturned: Int)

}