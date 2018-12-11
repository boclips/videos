package com.boclips.videos.service.infrastructure.video

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VideoSubjectRepository : CrudRepository<VideoSubject, VideoSubjectId> {
    fun findByVideoIdIn(videoIds: List<Long>): List<VideoSubject>
}