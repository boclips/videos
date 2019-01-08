package com.boclips.videos.service.infrastructure.video

import org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.stream.Stream
import javax.persistence.QueryHint

@Repository
interface VideoEntityRepository : CrudRepository<VideoEntity, Long> {
    fun countBySourceAndUniqueId(contentPartnerId: String, partnerVideoId: String): Int

    @QueryHints(value = [QueryHint(name = HINT_FETCH_SIZE, value = "" + Integer.MIN_VALUE)])
    @Query("select * from metadata_orig", nativeQuery = true)
    fun readAll(): Stream<VideoEntity>

    fun findAllByIdIn(ids: List<Long>): List<VideoEntity>
}