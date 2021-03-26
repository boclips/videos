package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.GetAllTaxonomies
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
    fun getTaxonomies(): ResponseEntity<Any> {
        val taxonomies = getAllTaxonomies()
        val taxonomyResource = TaxonomyResource(taxonomies)
        return ResponseEntity(HttpStatus.OK)
    }
}
