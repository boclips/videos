package com.boclips.videos.service.testsupport.fakes

import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.service.SearchService

class FakeSearchService : SearchService {
    private lateinit var searchIndex: MutableSet<VideoId>

    init {
        this.reset()
    }

    override fun removeFromSearch(videoId: VideoId) {
        searchIndex.minusAssign(videoId)
    }

    override fun search(query: String): List<VideoId> {
        return searchIndex.toList()
    }

    override fun isIndexed(videoId: VideoId): Boolean {
        return searchIndex.contains(videoId)
    }

    fun reset() {
        this.searchIndex = mutableSetOf(VideoId(videoId = "123", referenceId = "ref-id-1"))
    }

}