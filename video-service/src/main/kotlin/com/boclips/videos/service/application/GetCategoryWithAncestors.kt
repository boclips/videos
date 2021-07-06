package com.boclips.videos.service.application

import com.boclips.videos.service.application.exceptions.CategoryNotFoundException
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.service.taxonomy.CategoryService
import org.springframework.stereotype.Component

@Component
class GetCategoryWithAncestors(
    val categoryService: CategoryService
) {

    operator fun invoke(rawCode: String): CategoryWithAncestors {
        val code = CategoryCode(rawCode)
        val category = categoryService.findByCode(code)
        return category?.let {
            CategoryWithAncestors(
                codeValue = it.code,
                description = it.description,
                ancestors = it.resolveAncestorsCodes()
            )
        } ?: throw CategoryNotFoundException(rawCode)
    }
}
