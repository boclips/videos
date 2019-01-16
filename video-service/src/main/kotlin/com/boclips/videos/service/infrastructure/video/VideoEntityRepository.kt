package com.boclips.videos.service.infrastructure.video

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VideoEntityRepository : CrudRepository<VideoEntity, Long> {
    fun countBySourceAndUniqueId(contentPartnerId: String, partnerVideoId: String): Int

    fun findAllByIdIn(ids: List<Long>): List<VideoEntity>
}