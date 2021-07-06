package com.boclips.videos.service.domain.service.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryTree
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors

class CategoryService(
    private var categoryRepository: CategoryRepository
) {
    fun findAll(): List<Category> =
        categoryRepository.findAll()

    fun findByCode(code: CategoryCode): Category? =
        categoryRepository.findByCode(code)

    fun create(category: Category): Category =
        categoryRepository.create(category)

    fun buildTreeFromChild(child: CategoryWithAncestors): CategoryTree = reverseBuildTree(findAll(), child)

    private fun reverseBuildTree(categories: List<Category>, current: CategoryWithAncestors): CategoryTree {
        return categories.find { it.code == current.codeValue }?.parentCode?.let { parentCode ->
            categories.find { it.code == parentCode }?.let { parent ->
                CategoryTree(
                    codeValue = current.codeValue,
                    description = current.description,
                    parent = reverseBuildTree(
                        categories,
                        CategoryWithAncestors(
                            codeValue = parent.code,
                            description = parent.description,
                        )
                    )
                )
            }
        } ?: CategoryTree(
            codeValue = current.codeValue,
            description = current.description
        )
    }
}
