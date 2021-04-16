package com.boclips.videos.service.domain.service.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.Categories
import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategory
import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategoryWithAncestors
import com.boclips.videos.service.domain.service.video.TaxonomyRepository
import java.lang.RuntimeException

class TaxonomyService(
    private val taxonomyRepository: TaxonomyRepository
) {

    fun getCategories(): Categories {
        val categories = taxonomyRepository.findAll()
        val roots = categories.filter { it.parentCode == null }

        return roots.map { root ->
            root.codeValue to buildTree(filterRelevant(categories, root.codeValue), root)
        }.toMap()
    }

    fun getTaxonomyCategoryWithAncestors(code: String): TaxonomyCategoryWithAncestors =
        taxonomyRepository.findByCode(code)?.let { taxonomyCategory ->
            TaxonomyCategoryWithAncestors(
                codeValue = code,
                description = taxonomyCategory.description,
                ancestors = getAncestorCodes(taxonomyCategory)
            )
        } ?: throw RuntimeException()

    private fun getAncestorCodes(taxonomyCategory: TaxonomyCategory): Set<String> {
        val parent = taxonomyCategory.parentCode?.let { taxonomyRepository.findByCode(it) }
        return parent?.let { getAncestorCodes(parent).plus(parent.codeValue) } ?: mutableSetOf()
    }

    private fun buildTree(taxonomyCategories: List<TaxonomyCategory>, current: TaxonomyCategory): Category {
        val children = taxonomyCategories.filter { it.parentCode == current.codeValue }

        if (children.isNotEmpty()) {
            return Category(
                description = current.description,
                code = current.codeValue,
                children = children.map { child ->
                    child.codeValue to buildTree(
                        taxonomyCategories = filterRelevant(taxonomyCategories, child.codeValue),
                        current = child
                    )
                }.toMap()
            )
        }

        return Category(description = current.description, code = current.codeValue, children = emptyMap())
    }

    private fun filterRelevant(taxonomyCategories: List<TaxonomyCategory>, relevantCode: String) =
        taxonomyCategories.filter { it.codeValue.startsWith(relevantCode) }
}
