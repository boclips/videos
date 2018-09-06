package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.Video

interface SearchService {
    fun search(query: String): List<Video>
}