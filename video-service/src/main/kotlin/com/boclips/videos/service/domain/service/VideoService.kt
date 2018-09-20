package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.Video

interface VideoService {
    fun search(query: String): SearchResults
    fun findById(id: String): Video?
}

data class SearchResults(
        val searchId: String,
        val query: String,
        val videos: List<Video>
)
