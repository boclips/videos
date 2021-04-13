package com.boclips.videos.service.presentation

import com.boclips.videos.api.response.taxonomy.TaxonomyResource
import com.boclips.videos.service.application.GetAllCategories
import com.boclips.videos.service.presentation.converters.TaxonomyResourceConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class TaxonomyController {

    @Autowired
    lateinit var getAllCategories: GetAllCategories

    @GetMapping("/categories")
    fun getCategories(): ResponseEntity<TaxonomyResource> {
        val categories = getAllCategories()
        val taxonomyResource = TaxonomyResourceConverter.toResource(categories)
        return ResponseEntity(taxonomyResource, HttpStatus.OK)
    }
}
