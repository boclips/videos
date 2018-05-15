package com.boclips.api.infrastructure.videos

import com.boclips.api.domain.model.Video
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional

interface VideoRepository : CrudRepository<Video, Long> {

    @Transactional
    fun deleteBySource(source: String)

    fun findBySource(source: String): Set<Video>
}
