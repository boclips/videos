package com.boclips.videos.domain.services

import com.boclips.videos.domain.model.Package
import com.boclips.videos.infrastructure.packages.PackageEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PackageService {
    fun getById(packageId: String): Mono<Package>
    fun getAll(): Flux<Package>
    fun excludeContentProvider(packageId: String, contentProviderId: String): Mono<PackageEntity>
}

