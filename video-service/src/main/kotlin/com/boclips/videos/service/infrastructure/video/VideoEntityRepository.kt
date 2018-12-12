package com.boclips.videos.service.infrastructure.video

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.stream.Stream

@Repository
interface VideoEntityRepository : CrudRepository<VideoEntity, Long> {
    fun countBySourceAndUniqueId(contentPartnerId: String, partnerVideoId: String): Int

    @Query("select * from metadata_orig", nativeQuery = true)
    fun readAll(): Stream<VideoEntity>

    fun findAllByIdIn(ids: List<Long>): List<VideoEntity>
}