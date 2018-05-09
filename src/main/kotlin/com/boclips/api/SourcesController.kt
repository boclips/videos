package com.boclips.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sources")
class SourcesController(val sourcesService: SourcesService) {

    @GetMapping
    fun getSources(): Any {
        return sourcesService.getAllSources()
    }

    @PutMapping
    @RequestMapping("/{name}")
    fun putSource(@PathVariable("name") name: String): ResponseEntity<Void> {

        val created = sourcesService.createSource(name)

        val status = if(created) HttpStatus.CREATED else HttpStatus.OK
        return ResponseEntity(status)
    }

}
