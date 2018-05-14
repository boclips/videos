package com.boclips.api

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PackageService(val packageRepository: PackageRepository) {
    fun getById(packageId: String): Mono<com.boclips.api.Package> {
        return packageRepository.findById(packageId).map { it.toPackage() }
    }
    fun getAll() = packageRepository.findAll().map { it.toPackage() }
}
