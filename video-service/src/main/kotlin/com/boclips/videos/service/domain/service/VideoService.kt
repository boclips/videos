package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.Video

interface VideoService {
    fun find(query: String): List<Video>
    fun findById(id: String): Video?
}