package com.boclips.api

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class ContentProviderController(val contentProviderService: ContentProviderService) {

    @DeleteMapping("/content-providers/SNTV")
    fun deleteContentProvider(): Mono<DeleteResult> {
        return contentProviderService.deleteContentProvider("SNTV")
    }
}