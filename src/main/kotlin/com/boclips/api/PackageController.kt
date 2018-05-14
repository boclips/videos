package com.boclips.api

import com.boclips.api.infrastructure.configuration.WebfluxLinkBuilder
import com.boclips.api.presentation.resources.Package
import com.boclips.api.presentation.ResourceNotFoundException
import org.springframework.hateoas.Resources
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

@RestController
@RequestMapping("packages")
class PackageController(
        val packageService: PackageService
) {

    @GetMapping
    fun getPackages(uriBuilder: UriComponentsBuilder): Mono<Resources<Package>> =
            packageService.getAll()
                    .map { Package.fromPackage(it, uriBuilder) }
                    .collectList()
                    .map { Resources(it) }

    @PatchMapping("/{packageId}/content-providers", params = ["exclude=true"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excludeContentProvider(
            @RequestBody contentProviderUrl: String,
            @PathVariable("packageId") id: String
    ) {

    }

    @GetMapping("/{packageId}")
    fun getPackage(@PathVariable packageId: String, uriBuilder: UriComponentsBuilder) =
            packageService.getById(packageId)
                    .map { Package.fromPackage(it, uriBuilder) }
                    .switchIfEmpty(ResourceNotFoundException().toMono())
}
