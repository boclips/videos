package com.boclips.api

import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional

interface VideoRepository : CrudRepository<Video, Long> {

    @Transactional
    fun deleteBySource(source: String)

    fun findBySource(source: String): Set<Video>
}
