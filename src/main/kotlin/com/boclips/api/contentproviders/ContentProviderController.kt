package com.boclips.api.contentproviders

import com.boclips.api.presentation.ResourceNotFoundException
import com.boclips.api.presentation.resources.ContentProvider
import org.springframework.hateoas.Resources
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
            contentProviderService.getAll()
                    .map { ContentProvider.fromContentProvider(it, uriBuilder) }
                    .collectList()
                    .map { Resources(it) }

    @GetMapping("/{contentProviderId}")
    fun getContentProvider(@PathVariable contentProviderId: String, uriBuilder: UriComponentsBuilder) =
            contentProviderService.getById(contentProviderId)
                    .map { ContentProvider.fromContentProvider(it, uriBuilder) }
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