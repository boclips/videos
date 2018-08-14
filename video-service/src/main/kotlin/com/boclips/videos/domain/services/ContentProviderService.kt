package com.boclips.videos.domain.services

import com.boclips.videos.domain.model.ContentProvider
import com.boclips.videos.domain.model.DeleteResult
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ContentProviderService {
    fun deleteByName(contentProviderName: String): Mono<DeleteResult>
    fun getAll(): Flux<ContentProvider>
    fun create(name: String): Mono<Boolean>
    fun getById(contentProviderId: String): Mono<ContentProvider>
}