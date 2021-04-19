package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.taxonomy.TaxonomyResource
import com.boclips.videos.api.response.taxonomy.TaxonomyTreeResource
import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode

object TaxonomyResourceConverter {

    fun toResource(categories: List<Category>): TaxonomyResource {
        return TaxonomyResource(
            _embedded = categories.map { parentTaxonomy ->
                parentTaxonomy.code.value to
                    buildTree(categories, parentTaxonomy)
            }.toMap()
        )
    }

    private fun buildTree(taxonomyCategories: List<Category>, current: Category): TaxonomyTreeResource {
        val children = taxonomyCategories.filter { it.parentCode == current.code }

        if (children.isNotEmpty()) {
            return TaxonomyTreeResource(
                description = current.description,
                children = children.map { child ->
                    child.code.value to buildTree(
                        taxonomyCategories = filterRelevant(taxonomyCategories, child.code),
                        current = child
                    )
                }.toMap()
            )
        }

        return TaxonomyTreeResource(description = current.description, children = emptyMap())
    }

    private fun filterRelevant(taxonomyCategories: List<Category>, relevantCode: CategoryCode) =
        taxonomyCategories.filter { it.code.value.startsWith(relevantCode.value) }
}
