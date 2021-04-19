package com.boclips.videos.service.application

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.service.taxonomy.CategoryRepository
import org.springframework.stereotype.Component

@Component
class GetAllCategories(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): List<Category> = categoryRepository.findAll()
}
