package com.boclips.api.domain.services

import com.boclips.api.domain.model.Package
import com.boclips.api.infrastructure.packages.PackageEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PackageService {
    fun getById(packageId: String): Mono<Package>
    fun getAll(): Flux<Package>
    fun excludeContentProvider(packageId: String, contentProviderId: String): Mono<PackageEntity>
}

