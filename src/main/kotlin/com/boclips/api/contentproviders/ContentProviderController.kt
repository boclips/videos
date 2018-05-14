package com.boclips.api.contentproviders

import com.boclips.api.infrastructure.configuration.WebfluxLinkBuilder
import com.boclips.api.infrastructure.toResourceOfResources
import com.boclips.api.presentation.resources.ContentProvider
import com.boclips.api.presentation.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

@RestController
@RequestMapping("/content-providers")
class ContentProviderController(val contentProviderService: ContentProviderService) {

    @GetMapping
    fun getContentProviders(uriBuilder: UriComponentsBuilder) =
            contentProviderService.getAll().toResourceOfResources({
                listOf(
                        WebfluxLinkBuilder.fromContextPath(uriBuilder).slash("/content-providers").slash(it.id).withSelfRel())
            })

    @GetMapping("/{contentProviderId}")
    fun getContentProvider(@PathVariable contentProviderId: String, uriBuilder: UriComponentsBuilder) =
            contentProviderService.getById(contentProviderId)
                    .map { ContentProvider(it.name) }
                    .doOnNext { it.add(WebfluxLinkBuilder.fromContextPath(uriBuilder).slash("/content-providers").slash(contentProviderId).withSelfRel()) }
                    .switchIfEmpty(ResourceNotFoundException().toMono())

    @PostMapping
    fun putContentProvider(@RequestBody contentProvider: ContentProvider) =
            contentProviderService.create(contentProvider.name)
                    .map {
                        val status = if (it) HttpStatus.CREATED else HttpStatus.OK
                        ResponseEntity<Void>(status)
                    }

    @DeleteMapping("/SNTV")
    fun deleteContentProvider(): Mono<DeleteResult> {
        return contentProviderService.deleteByName("SNTV")
    }
}