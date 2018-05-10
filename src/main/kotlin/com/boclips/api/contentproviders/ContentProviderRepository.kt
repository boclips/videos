package com.boclips.api.contentproviders

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono


interface ContentProviderRepository : ReactiveMongoRepository<ContentProvider, String> {
    fun findByName(name: String): Mono<ContentProvider>
}