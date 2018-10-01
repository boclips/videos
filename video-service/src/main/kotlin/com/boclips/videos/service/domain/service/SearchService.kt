package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.VideoId

interface SearchService {
    fun search(query: String): List<VideoId>
}
