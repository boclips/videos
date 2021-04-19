package com.boclips.videos.service.infrastructure.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors

object CategoryWithAncestorsDocumentConverter {
    fun toDocument(category: CategoryWithAncestors): CategoryWithAncestorsDocument =
        CategoryWithAncestorsDocument(
            codeValue = category.codeValue.value,
            description = category.description,
            ancestors = category.ancestors.map { it.value }.toSet()
        )

    fun toCategoryWithAncestors(document: CategoryWithAncestorsDocument): CategoryWithAncestors =
        CategoryWithAncestors(
            codeValue = CategoryCode(document.codeValue),
            description = document.description,
            ancestors = document.ancestors.map { CategoryCode(it) }.toSet()
        )
}
