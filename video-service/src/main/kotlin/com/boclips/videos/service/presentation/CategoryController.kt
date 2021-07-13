package com.boclips.videos.service.presentation

import com.boclips.videos.api.response.taxonomy.CategoryResource
import com.boclips.videos.service.application.GetAllCategories
import com.boclips.videos.service.presentation.converters.CategoryResourceConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/categories")
class CategoryController {

    @Autowired
    lateinit var getAllCategories: GetAllCategories

    @GetMapping
    fun getCategories(): ResponseEntity<CategoryResource> {
        val categories = getAllCategories()
        val categoriesResource = CategoryResourceConverter.toResource(categories)
        return ResponseEntity(categoriesResource, HttpStatus.OK)
    }
}
