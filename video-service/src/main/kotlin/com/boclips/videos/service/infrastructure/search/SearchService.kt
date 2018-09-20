package com.boclips.videos.service.infrastructure.search

interface SearchService {
    fun search(query: String): ElasticSearchResults
    fun findById(id: String): ElasticSearchVideo?
}
