package com.boclips.api.contentproviders

import com.boclips.api.infrastructure.WebfluxLinkBuilder
import com.boclips.api.infrastructure.toResourceOfResources
import com.boclips.api.presentation.ContentProviderResource
import com.boclips.api.presentation.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/content-providers")
class ContentProviderController(val contentProviderService: ContentProviderService) {

    @GetMapping
    fun getContentProviders(uriBuilder: UriComponentsBuilder) =
            contentProviderService.getAllContentProviders().toResourceOfResources({
                listOf(
                        WebfluxLinkBuilder.fromContextPath(uriBuilder).slash("/content-providers").slash(it.id).withSelfRel())
            })

    @GetMapping("/{contentProviderId}")
    fun getContentProvider(@PathVariable contentProviderId: String, uriBuilder: UriComponentsBuilder) =
            contentProviderService.getById(contentProviderId)
                    .map { ContentProviderResource(it.name) }
                    .doOnNext { it.add(WebfluxLinkBuilder.fromContextPath(uriBuilder).slash("/content-providers").slash(contentProviderId).withSelfRel()) }
                    .switchIfEmpty(Mono.error(ResourceNotFoundException()))


    @PostMapping
    fun putContentProvider(@RequestBody contentProvider: ContentProvider) =
            contentProviderService.createContentProvider(contentProvider.name)
                    .map {
                        val status = if (it) HttpStatus.CREATED else HttpStatus.OK
                        ResponseEntity<Void>(status)
                    }

    @DeleteMapping("/SNTV")
    fun deleteContentProvider(): Mono<DeleteResult> {
        return contentProviderService.deleteContentProvider("SNTV")
    }
}