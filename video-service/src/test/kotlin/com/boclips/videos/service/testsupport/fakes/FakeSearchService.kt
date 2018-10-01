package com.boclips.videos.service.testsupport.fakes

import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.service.SearchService

class FakeSearchService : SearchService {
    override fun search(query: String): List<VideoId> {
        return listOf(VideoId(videoId = "123", referenceId = "ref-id-1"))
    }
}