package com.boclips.videos.service.infrastructure.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors

object VideoCategoriesDocumentConverter {
    fun toDocument(category: CategoryWithAncestors): CategoryWithAncestorsDocument =
        CategoryWithAncestorsDocument(
            codeValue = category.codeValue.value,
            description = category.description,
            ancestors = category.ancestors.map { it.value }.toSet()
        )

    fun fromDocument(categories: CategoryWithAncestorsDocument): CategoryWithAncestors =
        CategoryWithAncestors(
            codeValue = CategoryCode(categories.codeValue),
            description = categories.description,
            ancestors = categories.ancestors.map { CategoryCode(it) }.toSet()
        )
}
