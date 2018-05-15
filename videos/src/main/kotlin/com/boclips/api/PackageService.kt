package com.boclips.api

import com.boclips.api.infrastructure.PackageEntity
import com.boclips.api.infrastructure.SearchFilter
import com.boclips.api.infrastructure.SearchFilterType
import com.boclips.api.presentation.IllegalFilterException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PackageService(
        val packageRepository: PackageRepository
) {
    fun getById(packageId: String): Mono<com.boclips.api.Package> {
        return packageRepository.findById(packageId).map { it.toPackage() }
    }

    fun getAll() = packageRepository.findAll().map { it.toPackage() }

    fun excludeContentProvider(packageId: String, contentProviderId: String): Mono<PackageEntity> {
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
