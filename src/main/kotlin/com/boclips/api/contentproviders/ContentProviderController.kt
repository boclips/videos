package com.boclips.api.contentproviders

import com.boclips.api.infrastructure.toResourceOfResources
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/content-providers")
class ContentProviderController(val contentProviderService: ContentProviderService) {

    @GetMapping
    fun getContentProviders() = contentProviderService.getAllContentProviders().toResourceOfResources()

    @PutMapping
    @RequestMapping("/{name}")
    fun putContentProvider(@PathVariable("name") name: String): Mono<ResponseEntity<Void>> {

        return contentProviderService.createContentProvider(name)
                .map {
                    val status = if(it) HttpStatus.CREATED else HttpStatus.OK
                    ResponseEntity<Void>(status)
                }
    }

    @DeleteMapping("/SNTV")
    fun deleteContentProvider(): Mono<DeleteResult> {
        return contentProviderService.deleteContentProvider("SNTV")
    }
}