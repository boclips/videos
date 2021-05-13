package com.boclips.contentpartner.service.infrastructure.channel

import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.infrastructure.taxonomy.CategoryWithAncestorsDocument

object CategoriesDocumentConverter {
    fun toDocument(category: CategoryWithAncestors): CategoryWithAncestorsDocument =
        CategoryWithAncestorsDocument(
            codeValue = category.codeValue.value,
            description = category.description,
            ancestors = category.ancestors.map { it.value }.toSet()
        )

    fun fromDocument(categories: Set<CategoryWithAncestorsDocument>?): Set<CategoryWithAncestors> =
        categories?.map { categoryDocument ->
            CategoryWithAncestors(
                codeValue = CategoryCode(categoryDocument.codeValue),
                description = categoryDocument.description,
                ancestors = categoryDocument.ancestors.map { CategoryCode(it) }.toSet()
            )
        }?.toSet() ?: emptySet()
}
