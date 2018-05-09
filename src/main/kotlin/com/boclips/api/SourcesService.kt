package com.boclips.api

import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

@Component
class SourcesService(val sourcesRepository: SourcesRepository) {
    fun getAllSources(): List<Source> {
        return sourcesRepository.findAll()
    }

    fun createSource(name: String): Boolean {
        val existingSource = sourcesRepository.findByName(name)

        if(existingSource != null) {
            return false
        }

        val now = ZonedDateTime.now(ZoneOffset.UTC).toString()
        sourcesRepository.save(Source(name = name, dateCreated = now, dateUpdated = now, uuid = UUID.randomUUID().toString()))
        return true
    }
}