package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.taxonomy.CategoryResource
import com.boclips.videos.api.response.taxonomy.CategoryTreeResource
import com.boclips.videos.api.response.video.VideoCategoryResource
import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors

class CategoryResourceConverter {
    fun toResource(categories: List<Category>): CategoryResource {
        return CategoryResource(
            _embedded = categories
                .filter { it.isRootCategory() }
                .map { it.code.value to buildTree(categories, it) }
                .toMap()
        )
    }

    fun reverseBuildTree(categories: List<Category>, current: CategoryWithAncestors): VideoCategoryResource {
        val parentCode = categories.find { it.code == current.codeValue }?.parentCode
        val parentDescription = categories.find { it.code == parentCode }?.description

        if (parentCode != null && parentDescription != null) {

            return VideoCategoryResource(
                code = current.codeValue.value,
                value = current.description,
                parent = reverseBuildTree(
                    categories,
                    CategoryWithAncestors(
                        codeValue = parentCode,
                        description = parentDescription
                    )
                )
            )
        }
        return VideoCategoryResource(
            code = current.codeValue.value,
            value = current.description
        )

    }

    private fun buildTree(categories: List<Category>, current: Category): CategoryTreeResource {
        val children = categories.filter { it.parentCode == current.code }

        if (children.isNotEmpty()) {
            return CategoryTreeResource(
                description = current.description,
                children = children.map { child ->
                    child.code.value to buildTree(
                        categories = filterRelevant(categories, child.code),
                        current = child
                    )
                }.toMap()
            )
        }

        return CategoryTreeResource(description = current.description, children = emptyMap())
    }

    private fun filterRelevant(categories: List<Category>, relevantCode: CategoryCode) =
        categories.filter { it.code.value.startsWith(relevantCode.value) }
}
