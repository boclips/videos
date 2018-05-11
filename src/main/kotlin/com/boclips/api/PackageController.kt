package com.boclips.api

import com.boclips.api.infrastructure.WebfluxLinkBuilder
import com.boclips.api.infrastructure.toResourceOfResources
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("packages")
class PackageController(
        val packageRepository: PackageRepository
) {

    @GetMapping
    fun getPackages(uriBuilder: UriComponentsBuilder) = packageRepository.findAll().toResourceOfResources({
        listOf(
                WebfluxLinkBuilder.fromContextPath(uriBuilder)
                        .slash("packages")
                        .slash(it.id)
                        .slash("search-filters?type=exclude=true")
                        .withRel("excludeContentProvider"))
    })

    @PatchMapping("/{packageId}/content-providers", params = ["exclude=true"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excludeContentProvider(
            @RequestBody contentProviderUrl: String,
            @PathVariable("packageId") id: String
    ) {

    }

}