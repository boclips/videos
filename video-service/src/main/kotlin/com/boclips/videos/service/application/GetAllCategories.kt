package com.boclips.videos.service.application

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.service.taxonomy.CategoryService
import org.springframework.stereotype.Component

@Component
class GetAllCategories(
    private val categoryService: CategoryService
) {
    operator fun invoke(): List<Category> = categoryService.findAll()
}
