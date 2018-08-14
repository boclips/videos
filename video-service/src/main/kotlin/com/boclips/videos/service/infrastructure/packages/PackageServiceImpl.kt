package com.boclips.videos.service.infrastructure.packages

import com.boclips.videos.service.domain.model.Package
import com.boclips.videos.service.domain.services.PackageService
import com.boclips.videos.service.presentation.IllegalFilterException
import reactor.core.publisher.Mono

class PackageServiceImpl(
        val packageRepository: PackageRepository
) : PackageService {
    override fun getById(packageId: String): Mono<Package> {
        return packageRepository.findById(packageId).map { it.toPackage() }
    }

    override fun getAll() = packageRepository.findAll().map { it.toPackage() }

    override fun excludeContentProvider(packageId: String, contentProviderId: String): Mono<PackageEntity> {
        return packageRepository.findById(packageId)
                .map { p ->
                    var filter = p.searchFilters.firstOrNull { it._refType == SearchFilterType.Source }

                    if (filter == null) {
                        filter = SearchFilter(SearchFilterType.Source, true, mutableSetOf())
                        p.searchFilters.add(filter)
                    }

                    if (!filter.invertFilter) throw IllegalFilterException()

                    filter.items.add(contentProviderId)

                    return@map p
                }
                .flatMap { packageRepository.save(it) }
    }
}