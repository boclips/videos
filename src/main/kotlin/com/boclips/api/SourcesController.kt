package com.boclips.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/sources")
class SourcesController(val sourcesService: SourcesService) {

    @GetMapping
    fun getSources(): Any {
        return sourcesService.getAllSources()
    }

    @PutMapping
    @RequestMapping("/{name}")
    fun putSource(@PathVariable("name") name: String): Mono<ResponseEntity<Void>> {

        return sourcesService.createSource(name)
                .map {
                    val status = if(it) HttpStatus.CREATED else HttpStatus.OK
                    ResponseEntity<Void>(status)
                }
    }
}
