package com.boclips.videos.service.testsupport.fakes

import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.service.SearchService

class FakeSearchService : SearchService {
    private lateinit var searchIndex: MutableSet<VideoId>

    override fun removeFromSearch(videoId: VideoId) {
        searchIndex.minusAssign(videoId)
    }

    override fun search(query: String): List<VideoId> {
        if (query == "this is a query not returning any search results") return emptyList()
        return searchIndex.toList()
    }

    override fun isIndexed(videoId: VideoId): Boolean {
        return searchIndex.contains(videoId)
    }

    fun clear() {
        this.searchIndex = mutableSetOf()
    }

    fun addToIndex(videoId: VideoId) {
        this.searchIndex.add(videoId)
    }

}