package com.boclips.api

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono


interface SourcesRepository : ReactiveMongoRepository<Source, String> {
    fun findByName(name: String): Mono<Source>
}