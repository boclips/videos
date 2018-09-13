package com.boclips.videos.service.infrastructure.search


interface SearchService {
    fun search(query: String): SearchResults
    fun findById(id: String): ElasticSearchVideo?
}