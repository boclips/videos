package com.boclips.videos.service.presentation

import com.boclips.videos.api.response.taxonomy.TaxonomyResource
import com.boclips.videos.service.application.GetAllTaxonomies
import com.boclips.videos.service.presentation.converters.TaxonomyResourceConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/taxonomies")
class TaxonomyController {

    @Autowired
    lateinit var getAllTaxonomies: GetAllTaxonomies


    @GetMapping
    fun getTaxonomies(): ResponseEntity<TaxonomyResource> {
        val taxonomies = getAllTaxonomies()
        val taxonomyResource = TaxonomyResourceConverter.toResource(taxonomies)
        return ResponseEntity(taxonomyResource, HttpStatus.OK)
    }
}
