package com.boclips.api.infrastructure

import com.boclips.api.infrastructure.ContentProviderEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono


interface ContentProviderRepository : ReactiveMongoRepository<ContentProviderEntity, String> {
    fun findByName(name: String): Mono<ContentProviderEntity>
}