package com.boclips.videos.service.application

import com.boclips.videos.service.application.exceptions.CategoryNotFoundException
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.service.taxonomy.CategoryRepository
import org.springframework.stereotype.Component

@Component
class GetCategoryWithAncestors(
    val categoryRepository: CategoryRepository
) {

    operator fun invoke(rawCode: String): CategoryWithAncestors {
        val code = CategoryCode(rawCode)
        val category = categoryRepository.findByCode(code)
        return category?.let {
            CategoryWithAncestors(
                codeValue = it.code,
                description = it.description,
                ancestors = it.resolveAncestorsCodes()
            )
        } ?: throw CategoryNotFoundException(rawCode)
    }
}
