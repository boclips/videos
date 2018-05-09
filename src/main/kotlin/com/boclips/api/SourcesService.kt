package com.boclips.api

import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

@Component
class SourcesService(val sourcesRepository: SourcesRepository) {
    fun getAllSources(): Flux<Source> {
        return sourcesRepository.findAll()
    }

    fun createSource(name: String): Mono<Boolean> {
        return sourcesRepository.findByName(name)
                .map { false }
                .defaultIfEmpty(true)
                .filter { it }
                .flatMap {
                    val now = ZonedDateTime.now(ZoneOffset.UTC).toString()
                    sourcesRepository.save(Source(name = name, dateCreated = now, dateUpdated = now, uuid = UUID.randomUUID().toString()))
                            .map { true }
                }
                .defaultIfEmpty(false)
    }
}