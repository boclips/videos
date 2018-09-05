package com.boclips.videos.service.infrastructure.analytics

sealed class AnalyticsEvent<TData>(val type: String, val data: TData)

data class SearchEventData(val query: String, val resultsReturned: Int)

class SearchEvent(query: String, resultsReturned: Int) : AnalyticsEvent<SearchEventData>("SEARCH", SearchEventData(query, resultsReturned)) {

}
