package com.boclips.videos.infrastructure.videos

import com.boclips.videos.domain.model.Video
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional

interface VideoRepository : CrudRepository<Video, Long> {

    @Transactional
    fun deleteBySource(source: String)

    fun findBySource(source: String): Set<Video>
}
