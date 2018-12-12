package com.boclips.videos.service.infrastructure.video.subject

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SubjectCrudRepository : CrudRepository<VideoSubjectEntity, VideoSubjectId> {
    fun findByVideoIdIn(videoIds: List<Long>): List<VideoSubjectEntity>
    fun findByVideoId(videoId: Long): List<VideoSubjectEntity>
}